package ru.spbau.mit.java.wit.repository;

import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
public class WitStatusUtils {
    /**
     * Returns files, which are prepared to commit (meaning that there is
     * some payload to commit), but not committed yet
     */
    public static Stream<Index.Entry> getStagedEntries(Index index) {
        return Stream.of(
                getStagedDeleted(index),
                getStagedModified(index),
                getStagedNew(index)
        ).flatMap(Function.identity());
    }

    /**
     * Returns staged modified files  (newly added staged files not included)
     */
    public static Stream<Index.Entry> getStagedModified(Index index) {
        return index.stream()
                .filter(e -> !e.curBlobId.equals(e.lastCommittedBlobId)
                        && !e.lastCommittedBlobId.equals(ShaId.EmptyId));
    }

    /**
     * Returns files, which are staged and going to be deleted on next commit
     */
    public static Stream<Index.Entry> getStagedDeleted(Index index) {
        return index.stream().filter(Index::isStagedForDelete);
    }

    /**
     * Returns files, which are newly created and staged
     */
    public static Stream<Index.Entry> getStagedNew(Index index) {
        return index.stream()
                .filter(e -> !e.curBlobId.equals(ShaId.EmptyId)
                        && e.lastCommittedBlobId.equals(ShaId.EmptyId));
    }

    /**
     * returns all changes in the working tree (new files, modified files, deleted files)
     * accordingly to repo index
     */
    public static Stream<Path> getTreeChangedFiles(Path userRepoRoot, Index index) throws IOException {
        return Stream.of(
                getTreeDeletedFiles(userRepoRoot, index),
                getTreeModifiedFiles(userRepoRoot, index),
                getTreeNewPaths(userRepoRoot, index)
        ).flatMap(Function.identity());
    }

    /**
     * Returns files, which are deleted from repository tree, but still
     * in index
     *
     * @param userRepoRoot base user repository dir
     * @return list of ABSOLUTE paths
     */
    public static Stream<Path> getTreeDeletedFiles(Path userRepoRoot, Index index) {
        return index.stream()
                .map(e -> userRepoRoot.resolve(e.fileName))
                .filter(Files::notExists);
    }

    /**
     * Return files, which are modified in working tree, but this modifications
     * are not staged for commit
     *
     * @param userRepoRoot base repository dir
     */
    public static Stream<Path> getTreeModifiedFiles(Path userRepoRoot, Index index) {
        List<Path> files = new ArrayList<>();
        for (Index.Entry e : index) {
            Path p = userRepoRoot.resolve(e.fileName);
            if (Files.notExists(p)) {
                continue;
            }
            if (p.toFile().lastModified() > e.modified) {
                files.add(p);
            }
        }
        return files.stream();
    }

    /**
     * Return regular files paths, which are newly added to working tree and not staged
     *
     * @param userRepoRoot base repository dir
     */
    public static Stream<Path> getTreeNewPaths(Path userRepoRoot, Index index) throws IOException {
        Set<Path> treeFiles = WitUtils.walk(userRepoRoot, WitUtils.resolveStoragePath(userRepoRoot))
                .filter(p -> Files.isRegularFile(p))
                .collect(Collectors.toSet());
        Set<Path> indexedFiles = index.stream().map(it -> userRepoRoot.resolve(it.fileName))
                .collect(Collectors.toSet());

        treeFiles.removeAll(indexedFiles);

        return treeFiles.stream();
    }
}
