package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.ShaId;

import java.io.File;
import java.io.InputStream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class FileStorage {
    private FileStorage() {}

    public static ShaId write(InputStream obj) {
        return null;
    }

    public static InputStream read(ShaId id) {
        return null;
    }

    public static File getBlobFile(ShaId id) {
        return null;
    }
}
