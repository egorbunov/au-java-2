package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
@Command(name = "status", description = "Print information about changes in working tree and staged files")
public class WitStatus implements WitCommand {
    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        Index index = storage.readIndex();
        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());

        String curBranch = storage.readCurBranchName();
        String mergeBranch = storage.readMergeFlag();

        List<Index.Entry> stagedDeleted = WitUtils.getStagedDeleted(index).collect(Collectors.toList());
        List<Index.Entry> stagedModified = WitUtils.getStagedModified(index).collect(Collectors.toList());
        List<Index.Entry> stagedNew = WitUtils.getStagedNew(index).collect(Collectors.toList());

        List<Path> notStagedDeleted = WitUtils.getTreeDeletedFiles(userRepositoryPath, index)
                .collect(Collectors.toList());
        List<Path> notStagedModified = WitUtils.getTreeModifiedFiles(userRepositoryPath, index)
                .collect(Collectors.toList());
        List<Path> notStagedNew = WitUtils.getTreeNewFiles(userRepositoryPath, index)
                .collect(Collectors.toList());

        if (stagedDeleted.size() == 0 && stagedModified.size() == 0 && stagedNew.size() == 0 &&
                notStagedDeleted.size() == 0 && notStagedModified.size() == 0 && notStagedNew.size() == 0) {
            System.out.println("Everything is up to date.");
            return 0;
        }

        System.out.println("On branch [ " + curBranch + " ]");
        if (mergeBranch != null) {
            System.out.println("MERGING with branch: " + mergeBranch);
        }

        // listing staged files if exist
        if (stagedDeleted.size() != 0 || stagedModified.size() != 0 || stagedNew.size() != 0) {
            System.out.println("Changes staged for commit: ");
            for (Index.Entry e : stagedDeleted) {
                Path p = workingDir.relativize(userRepositoryPath.resolve(e.fileName));
                System.out.println("    deleted:   " + p);
            }
            for (Index.Entry e : stagedModified) {
                Path p = workingDir.relativize(userRepositoryPath.resolve(e.fileName));
                System.out.println("    modified:  " + p);
            }
            for (Index.Entry e : stagedNew) {
                Path p = workingDir.relativize(userRepositoryPath.resolve(e.fileName));
                System.out.println("    new:       " + p);
            }
            System.out.println();
        }

        // listing not staged changed files if exist
        if (notStagedDeleted.size() != 0 || notStagedModified.size() != 0) {
            System.out.println("Not staged changes:");
            notStagedDeleted.forEach(p -> {
                System.out.println("    deleted:   " + workingDir.relativize(p));
            });
            notStagedModified.forEach(p -> {
                System.out.println("    modified:  " + workingDir.relativize(p));
            });
            System.out.println();
        }

        // listing new files in tree if exist
        if (notStagedNew.size() != 0) {
            System.out.println("Not tracked files:");
            notStagedNew.forEach(p -> {
                System.out.println("               " + workingDir.relativize(p));
            });
        }

        return 0;
    }
}
