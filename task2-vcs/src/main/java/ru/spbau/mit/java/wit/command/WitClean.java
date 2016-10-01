package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
@Command(name = "clean", description = "Remove all not tracked files from working repository tree")
public class WitClean implements WitCommand {
    @Option(name = "-d", description = "Also delete untracked directories")
    boolean deleteDirs;

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        Index index = storage.readIndex();
        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());

        List<Path> untrackedFiles = WitUtils.getTreeNewPaths(userRepositoryPath, index)
                .collect(Collectors.toList());

        for (Path p : untrackedFiles) {
            Files.delete(p);
        }

        if (deleteDirs) {
            List<Path> dirs = WitUtils.walk(userRepositoryPath, storage.getWitRoot())
                    .filter(Files::isDirectory)
                    .sorted((p1, p2) -> Integer.compare(p2.getNameCount(), p1.getNameCount()))
                    .collect(Collectors.toList());
            for (Path p : dirs) {
                try {
                    Files.delete(p);
                } catch (DirectoryNotEmptyException ignored) {}
            }
        }

        return 0;
    }
}
