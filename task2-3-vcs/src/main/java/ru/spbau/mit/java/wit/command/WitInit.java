package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.java.wit.command.except.WitRepoAlreadyExists;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "init", description = "initializes empty wit repository")
public class WitInit implements WitCommand {
    // initial branch state
    private static final Branch initialBranch = new Branch("master", ShaId.EmptyId);
    private static final List<ShaId> initialBranchLog = new ArrayList<>();
    private static final Index initialIndex = new Index();

    /**
     * Tries to initialize new repository; If no wit storage
     * directory above working dir found, when repository initialized
     * under current working dir; If appropriate wit storage dir
     * already exists in working dir or above, when nothing is done
     */
    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        // checking if repository is already initialized
        Path repoRoot = findRepositoryRoot(workingDir);
        if (repoRoot != null) {
            throw new WitRepoAlreadyExists(repoRoot.toString());
        }

        // initializing new repository
        Path storageRoot = WitUtils.resolveStoragePath(workingDir);
        try {
            WitStorage.createStorageStructure(storageRoot);
            storage = new WitStorage(storageRoot);
            storage.writeBranch(initialBranch);
            storage.writeCurBranchName(initialBranch.getName());
            storage.writeCommitLog(initialBranchLog, initialBranch.getName());
            storage.writeIndex(initialIndex);
        } catch (IOException e) {
            try {
                FileUtils.deleteDirectory(storageRoot.toFile());
            } catch (IOException e1) {
                throw new RuntimeException(
                        "Cheese and rice! Can't remove wit root after init error!", e1
                );
            }
            return -1;
        }

        System.out.println("Initialized wit repository at: " + storageRoot);
        return 0;
    }

    /**
     * Tries to find root repository data directory starting
     * from given directory
     *
     * @param baseDir working directory
     * @return path to vcs root dir if found, {@code null} otherwise
     */
    public static Path findRepositoryRoot(Path baseDir) throws IOException {
        Path next = baseDir.toAbsolutePath();

        while (true) {
            baseDir = next;
            if (baseDir == null) {
                return null;
            }
            next = baseDir.getParent();

            Path witRoot = WitUtils.resolveStoragePath(baseDir);
            if (!WitStorage.isValidStorageStructure(witRoot)) {
                continue;
            }
            WitStorage storage = new WitStorage(witRoot);
            Branch master = storage.readBranch(initialBranch.getName());
            if (master == null || !master.getName().equals(initialBranch.getName())
                    || master.getHeadCommitId() == null) {
                continue;
            }
            break;
        }

        return WitUtils.resolveStoragePath(baseDir);
    }


}
