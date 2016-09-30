package ru.spbau.mit.java.wit.repository.storage;

import java.io.IOException;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
@FunctionalInterface
public interface IOFunction<T, R> {
    R apply(T t) throws IOException;
}