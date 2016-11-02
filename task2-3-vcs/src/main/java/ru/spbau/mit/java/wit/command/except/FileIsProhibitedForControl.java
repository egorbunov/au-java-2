package ru.spbau.mit.java.wit.command.except;

public class FileIsProhibitedForControl extends RuntimeException {
    private String filename;

    public FileIsProhibitedForControl(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
