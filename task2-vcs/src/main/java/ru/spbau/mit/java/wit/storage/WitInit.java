package ru.spbau.mit.java.wit.storage;

import org.apache.commons.io.FileUtils;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.Log;
import ru.spbau.mit.java.wit.model.id.ShaId;
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
public class WitInit {
    private WitInit() {}

    /**
     * List of dirs and files, which designate wit repo storage folder
     */
    private static List<Path> witStorageDirsSignature = Arrays.asList(
            WitPaths.getBlobsDir(Paths.get("")),
            WitPaths.getCommitsDir(Paths.get("")),
            WitPaths.getSnapshotsDir(Paths.get("")),
            WitPaths.getBranchesDirPath(Paths.get("")),
            WitPaths.getLogDir(Paths.get(""))
    );
    private static List<Path> witStorageFilesSignature = Arrays.asList(
            WitPaths.getIndexFilePath(Paths.get("")),
            WitPaths.getCurBranchNameFilePath(Paths.get("")),
            WitPaths.getBranchInfoFilePath(Paths.get(""), "master")
    );
    // initial branch state
    private static Branch initialBranch = new Branch("master", ShaId.EmptyId, ShaId.EmptyId);
    private static Log initialBranchLog = new Log();
    private static Index initialIndex = new Index();

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
        WitStorage storage = new WitStorage(storageRoot);
        try {
            Files.createDirectory(storageRoot);
            for (Path p : witStorageDirsSignature) {
                Files.createDirectories(storageRoot.resolve(p));
            }
            for (Path p: witStorageFilesSignature) {
                Files.createFile(storageRoot.resolve(p));
            }

            storage.writeBranch(initialBranch);
            storage.writeCurBranchName(initialBranch.getName());
            storage.writeLog(initialBranchLog, initialBranch.getName());
            storage.writeIndex(initialIndex);
        } catch (WitStorage.StorageException | IOException e) {
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
     * from given directory
     *
     * @param baseDir working directory
     * @return path to vcs root dir if found, {@code null} otherwise
     */
    public static Path findRepositoryRoot(Path baseDir) {
        Path next = baseDir.toAbsolutePath();

        while (true) {
            baseDir = next;
            if (baseDir == null) {
                return null;
            }
            next = baseDir.getParent();

            Path witRoot = WitPaths.resolveStoragePath(baseDir);
            boolean isNotOk = Stream.concat(
                    witStorageDirsSignature.stream(),
                    witStorageFilesSignature.stream()
            ).map(witRoot::resolve).anyMatch(Files::notExists);

            if (isNotOk) {
                continue;
            }

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

        return WitPaths.resolveStoragePath(baseDir);
    }
}