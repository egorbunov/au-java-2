package ru.spbau.mit.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Session for one client-server connection; It represents
 * client-server dialogue. Class is responsible for reading
 * client requests and serving them accordingly to protocol:
 * <p>
 * 1) Every client must open two connections:
 * * data channel
 * * status channel
 * - Status channel is write only (all input from client is ignored)
 * - Status of serving a command is always sent to status channel
 * - Data channel is used to read requests and sent responses
 * <p>
 * 2) See {@link Command} docs for response/request syntax and rules
 * <p>
 * In {@code FtpSession} class status channel is represented by {@code statusOut}
 * output stream and data channel ends by {@code dataIn} and {@code dataOut}
 */
public class FtpSession implements Runnable {
    private Logger logger = Logger.getLogger("FtpSession");

    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;
    private final DataOutputStream statusOut;

    private boolean isRunning = false;

    public FtpSession(InputStream dataIn,
                      OutputStream dataOut,
                      OutputStream statusOut) {
        this.dataIn = new DataInputStream(dataIn);
        this.dataOut = new DataOutputStream(dataOut);
        this.statusOut = new DataOutputStream(statusOut);
    }

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            try {
                int cmd = dataIn.readInt();

                if (cmd == Command.DISCONNECT) {
                    logger.info("Got DISCONNECT signal. Aborting session.");
                    break;
                }

                int code = serve(cmd);
                statusOut.writeInt(code);
            } catch (EOFException e) {
                try {
                    dataOut.close();
                    statusOut.close();
                } catch (IOException e1) {
                    throw new IOError(e1); // cheese and rice =(
                }
                break;
            } catch (IOException e) {
                try {
                    statusOut.writeInt(ResponseCode.SERVER_ERROR);
                } catch (IOException e1) {
                    throw new IOError(e1); // cheese and rice =(
                }
            }
        }
        if (!isRunning) {
            logger.info("Session was stopped");
        }

    }

    public void stop() {
        isRunning = false;
    }

    /**
     * Dispatch command to it's proper evaluator
     */
    private int serve(int cmd) throws IOException {
        switch (cmd) {
            case Command.GET_FILE:
                logger.info("Got GET FILE command");
                return serveGetFile();
            case Command.LIST_FILES:
                logger.info("Got LIST FILES command");
                return serveListFiles();
            default:
                logger.info("Got UNKNOWN command");
                return serveUnknownCommand();
        }
    }

    private int serveGetFile() throws IOException {
        File file = new File(dataIn.readUTF());
        InputStream fileIn = null;
        try {
            fileIn = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException ignored) {
            return ResponseCode.NO_DATA;
        }

        long fileSize = FileUtils.sizeOf(file);
        dataOut.writeLong(fileSize);
        IOUtils.copy(fileIn, dataOut);

        return ResponseCode.OK;
    }

    private int serveListFiles() throws IOException {
        Path dir = Paths.get(dataIn.readUTF());
        List<Path> paths;
        try {
            paths = Files.list(dir).collect(Collectors.toList());
        } catch (FileNotFoundException | NotDirectoryException e) {
            return ResponseCode.NO_DATA;
        }

        logger.info("Writing [ " + paths.size() + " ] files...");
        dataOut.writeInt(paths.size());
        for (Path p : paths) {
            dataOut.writeUTF(p.toString());
            dataOut.writeBoolean(Files.isDirectory(p));
        }

        return ResponseCode.OK;
    }

    private int serveUnknownCommand() {
        return ResponseCode.COMMAND_UNKNOWN;
    }
}
