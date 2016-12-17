package ru.spbau.mit.java.shared.error;


import java.io.IOError;

/**
 * Error, thrown in case server failed to read request or write response...
 */
public class ServerIOError extends IOError {
    public ServerIOError(Throwable cause) {
        super(cause);
    }
}
