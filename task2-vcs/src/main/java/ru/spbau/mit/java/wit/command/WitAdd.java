package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "add", description = "WitAdd files to be managed by vcs")
public class WitAdd implements WitCommand {
    @Arguments(description = "Directories and file names to be added")
    private List<String> fileNames;

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        Path witRoot = storage.getWitRoot();
        Set<Path> filesForStage = resolveFilesToAdd(witRoot);
        if (filesForStage == null) {
            return -1;
        }

        Index index = storage.readIndex();
        Path userRepositoryPath = WitUtils.stripWitStoragePath(witRoot);
        for (Path p : filesForStage) {
            String name = p.subpath(userRepositoryPath.getNameCount(), p.getNameCount()).toString();
            File file = p.toFile();
            long lastModified = file.lastModified();

            if (index.contains(name) && lastModified == index.getEntryByFile(name).modified) {
                if (!index.getEntryByFile(name).isStaged()) {
                    System.out.println("File is already up to date: [ " + name + " ]");
                } else {
                    System.out.println("File is already staged: [ " + name + " ]");
                }
                continue;
            }

            ShaId id = storage.writeBlob(file);
            index.addUpdate(name, lastModified, id);
        }

        storage.writeIndex(index);
        return 0;
    }

    public Set<Path> resolveFilesToAdd(Path witRoot) throws IOException {
        WitUtils.CollectedPaths collectedPaths
                = WitUtils.collectExistingFiles(fileNames, witRoot);
        if (collectedPaths.prohibitedPaths.size() != 0) {
            System.out.println("Error: file "
                    + collectedPaths.prohibitedPaths.iterator().next() +
                    " is prohibited! (service or outside repo)");
            return null;
        }
        if (collectedPaths.nonExistingPaths.size() != 0) {
            System.out.println("Error: file "
                    + collectedPaths.nonExistingPaths.iterator().next() +
                    " not exists!");
            return null;
        }
        return collectedPaths.existingPaths;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}
