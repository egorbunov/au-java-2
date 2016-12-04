package ru.spbau.mit.java.shared.response;

/**
 * Response to Update request
 */
public class UpdateResponse {
    private final boolean status;

    public UpdateResponse(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "{status: " + Boolean.toString(status) + "}";
    }
}
