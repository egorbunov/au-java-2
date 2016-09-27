package ru.spbau.mit.java.wit.storage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/27/16
 * Email: egor-mailbox@ya.com
 */
public class WitRepo {
    private WitRepo() {}

    /**
     * Tries to initialize new repository; If no wit storage
     * directory above working dir found, when repository initialized
     * under current working dir; If appropriate wit storage dir
     * already exists in working dir or above, when nothing is done
     *
     * @return path to found or initialized repository storage root
     */
    public static Path init() {
        return null; // TODO: fixme
    }

    /**
     * Tries to find root repository data directory starting
     * from current working directory
     * @return path to vcs root dir if found, {@code null} otherwise
     */
    public static Path findStorageRoot() {
        return null; // TODO: fixme
    }

    /**
     * @return root directory of repository
     */
    public static Path findRepositoryRoot() {
        if (findRepositoryRoot() == null) {
            return null;
        }
        return findStorageRoot().getParent();
    }
}
