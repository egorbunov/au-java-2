package ru.spbau.mit.java.shared.error;


public class UnknownRequestCode extends Exception {
    private final byte code;

    public UnknownRequestCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}
