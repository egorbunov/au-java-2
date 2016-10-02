package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
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
        Path witStorageRoot = storage.getWitRoot();

        // checking if all fileNames are existing and in repo
        List<String> toReset = new ArrayList<>();
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
            toReset.add(p.toAbsolutePath().subpath(userRepositoryPath.getNameCount(),
                    p.getNameCount()).toString());
        }

        Index index = storage.readIndex();

        for (String file : toReset) {
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
