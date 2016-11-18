package ru.spbau.mit.java.shared.error;


import java.io.IOError;

public class ServeIOError extends IOError {
    public ServeIOError(Throwable cause) {
        super(cause);
    }
}
