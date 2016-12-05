package ru.spbau.mit.java.shared.error;


public class UnknownRequestCode extends RuntimeException {
    private final byte code;

    public UnknownRequestCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}
