package ru.spbau.mit.java.wit.storage;

import ru.spbau.mit.java.wit.model.ShaId;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public abstract class ObjStorage <T> {
    abstract ShaId write(T obj);
    abstract T read(ShaId id);
}
