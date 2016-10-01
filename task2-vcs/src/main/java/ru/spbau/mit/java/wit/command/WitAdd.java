package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.log.Logging;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "add", description = "WitAdd files to be managed by vcs")
public class WitAdd implements WitCommand {
    @Arguments(description = "Directories and file names to be added")
    private List<String> fileNames;

    private final Logger logger = Logging.getLogger(WitAdd.class.getName());

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        // checking if all files are existing
        List<Path> nonExisting = fileNames.stream().map(Paths::get).filter(Files::notExists)
                .collect(Collectors.toList());
        if (nonExisting.size() != 0) {
            nonExisting.forEach(p -> System.err.println(
                    "File: [ " + p.toString() + " ] does not exists")
            );
            return -1;
        }

        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());

        // collecting all files which user specified (walking dirs and stuff)
        Set<Path> filesForStage = new HashSet<>();
        for (String f : fileNames) {
            Path p = Paths.get(f).normalize().toAbsolutePath();
            if (p.startsWith(storage.getWitRoot())) {
                logger.info("Omitting files under repo storage dir: " + p);
                continue;
            }
            WitUtils.walk(p, storage.getWitRoot())
                    .filter(Files::isRegularFile)
                    .forEach(fp -> {
                        assert !fp.startsWith(storage.getWitRoot());
                        if (!fp.startsWith(userRepositoryPath)) {
                            System.out.println("File " + p + " is outside repository; " +
                                    "Omitting");
                        } else {
                            filesForStage.add(fp);
                        }
                    });
        }

        // staging files; preparing new index
        Index index;
        index = storage.readIndex();

        for (Path p : filesForStage) {
            String name = p.subpath(userRepositoryPath.getNameCount(), p.getNameCount()).toString();

            File file = p.toFile();
            long lastModified = file.lastModified();
            logger.info(name + " | last modified = " + lastModified);

            if (index.contains(name) && lastModified == index.getEntryByFile(name).modified) {
                if (Index.isCommitedAndNotChanged(index.getEntryByFile(name))) {
                    System.out.println("File is already up to date: [ " + name + " ]");
                } else {
                    System.out.println("File is already staged: [ " + name + " ]");
                }
                continue;
            }

            // trying to write file to storage
            ShaId id;
            id = storage.writeBlob(file);

            // changing index
            if (!index.contains(name)) {
                logger.info("Adding new entry to index; " +
                        "(" + name + ", " + id.toString() + ", " + lastModified + ")");

                index.add(new Index.Entry(
                        name, lastModified, id, ShaId.EmptyId
                ));
            } else {
                logger.info("Updating index entry, because file changed");

                Index.Entry entry = index.getEntryByFile(name);
                index.remove(entry);
                index.add(new Index.Entry(
                        name, lastModified, id, entry.lastCommittedBlobId
                ));
            }
        }

        // updating index on disk
        storage.writeIndex(index);

        return 0;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}
