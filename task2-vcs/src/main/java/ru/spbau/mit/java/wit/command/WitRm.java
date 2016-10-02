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
import java.nio.file.Paths;
import java.util.ArrayList;
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
        Path witStorageRoot = storage.getWitRoot();

        // checking if all fileNames are existing and in repo
        List<String> toRemove = new ArrayList<>();
        for (String s : fileNames) {
            Path p = Paths.get(s);
            if (Files.notExists(p)) {
                System.err.println("Error: file [ " + p.toString() + " ] does not exists");
                return -1;
            }
            p = p.toAbsolutePath();
            if (WitUtils.isWitServicePath(p, witStorageRoot)) {
                System.err.println("Error: file [ " + p.toString() + " ] outside repository");
                return -1;
            }
            toRemove.add(p.toAbsolutePath().subpath(userRepositoryPath.getNameCount(),
                    p.getNameCount()).toString());
        }

        Index index = storage.readIndex();

        for (String file : toRemove) {
            Index.Entry e = index.getEntryByFile(file);
            if (e == null) {
                System.out.println("File: " + file + " is not tracked. Omitting...");
            } else if (e.isStaged()) {
                System.out.println("File: " + file + " is staged for commit! " +
                        "Use reset to unstage it! Omitting...");
            } else {
                index.remove(e);
                index.add(new Index.Entry(e.fileName, e.modified, ShaId.EmptyId, e.lastCommittedBlobId));
            }
        }

        storage.writeIndex(index);
        return 0;
    }
}
