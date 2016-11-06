package ru.spbau.mit.protocol.response;

/**
 * Response to Update request
 */
public class UpdateResponse {
    private final boolean status;

    public UpdateResponse(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
}
