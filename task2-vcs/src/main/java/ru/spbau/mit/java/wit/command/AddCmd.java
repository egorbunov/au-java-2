package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.WitCommand;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.storage.WitStorage;
import ru.spbau.mit.java.wit.storage.WitInit;

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

@Command(name = "add", description = "AddCmd files to be managed by vcs")
public class AddCmd implements WitCommand {
    @Arguments(description = "Directories and file names to be added")
    public List<String> fileNames;

    Logger logger = Logger.getLogger(AddCmd.class.getName());

    @Override
    public int run(Path baseDir, WitStorage storage) {
        // checking if all files are existing
        List<Path> nonExisting = fileNames.stream().map(Paths::get).filter(Files::notExists)
                .collect(Collectors.toList());
        if (nonExisting.size() != 0) {
            nonExisting.forEach(p -> System.err.println(
                    "File: [ " + p.toString() + " ] does not exists")
            );
            return -1;
        }

        // collecting all files which user specified (walking dirs and stuff)
        Set<Path> filesForStage = new HashSet<>();
        for (String f : fileNames) {
            Path p = Paths.get(f).toAbsolutePath();
            if (p.startsWith(storage.getWitRoot())) {
                logger.info("Omitting files under repo storage dir: " + p);
                continue;
            }
            try {
                Files.walk(p)
                        .filter(it -> !it.startsWith(storage.getWitRoot()))
                        .filter(Files::isRegularFile)
                        .forEach(filesForStage::add);
            } catch (IOException e) {
                System.out.println("Error: Can't collect files for stage");
                return -1;
            }
        }

        // staging files; preparing new index
        Index index;
        index = storage.readIndex();
        for (Path p : filesForStage) {
            if (!p.startsWith(baseDir)) {
                System.out.println("File " + p + " is outside repository; " +
                        "Omitting");
                continue;
            }
            String name = p.subpath(baseDir.getNameCount(), p.getNameCount()).toString();

            File file = p.toFile();
            long lastModified = file.lastModified();
            logger.info(name + " | last modified = " + lastModified);

            if (index.contains(name) && lastModified == index.getEntryByFile(name).lastModified) {
                System.out.println("File is already staged: [ " + name + " ]");
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
                        id, lastModified, name, ShaId.EmptyId
                ));
            } else {
                logger.info("Updating index entry, because file changed");
                Index.Entry entry = index.getEntryByFile(name);
                index.remove(entry);
                index.add(new Index.Entry(
                        id, lastModified, name, entry.lastCommitedBlobId
                ));
            }
        }

        // updating index on disk
        storage.writeIndex(index);

        return 0;
    }
}
