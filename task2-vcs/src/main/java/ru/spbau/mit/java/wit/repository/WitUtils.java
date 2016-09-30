package ru.spbau.mit.java.wit.repository;

import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */
public class WitUtils {
    /**
     * We can't store sha id in commit file because it's name
     * based on commit contents.
     */
    public static class IdCommit {
        public final ShaId id;
        public final Commit commit;

        public IdCommit(ShaId id, Commit commit) {
            this.id = id;
            this.commit = commit;
        }
    }

    /**
     * Returns list of commits, sorted accordingly to their time stamp
     * Commits are retrieved by traversing parent commits from head commit,
     * specified by {@code headCommitId}.
     */
    public static Stream<IdCommit> getCommitHistory(ShaId headCommitId, WitStorage storage) {
        return collectCommitHistoryStream(headCommitId, storage)
                .distinct()
                .sorted(Comparator.comparing(c -> c.commit.getTimestamp()));
    }

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
                .filter(e -> !e.curBlobId.equals(e.lastCommitedBlobId)
                        && !e.lastCommitedBlobId.equals(ShaId.EmptyId));
    }

    /**
     * Returns files, which are staged and going to be deleted on next commit
     */
    public static Stream<Index.Entry> getStagedDeleted(Index index) {
        return index.stream()
                .filter(e -> e.curBlobId.equals(ShaId.EmptyId));
    }

    /**
     * Returns files, which are newly created and staged
     */
    public static Stream<Index.Entry> getStagedNew(Index index) {
        return index.stream()
                .filter(e -> !e.curBlobId.equals(ShaId.EmptyId)
                        && e.lastCommitedBlobId.equals(ShaId.EmptyId));
    }

    /**
     * Returns files, which are deleted from repository tree, but stil
     * in index
     *
     * @param userRepoRoot base user repository dir
     * @return list of ABSOLUTE paths
     */
    public static Collection<Path> getTreeDeletedFiles(Path userRepoRoot, Index index) {
        return index.stream()
                .map(e -> userRepoRoot.resolve(e.fileName))
                .filter(Files::notExists)
                .collect(Collectors.toList());
    }

    /**
     * Return files, which are modified in working tree, but this modifications
     * are not staged for commit
     *
     * @param userRepoRoot base repository dir
     */
    public static Collection<Path> getTreeModifiedFiles(Path userRepoRoot, Index index) {
        List<Path> files = new ArrayList<>();
        for (Index.Entry e : index) {
            Path p = userRepoRoot.resolve(e.fileName);
            if (Files.notExists(p)) {
                continue;
            }
            if (p.toFile().lastModified() > e.lastModified) {
                files.add(p);
            }
        }
        return files;
    }

    /**
     * Return files, which are newly added to working tree and not staged
     *
     * @param userRepoRoot base repository dir
     */
    public static Collection<Path> getTreeNewFiles(Path userRepoRoot, Index index) throws IOException {
        Set<Path> treeFiles = walk(userRepoRoot, resolveStoragePath(userRepoRoot))
                .collect(Collectors.toSet());
        Set<Path> indexedFiles = index.stream().map(it -> userRepoRoot.resolve(it.fileName))
                .collect(Collectors.toSet());

        treeFiles.removeAll(indexedFiles);
        return treeFiles;
    }

    /**
     * Returns proper wit storage root basing to given directory
     * @param baseDir base directory for version control
     * @return wit vcs storage root dir
     */
    public static Path resolveStoragePath(Path baseDir) {
        return baseDir.resolve(".wit");
    }

    /**
     * Returns path, from which service specific part was removed,
     * so this path is path to actual user repository data
     */
    public static Path stripWitStoragePath(Path witStoragePath) {
        return witStoragePath.getParent();
    }

    /**
     * Returns stream of regular file paths such that service wit paths are not included
     *
     * @param repositoryRoot path to some directory
     * @param witRoot path to storage repository root
     */
    public static Stream<Path> walk(Path repositoryRoot, Path witRoot) throws IOException {
        Path servicePrefix = witRoot;
        while (!servicePrefix.getParent().equals(stripWitStoragePath(witRoot))) {
            // that is done because service path may be not just root/.wit, but root/.what/.wit
            servicePrefix = servicePrefix.getParent();
        }
        Path leastServicePrefix = servicePrefix;
        return Files.walk(repositoryRoot).filter(p -> !p.startsWith(leastServicePrefix))
                .filter(Files::isRegularFile);
    }

    /**
     * Helper function
     */
    private static Stream<IdCommit> collectCommitHistoryStream(ShaId head, WitStorage storage) {
        if (head.equals(ShaId.EmptyId)) {
            return Stream.empty();
        }

        Commit c = storage.readCommit(head);
        Stream<IdCommit> commits = Stream.of(new IdCommit(head, c));

        for (ShaId parent : c.getParentCommitsIds()) {
            commits = Stream.concat(commits, collectCommitHistoryStream(parent, storage));
        }

        return commits;
    }
}
