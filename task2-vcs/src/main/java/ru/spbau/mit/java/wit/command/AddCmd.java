package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.storage.WitStorage;
import ru.spbau.mit.java.wit.storage.pack.IndexStore;
import ru.spbau.mit.java.wit.storage.WitPaths;
import ru.spbau.mit.java.wit.storage.WitRepo;
import ru.spbau.mit.java.wit.storage.io.StoreUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "add", description = "AddCmd files to be managed by vcs")
public class AddCmd implements Runnable {
    @Arguments(description = "Directories and file names to be added")
    public List<String> fileNames;

    @Override
    public void run() {
        Path baseDir = Paths.get(System.getProperty("user.dir"));
        // checking if repository is initialized
        Path witRoot = WitRepo.findRepositoryRoot(baseDir);
        if (witRoot == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        WitStorage storage = new WitStorage(witRoot);

        // checking if all files are existing
        List<Path> nonExisting = fileNames.stream().map(Paths::get).filter(Files::notExists)
                .collect(Collectors.toList());
        if (nonExisting.size() != 0) {
            nonExisting.forEach(p -> System.err.println(
                    "File: [ " + p.toString() + " ] does not exists")
            );
            return;
        }

        // collecting all files which user specified (walking dirs and stuff)
        Set<Path> filesForStage = new HashSet<>();
        for (String f : fileNames) {
            Path p = Paths.get(f).toAbsolutePath();
            if (Files.isDirectory(p)) {
                try {
                    Files.walk(p).forEach(filesForStage::add);
                } catch (IOException e) {
                    System.out.println("Error: Can't collect files for stage");
                    return;
                }
            } else if (Files.isRegularFile(p)) {
                filesForStage.add(p);
            }
        }

        // staging files; preparing new index
        Index index;
        try {
            index = storage.readIndex();
        } catch (IOException e) {
            System.err.println("Can't read index");
            return;
        }
        for (Path p : filesForStage) {
            String name = p.relativize(witRoot).toString();
            File file = p.toFile();
            long lastModified = file.lastModified();

            if (index.contains(name) && lastModified <= index.getEntryByFile(name).lastModified) {
                continue;
            }

            // trying to write file to storage
            ShaId id;
            try {
                id = storage.writeBlob(file);
            } catch (IOException e) {
                System.out.println("Error: Can't add file: " + file
                        + "[ " + e.getMessage() + " ]");
                e.printStackTrace();
                continue;
            }

            // changing index
            if (!index.contains(name)) {
                index.add(new Index.Entry(
                        id, lastModified, name, ShaId.EmptyId
                ));
            } else {
                Index.Entry entry = index.getEntryByFile(name);
                index.remove(entry);
                index.add(new Index.Entry(
                        id, lastModified, name, entry.lastCommitedBlobId
                ));
            }
        }

        // updating index on disk
        try {
            storage.writeIndex(index);
        } catch (IOException e) {
            System.out.println("Error: Can't write index "
                    + "[ " + e.getMessage() + " ]");
            e.printStackTrace();
        }
    }
}
