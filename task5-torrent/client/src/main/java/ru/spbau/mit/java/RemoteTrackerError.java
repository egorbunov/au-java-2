package ru.spbau.mit.java;

/**
 * IO error wrapper for remote tracker
 */
public class RemoteTrackerError extends RuntimeException {
    public RemoteTrackerError() {
        super();
    }

    public RemoteTrackerError(String message) {
        super(message);
    }

    public RemoteTrackerError(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteTrackerError(Throwable cause) {
        super(cause);
    }

    protected RemoteTrackerError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
