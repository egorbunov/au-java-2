package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.command.except.FileIsProhibitedForControl;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.repository.WitStatusUtils;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Path witRoot = storage.getWitRoot();
        Index index = storage.readIndex();

        Set<Path> toReset = getFilesToReset(workingDir, userRepositoryPath, witRoot, index);

        // index is changed in next method call
        resetFiles(userRepositoryPath, index, toReset);

        storage.writeIndex(index);
        return 0;
    }

    private void resetFiles(Path userRepositoryPath, Index index, Set<Path> toReset) {
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
    }

    private Set<Path> getFilesToReset(Path workingDir, Path userRepositoryPath, Path witRoot, Index index) throws IOException {
        WitUtils.CollectedPaths collectedPaths
                = WitUtils.collectExistingFiles(fileNames, witRoot);

        if (collectedPaths.prohibitedPaths.size() != 0) {
            throw new FileIsProhibitedForControl(
                    collectedPaths.prohibitedPaths.iterator().next().toString());
        }

        Set<Path> toReset = collectedPaths.existingPaths;

        // also adding files, which are in index and deleted from tree,
        // if specified, because there may be staged files, which were deleted from tree
        Set<Path> nonExisting = collectedPaths.nonExistingPaths.stream()
                .map(it -> workingDir.resolve(it).normalize()).collect(Collectors.toSet());
        Set<Path> treeDeleted = WitStatusUtils.getTreeDeletedFiles(userRepositoryPath, index)
                .collect(Collectors.toSet());
        for (Path p : nonExisting) {
            if (treeDeleted.contains(p)) {
                toReset.add(p);
            } else {
                throw new FileNotFoundException(p.toString());
            }
        }

        return toReset;
    }
}
