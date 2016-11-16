package ru.spbau.mit.java.leech;

import ru.spbau.mit.java.leech.SeederConnection;

/**
 * Seeders connections creator. If client want to download
 * file from many clients he normally queries class implementing
 * this interface to create connections for him
 *
 * @param <T> type of client id
 */
public interface SeederConnectionFactory<T> {
    SeederConnection getSeederConnection(T clientId);
}
