package ru.spbau.mit.java.shared;


import java.io.IOError;

public class ServeIOError extends IOError {
    public ServeIOError(Throwable cause) {
        super(cause);
    }
}
