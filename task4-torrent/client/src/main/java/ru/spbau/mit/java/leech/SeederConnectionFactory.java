package ru.spbau.mit.java.leech;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Seeders connections creator. If client want to download
 * file from many clients he normally queries class implementing
 * this interface to create connections for him
 *
 * @param <T> type of client id
 */
public interface SeederConnectionFactory<T> {
    SeederConnection connectToClient(T clientId) throws IOException;
}
