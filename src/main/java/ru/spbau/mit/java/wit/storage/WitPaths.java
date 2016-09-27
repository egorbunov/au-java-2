package ru.spbau.mit.java.wit.storage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
class WitPaths {
    private WitPaths() {}

    /**
     * Returns proper wit storage root basing to given directory
     * @param baseDir base directory for version control
     * @return wit vcs storage root dir
     */
    public static Path resolveStorageRoot(Path baseDir) {
        return baseDir.resolve(".wit");
    }

    /**
     * Find wit storage root directory as parent of given base directory
     * @return null if no such directory found or path to that dir
     */
    public static Path findStorageRoot(Path baseDir) {
        return null; // FIXME
    }

    public static Path getBlobsDir(Path witRoot) {
        return witRoot.resolve("objects");
    }

    public static Path getCommitsDir(Path witRoot) {
        return witRoot.resolve("commits");
    }

    public static Path getSnaphotsDir(Path witRoot) {
        return witRoot.resolve("snapshots");
    }

    public static Path getIndexFilePath(Path witRoot) {
        return witRoot.resolve("index");
    }

    public static Path getCurBranchNameFilePath(Path witRoot) {
        return witRoot.resolve("branch");
    }

    public static Path getBranchInfoFilePath(Path witRoot, String branchName) {
        return witRoot.resolve("branches").resolve(branchName);
    }
}
