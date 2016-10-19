package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.repository.WitStatusUtils;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
@Command(name = "reset", description = "Remove fileNames from stage (undo add command)")
public class WitReset implements WitCommand {
    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    @Arguments
    private List<String> fileNames;

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());

        WitUtils.CollectedPaths collectedPaths
                = WitUtils.collectExistingFiles(fileNames, storage.getWitRoot());
        if (collectedPaths.prohibitedPaths.size() != 0) {
            System.out.println("Error: file "
                    + collectedPaths.prohibitedPaths.iterator().next() +
                    " is prohibited! (service or outside repo)");
            return -1;
        }
        Set<Path> toReset = collectedPaths.existingPaths;

        Index index = storage.readIndex();
        // also adding files, which are in index and deleted from tree,
        // if specified, because there may be committed files, which were deleted from tree
        Set<Path> nonExisting = collectedPaths.nonExistingPaths.stream()
                .map(it -> workingDir.resolve(it).normalize()).collect(Collectors.toSet());
        Set<Path> treeDeleted = WitStatusUtils.getTreeDeletedFiles(userRepositoryPath, index)
                .collect(Collectors.toSet());
        for (Path p : nonExisting) {
            if (treeDeleted.contains(p)) {
                toReset.add(p);
            } else {
                System.err.println("Error: file " + p + " not exists!");
                return -1;
            }
        }


        for (Path p : toReset) {
            String file = p.subpath(userRepositoryPath.getNameCount(), p.getNameCount()).toString();
            Index.Entry e = index.getEntryByFile(file);
            if (e == null) {
                System.out.println("File: " + file + " is not in index, omitting...");
            } else if (e.isStaged()) {
                // we have something to reset
                index.remove(e);
                Index.Entry newEntry = new Index.Entry(
                        e.fileName, e.modified, e.lastCommittedBlobId, e.lastCommittedBlobId
                );
                if (!newEntry.isInvalid()) {
                    index.add(e);
                }
            } else {
                System.out.println("File: " + file + " is not staged, omitting...");
            }
        }


        storage.writeIndex(index);
        return 0;
    }
}
