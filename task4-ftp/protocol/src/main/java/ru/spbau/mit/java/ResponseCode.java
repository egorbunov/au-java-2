package ru.spbau.mit.java;

public class ResponseCode {
    private ResponseCode() {}

    public static final int OK = 0;
    public static final int NO_DATA = -1;
    public static final int SERVER_ERROR = -2;
    public static final int COMMAND_UNKNOWN = -3;
}
