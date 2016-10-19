package ru.spbau.mit.java;

/**
 * Class, which provides integer codes for every possible
 * request to out "FTP" server.
 * Every request must be prefixed with request code.
 * Client, while forming requests to server, must follow rules,
 * which are described near command identifiers below.
 */
public class Command {
    private Command() {}

    /**
     * Expected server response: list of files in a specified
     *                           directory
     *
     * Request scheme: <1: Int> <path: String>
     *
     * String must be encoded as UTF string, see {@link java.io.DataOutput}
     * {@code writeUTF} method
     *
     * Response scheme: <size: Int> (<name: String> <is_dir: Boolean>)*
     *
     * In case of no such directory, response code will be {@code NO_DATA}
     * (see {@link ResponseCode})
     */
    public static final int LIST_FILES = 1;

    /**
     * Expected server response: file, sent byte by byte with it's size
     *                           as response prefix
     *
     * Request scheme: <2: Int> <path: String>
     *
     * String must be encoded as UTF string, see {@link java.io.DataOutput}
     * {@code writeUTF} method
     *
     * Response scheme: <size: Long> <content: Bytes>
     *
     * In case of no such file, response code will be {@code NO_DATA}
     * (see {@link ResponseCode})
     */
    public static final int GET_FILE = 2;

    public static final int DISCONNECT = 3;
}
