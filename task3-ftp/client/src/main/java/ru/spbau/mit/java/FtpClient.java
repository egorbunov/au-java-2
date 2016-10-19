package ru.spbau.mit.java;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class FtpClient {
    private Logger logger = Logger.getLogger("FtpClient");

    private final String host;
    private final int port;

    private DataInputStream statusIn;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    public FtpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() throws IOException {
        logger.info("Handshaking " + host + ":" + port + "...");
        Socket dataSocket = new Socket(host, port);
        logger.info("Connected!");

        DataInputStream in = new DataInputStream(dataSocket.getInputStream());
        long id = in.readLong();
        logger.info("Got handshake id = " + id);

        logger.info("Opening channel for statuses...");
        Socket statusSock = new Socket(host, port);
        DataOutputStream out = new DataOutputStream(statusSock.getOutputStream());
        out.writeLong(id);

        // dataSocket.setSoTimeout(1000);
        if (in.readBoolean()) {
            logger.info("Connection established!");

            dataIn = new DataInputStream(dataSocket.getInputStream());
            dataOut = new DataOutputStream(dataSocket.getOutputStream());
            statusIn = new DataInputStream(statusSock.getInputStream());
            return true;
        }

        logger.info("Fail!");
        return false;
    }

    public void disconnect() throws IOException {
        dataOut.writeInt(Command.DISCONNECT);
    }

    public List<FileInfo> executeList(String dir) throws IOException {
        dataOut.writeInt(Command.LIST_FILES);
        dataOut.writeUTF(dir);

        if (!checkStatus()) {
            return null;
        }

        int size = dataIn.readInt();
        List<FileInfo> files = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            files.add(new FileInfo(dataIn.readUTF(), dataIn.readBoolean()));
        }

        return files;
    }

    public FtpFile executeGet(String fileName) throws IOException {
        dataOut.writeInt(Command.GET_FILE);
        dataOut.writeUTF(fileName);

        if (!checkStatus()) {
            return null;
        }

        long size = dataIn.readLong();
        InputStream res = new BoundedInputStream(dataIn, size);

        return new FtpFile(size, res);
    }

    private boolean checkStatus() throws IOException {
        int status = statusIn.readInt();
        logger.info("Response status: " + status);

        if (status != ResponseCode.OK) {
            logger.severe("ERROR: server responded with status " + status);
            return false;
        }
        return true;
    }
}
