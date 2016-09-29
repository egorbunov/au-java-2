package ru.spbau.mit.java.wit.storage;

import org.apache.commons.io.FileUtils;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.ShaId;
import ru.spbau.mit.java.wit.storage.io.StoreUtils;
import ru.spbau.mit.java.wit.storage.pack.BranchStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/27/16
 * Email: egor-mailbox@ya.com
 */
public class WitRepo {
    private WitRepo() {}

    /**
     * List of dirs and files, which designate wit repo storage folder
     */
    private static List<Path> witStorageDirsSignature = Arrays.asList(
            WitPaths.getBlobsDir(Paths.get("")),
            WitPaths.getCommitsDir(Paths.get("")),
            WitPaths.getSnapshotsDir(Paths.get("")),
            WitPaths.getBranchesDirPath(Paths.get(""))
    );
    private static List<Path> witStorageFilesSignature = Arrays.asList(
            WitPaths.getIndexFilePath(Paths.get("")),
            WitPaths.getCurBranchNameFilePath(Paths.get("")),
            WitPaths.getBranchInfoFilePath(Paths.get(""), "master")
    );
    // initial branch state
    private static Branch initialBranch = new Branch("master", ShaId.EmptyId, ShaId.EmptyId);

    /**
     * Tries to initialize new repository; If no wit storage
     * directory above working dir found, when repository initialized
     * under current working dir; If appropriate wit storage dir
     * already exists in working dir or above, when nothing is done
     *
     * @return path to found or initialized repository storage root
     *         {@code null} is returned in case error is occurred
     */
    public static Path init(Path baseDir) {
        Path storageRoot = findRepositoryRoot(baseDir);
        if (storageRoot != null) {
            return storageRoot;
        }

        storageRoot = WitPaths.resolveStoragePath(baseDir);

        try {
            Files.createDirectory(storageRoot);
            for (Path p : witStorageDirsSignature) {
                Files.createDirectories(storageRoot.resolve(p));
            }
            for (Path p: witStorageFilesSignature) {
                Files.createFile(storageRoot.resolve(p));
            }

            // writing initial branch data
            StoreUtils.write(initialBranch,
                    WitPaths.getBranchInfoFilePath(storageRoot, initialBranch.getName()),
                    BranchStore::pack);

            // writing current branch name
            StoreUtils.write(initialBranch.getName(),
                    WitPaths.getCurBranchNameFilePath(storageRoot),
                    StoreUtils::stringPack);

        } catch (IOException e) {
            try {
                FileUtils.deleteDirectory(storageRoot.toFile());
            } catch (IOException e1) {
                throw new RuntimeException(
                        "Cheese and rice! Can't remove wit root after init error!", e1
                );
            }
            return null;
        }
        return storageRoot;
    }

    /**
     * Tries to find root repository data directory starting
     * from current working directory
     * @return path to vcs root dir if found, {@code null} otherwise
     */
    public static Path findRepositoryRoot(Path baseDir) {
        baseDir = baseDir.toAbsolutePath();
        Path candidate;

        while (true) {
            candidate = WitPaths.lookupStoragePath(baseDir);
            baseDir = candidate;
            if (candidate == null) {
                return null;
            }
            boolean isOk = Stream.concat(
                    witStorageDirsSignature.stream(),
                    witStorageFilesSignature.stream()
            ).map(baseDir::resolve).anyMatch(Files::notExists);

            if (!isOk) {
                continue;
            }

            Path witRoot = WitPaths.resolveStoragePath(candidate);
            Branch master;
            try {
                master = StoreUtils.read(
                        WitPaths.getBranchInfoFilePath(witRoot, initialBranch.getName()),
                        BranchStore::unpack);
            } catch (IOException e) {
                continue;
            }

            if (!master.getName().equals(initialBranch.getName()) ||
                    master.getCurCommitId() == null ||
                    master.getHeadCommitId() == null) {
                continue;
            }

            break;
        }
        return candidate;
    }
}
