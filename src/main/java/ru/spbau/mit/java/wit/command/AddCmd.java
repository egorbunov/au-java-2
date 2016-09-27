package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.ShaId;
import ru.spbau.mit.java.wit.storage.FileStorage;
import ru.spbau.mit.java.wit.storage.IndexStorage;
import ru.spbau.mit.java.wit.storage.WitRepo;

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
        // checking if repository is initialized
        Path repoRoot = WitRepo.findRepositoryRoot();
        if (repoRoot == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        // checking ig all files are existing
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
                    e.printStackTrace();
                }
            } else if (Files.isRegularFile(p)) {
                filesForStage.add(p);
            }
        }

        // staging files; updating index
        Index index = IndexStorage.readIndex();
        for (Path p : filesForStage) {
            String name = p.relativize(repoRoot).toString();
            Index.Entry entry = index.getEntry(name);

            File file = p.toFile();

            if (entry == null) {
                ShaId id = stageFile(file);
                index.addEntry(new Index.Entry(id, file.lastModified(), name, ShaId.EmptyId));
            } else {
                long lastModified = file.lastModified();
                if (lastModified < entry.lastModified) {
                    ShaId id = stageFile(file);
                    index.removeEntry(entry);
                    index.addEntry(new Index.Entry(id, file.lastModified(), name,
                            entry.lastCommitedBlobId));
                }
            }
        }
    }

    private ShaId stageFile(File file) {
        InputStream is;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error: file not exists OMG"); // can't be true
        }

        ShaId id = FileStorage.write(is);

        try {
            is.close();
        } catch (IOException e) {
            System.err.println("Error: can't close file " + file.getName());
        }

        return id;
    }
}
