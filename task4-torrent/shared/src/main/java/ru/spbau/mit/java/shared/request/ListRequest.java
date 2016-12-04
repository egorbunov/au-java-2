package ru.spbau.mit.java.shared.request;

/**
 * Request for list of files seen by tracker as available for download
 */
public class ListRequest {
    public static final byte code = 1;

    @Override
    public String toString() {
        return "list {}";
    }
}
