package ru.spbau.mit.java.shared.error;


public class ServerStartupError extends RuntimeException {
    public ServerStartupError(String message) {
        super(message);
    }

    public ServerStartupError() {
        super();
    }

    public ServerStartupError(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerStartupError(Throwable cause) {
        super(cause);
    }
}
