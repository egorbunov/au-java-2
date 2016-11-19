package ru.spbau.mit.java.shared.error;


public class SessionStartError extends RuntimeException {
    public SessionStartError() {
        super();
    }

    public SessionStartError(String message) {
        super(message);
    }

    public SessionStartError(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionStartError(Throwable cause) {
        super(cause);
    }

    protected SessionStartError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
