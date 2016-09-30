package ru.spbau.mit.java.wit.repository.storage;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
class WitStoragePaths {
    private WitStoragePaths() {}

    static Path getBlobsDir(Path witRoot) {
        return witRoot.resolve("objects");
    }

    static Path getCommitsDir(Path witRoot) {
        return witRoot.resolve("commits");
    }

    static Path getSnapshotsDir(Path witRoot) {
        return witRoot.resolve("snapshots");
    }

    static Path getIndexFilePath(Path witRoot) {
        return witRoot.resolve("index");
    }

    static Path getCurBranchNameFilePath(Path witRoot) {
        return witRoot.resolve("branch");
    }

    static Path getBranchInfoFilePath(Path witRoot, String branchName) {
        return getBranchesDirPath(witRoot).resolve(branchName);
    }

    static Path getBranchesDirPath(Path witRoot) {
        return witRoot.resolve("branches");
    }

    static Path getLogDir(Path witRoot) {
        return witRoot.resolve("log");
    }

    static Path getLogPath(Path witRoot, String branch) {
        return getLogDir(witRoot).resolve(branch);
    }
}
