package ru.spbau.mit.java.shared.error;


public class ServerShutdownError extends RuntimeException {
    public ServerShutdownError() {
        super();
    }

    public ServerShutdownError(String message) {
        super(message);
    }

    public ServerShutdownError(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerShutdownError(Throwable cause) {
        super(cause);
    }
}
