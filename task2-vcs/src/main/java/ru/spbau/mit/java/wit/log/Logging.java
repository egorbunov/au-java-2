package ru.spbau.mit.java.wit.log;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class Logging {
    private static final FileHandler fh;
    private static final String logPath;

    static {
        try {
            File tempFile = File.createTempFile("wit_log", ".txt");
            logPath = tempFile.toPath().toString();
            fh = new FileHandler(logPath);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static String getLogPath() {
        return logPath;
    }

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.addHandler(fh);
        return logger;
    }
}
