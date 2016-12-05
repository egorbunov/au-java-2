package ru.spbau.mit.java.leech;

import java.io.IOException;

/**
 * Seeders connections creator. If client want to start
 * file from many clients he normally queries class implementing
 * this interface to create connections for him
 *
 * @param <T> type of client id
 */
public interface SeederConnectionFactory<T> {
    SeederConnection connectToClient(T clientId) throws IOException;
}
