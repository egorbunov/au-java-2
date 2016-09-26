package ru.spbau.mit.java.wit.storage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class WitUtils {
    public static Path getStorageRoot() {
        return new File("IMPLEMENT").toPath(); // TODO: Implement
    }

    public static Path getObjDir() {
        return Paths.get(getStorageRoot().toString(), "objects");
    }

    public static Path getIndexFilePath() {
        return Paths.get(getStorageRoot().toString(), "index");
    }

    public static Path getCurBranchNameFilePath() {
        return Paths.get(getStorageRoot().toString(), "branch");
    }

    public static Path getBranchInfoFilePath(String branchName) {
        return Paths.get(getStorageRoot().toString(), "branches", branchName);
    }
}
