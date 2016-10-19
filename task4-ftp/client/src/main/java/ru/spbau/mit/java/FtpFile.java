package ru.spbau.mit.java;

import java.io.InputStream;

/**
 * Created by: Egor Gorbunov
 * Date: 10/19/16
 * Email: egor-mailbox@ya.com
 */
public class FtpFile {
    private final long size;
    private final InputStream inputStream;

    public FtpFile(long size, InputStream inputStream) {
        this.size = size;
        this.inputStream = inputStream;
    }

    public long getSize() {
        return size;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
