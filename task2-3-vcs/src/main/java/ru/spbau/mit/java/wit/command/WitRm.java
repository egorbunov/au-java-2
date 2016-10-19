package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitStatusUtils;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
@Command(name = "rm", description = "Delete file from repository and from working tree")
public class WitRm implements WitCommand {
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
        Set<Path> toRemove = collectedPaths.existingPaths;

        Index index = storage.readIndex();

        // also adding files, which are in index and deleted from tree,
        // if specified, because there may be committed files, which were deleted from tree
        Set<Path> nonExisting = collectedPaths.nonExistingPaths.stream()
                .map(it -> workingDir.resolve(it).normalize()).collect(Collectors.toSet());
        Set<Path> treeDeleted = WitStatusUtils.getTreeDeletedFiles(userRepositoryPath, index)
                .collect(Collectors.toSet());
        for (Path p : nonExisting) {
            if (treeDeleted.contains(p)) {
                toRemove.add(p);
            } else {
                System.err.println("Error: file " + p + " not exists!");
            }
        }

        for (Path p : toRemove) {
            String file = p.subpath(userRepositoryPath.getNameCount(), p.getNameCount()).toString();
            Index.Entry e = index.getEntryByFile(file);
            if (e == null) {
                System.out.println("File: " + file + " is not tracked. Omitting...");
            } else if (e.isStaged()) {
                System.out.println("File: " + file + " is staged for commit! " +
                        "Use reset to unstage it! Omitting...");
            } else {
                Files.deleteIfExists(userRepositoryPath.resolve(file));
                index.remove(e);
                index.add(new Index.Entry(e.fileName, e.modified, ShaId.EmptyId, e.lastCommittedBlobId));
            }
        }

        storage.writeIndex(index);
        return 0;
    }
}
