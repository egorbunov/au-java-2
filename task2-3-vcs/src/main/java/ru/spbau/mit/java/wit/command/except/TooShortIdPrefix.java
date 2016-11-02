package ru.spbau.mit.java.wit.command.except;

public class TooShortIdPrefix extends RuntimeException {
    private String prefix;

    public TooShortIdPrefix(String prefix) {

        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
