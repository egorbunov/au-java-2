package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.java.wit.command.except.BranchNotSpecified;
import ru.spbau.mit.java.wit.command.except.NotAllChangesCommitted;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.Snapshot;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitStatusUtils;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;
import ru.spbau.mit.java.wit.utils.MergeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Command(name = "merge", description = "Merge some branch to currently active")
public class WitMerge implements WitCommand {
    @Arguments(description = "Branch, which will be merged into active branch")
    private String branch;

    private static class MergePair {
        final ShaId blobTo;
        final ShaId blobFrom;
        public final String fileName;


        private MergePair(ShaId blobTo, ShaId blobFrom, String fileName) {
            this.blobTo = blobTo;
            this.blobFrom = blobFrom;
            this.fileName = fileName;
        }
    }

    private class FilesForMerge {
        final List<MergePair> toMerge;
        final List<Snapshot.Entry> toCheckout;

        FilesForMerge(List<MergePair> toMerge, List<Snapshot.Entry> toCheckout) {
            this.toMerge = toMerge;
            this.toCheckout = toCheckout;
        }
    }

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        if (branch == null || branch.isEmpty()) {
            throw new BranchNotSpecified();
        }
        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());

        Branch mergingBranch = storage.readBranch(branch);
        if (mergingBranch == null) {
            System.err.println("Error: no such branch " + branch);
            return -1;
        }

        Index index = storage.readIndex();

        if (!WitStatusUtils.getStagedEntries(index).findAny().equals(Optional.empty())) {
            throw new NotAllChangesCommitted();
        }

        Snapshot mergingSnapshot = storage.readSnapshot(
                storage.readCommit(mergingBranch.getHeadCommitId()).getSnapshotId());

        FilesForMerge filesForMerge = getFilesForMerge(index, mergingSnapshot);
        List<MergePair> toMerge = filesForMerge.toMerge;
        List<Snapshot.Entry> toCheckout = filesForMerge.toCheckout;

        if (toMerge.size() == 0 && toCheckout.size() == 0) {
            System.out.println("Everything is up to date...");
            return 0;
        }

        checkoutNoConflict(storage, userRepositoryPath, index, toCheckout);
        mergeConflicting(storage, userRepositoryPath, mergingBranch, index, toMerge);

        storage.writeIndex(index);

        System.out.println("Merge complete. Now you can commit merge changes using 'wit commit'.");
        if (toMerge.size() > 0) {
            System.out.println("IMPORTANT: fix automatically merged files by hand!");
            System.out.println("           after fixing use 'wit add' to stage files");
        }

        return 0;
    }

    private void mergeConflicting(WitStorage storage, Path userRepositoryPath,
                                  Branch mergingBranch,
                                  Index index, List<MergePair> toMerge) throws IOException {
        if (toMerge.size() > 0) {
            System.out.println("Merging...");
        }

        String curBranchName = storage.readCurBranchName();
        for (MergePair mp : toMerge) {
            try {
                System.out.println("    " + mp.fileName);
                List<String> linesA = Files.readAllLines(storage.getBlobFile(mp.blobTo));
                List<String> linesB = Files.readAllLines(storage.getBlobFile(mp.blobFrom));
                List<String> merge = MergeUtils.merge(linesA, linesB, curBranchName, mergingBranch.getName());

                File fileToWrite = userRepositoryPath.resolve(mp.fileName).toFile();
                FileUtils.writeLines(fileToWrite, merge);
            } catch (MalformedInputException e) {
                System.err.println("Can't merge non-text file. Current master file version is " +
                        "remained.");
            }
        }

        // staging merged files
        for (MergePair mp : toMerge) {
            File file = userRepositoryPath.resolve(mp.fileName).toFile();
            ShaId blobId = storage.writeBlob(file);
            Index.Entry e = index.getEntryByFile(mp.fileName);
            index.remove(e);
            index.add(new Index.Entry(mp.fileName, file.lastModified(), blobId, e.curBlobId));
        }

        storage.writeMergeFlag(branch);
    }

    private void checkoutNoConflict(WitStorage storage, Path userRepositoryPath,
                                    Index index, List<Snapshot.Entry> toCheckout) throws IOException {
        for (Snapshot.Entry entry : toCheckout) {
            String name = entry.fileName;
            Path pToCheckout = userRepositoryPath.resolve(name);
            storage.checkoutBlob(entry.id, pToCheckout);
            index.add(new Index.Entry(name, pToCheckout.toFile().lastModified(),
                    entry.id, ShaId.EmptyId)); // file is treated as new one
        }
    }

    private FilesForMerge getFilesForMerge(Index index, Snapshot mergingSnapshot) {
        List<MergePair> toMerge = new ArrayList<>();
        List<Snapshot.Entry> toCheckout = new ArrayList<>();
        for (Snapshot.Entry entry : mergingSnapshot) {
            if (index.contains(entry.fileName)) {
                toMerge.add(new MergePair(
                        index.getEntryByFile(entry.fileName).curBlobId,
                        entry.id,
                        entry.fileName
                ));
            } else {
                toCheckout.add(entry);
            }
        }

        return new FilesForMerge(toMerge, toCheckout);
    }

    public void setBranch(String branch) {

        this.branch = branch;
    }
}
