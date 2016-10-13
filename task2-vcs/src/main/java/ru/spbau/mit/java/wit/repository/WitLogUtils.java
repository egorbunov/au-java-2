package ru.spbau.mit.java.wit.repository;

import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.id.IdCommit;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Egor Gorbunov
 * Date: 10/2/16
 * Email: egor-mailbox@ya.com
 */
public class WitLogUtils {
    /**
     * Reads branch commit history from storage
     */
    public static List<IdCommit> readBranchLog(String branch, WitStorage storage) throws IOException {
        List<ShaId> ids = storage.readCommitLog(branch);
        List<IdCommit> log = new ArrayList<>(ids.size());
        for (ShaId id : ids) {
            log.add(new IdCommit(id, storage.readCommit(id)));
        }
        return log;
    }
    /**
     * Merges branches logs accordingly to timestamp
     * TODO: improve algorithm complexity
     */
    public static List<IdCommit> mergeBranchHistories(List<String> branches, WitStorage storage) throws IOException {
        Stream<IdCommit> logStream = Stream.empty();
        for (String b : branches) {
            logStream = Stream.concat(logStream, readBranchLog(b, storage).stream());
        }
        return logStream.collect(Collectors.toSet())
                .stream()
                .sorted(Comparator.comparing(c -> c.commit.getTimestamp()))
                .collect(Collectors.toList());
    }

    public static Stream<IdCommit> mergeCommitHistories(List<ShaId> commits, WitStorage storage)
            throws IOException {
        Stream<IdCommit> commitStream = Stream.empty();
        for (ShaId id : commits) {
            commitStream = Stream.concat(commitStream, collectCommitHistoryStream(id, storage));
        }
        return sortedHistoryStream(commitStream);
    }

    /**
     * Returns list of commits, sorted accordingly to their time stamp
     * Commits are retrieved by traversing parent commits from head commit,
     * specified by {@code headCommitId}.
     */
    public static Stream<IdCommit> readCommitHistory(ShaId headCommitId, WitStorage storage) throws IOException {
        return sortedHistoryStream(collectCommitHistoryStream(headCommitId, storage));
    }

    private static Stream<IdCommit> sortedHistoryStream(Stream<IdCommit> stream) {
        return stream
                .collect(Collectors.toSet())
                .stream()
                .sorted(Comparator.comparing(c -> c.commit.getTimestamp()));
    }

    /**
     * Helper function; Merges all commits up from given HEAD, but do not sorting them
     */
    private static Stream<IdCommit> collectCommitHistoryStream(ShaId head, WitStorage storage) throws IOException {
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
