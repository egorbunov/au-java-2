package ru.spbau.mit.java.wit.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class WitPaths {
    private WitPaths() {}

    /**
     * Returns proper wit storage root basing to given directory
     * @param baseDir base directory for version control
     * @return wit vcs storage root dir
     */
    public static Path resolveStoragePath(Path baseDir) {
        return baseDir.resolve(".wit");
    }

    /**
     * Find wit storage root directory as parent of given base directory
     * @return null if no such directory found or path to that dir
     */
    public static Path lookupStoragePath(Path baseDir) {
        Path dir = baseDir;
        while (dir != null && !dir.getFileName().equals(Paths.get(".wit"))) {
            dir = dir.getParent();
        }
        return dir;
    }

    public static Path getBlobsDir(Path witRoot) {
        return witRoot.resolve("objects");
    }

    public static Path getCommitsDir(Path witRoot) {
        return witRoot.resolve("commits");
    }

    public static Path getSnapshotsDir(Path witRoot) {
        return witRoot.resolve("snapshots");
    }

    public static Path getIndexFilePath(Path witRoot) {
        return witRoot.resolve("index");
    }

    public static Path getCurBranchNameFilePath(Path witRoot) {
        return witRoot.resolve("branch");
    }

    public static Path getBranchInfoFilePath(Path witRoot, String branchName) {
        return getBranchesDirPath(witRoot).resolve(branchName);
    }

    public static Path getBranchesDirPath(Path witRoot) {
        return witRoot.resolve("branches");
    }

    public static Path getLogPath(Path witRoot, String branch) {
        return witRoot.resolve("log").resolve(branch);
    }
}
