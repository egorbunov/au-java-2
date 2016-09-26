package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.ShaId;

import java.io.InputStream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class FileStorage extends ObjStorage<InputStream> {
    @Override
    ShaId write(InputStream obj) {
        return null;
    }

    @Override
    InputStream read(ShaId id) {
        return null;
    }
}
