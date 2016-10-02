package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

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

        HashSet<Path> toRemove = new HashSet<>();
        WitUtils.collectFiles(fileNames, storage.getWitRoot(), toRemove);

        Index index = storage.readIndex();

        for (Path p : toRemove) {
            String file = p.subpath(userRepositoryPath.getNameCount(), p.getNameCount()).toString();
            Index.Entry e = index.getEntryByFile(file);
            if (e == null) {
                System.out.println("File: " + file + " is not tracked. Omitting...");
            } else if (e.isStaged()) {
                System.out.println("File: " + file + " is staged for commit! " +
                        "Use reset to unstage it! Omitting...");
            } else {
                Files.delete(userRepositoryPath.resolve(file));
                index.remove(e);
                index.add(new Index.Entry(e.fileName, e.modified, ShaId.EmptyId, e.lastCommittedBlobId));
            }
        }

        storage.writeIndex(index);
        return 0;
    }
}
