package ru.spbau.mit.java.wit.command.except;

public class WitRepoAlreadyExists extends RuntimeException {
    private String path;

    public WitRepoAlreadyExists(String path) {

        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
