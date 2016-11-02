package ru.spbau.mit.java.wit.command.except;

public class CommitNotFound extends RuntimeException {
    private String id;

    public CommitNotFound(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
