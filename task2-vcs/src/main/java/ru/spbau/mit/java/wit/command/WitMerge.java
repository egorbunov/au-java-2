package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import org.apache.commons.io.FileUtils;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.Snapshot;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;
import ru.spbau.mit.java.wit.utils.MergeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "merge", description = "WitMerge some branch to currently active")
public class WitMerge implements WitCommand {
    @Arguments(description = "Branch, which will be merged into active branch")
    private String branch;

    private static class MergePair {
        public final ShaId blobTo;
        public final ShaId blobFrom;
        public final String fileName;


        private MergePair(ShaId blobTo, ShaId blobFrom, String fileName) {
            this.blobTo = blobTo;
            this.blobFrom = blobFrom;
            this.fileName = fileName;
        }
    }

    @Override
    public int execute(Path workingDir, WitStorage storage) throws IOException {
        if (branch == null || branch.isEmpty()) {
            System.err.println("Error: no branch specified to merge...");
            return -1;
        }
        Path userRepositoryPath = WitUtils.stripWitStoragePath(storage.getWitRoot());

        /*
            1) Read index of current branch
            2) Read snapshot of merged branch
            3) Separate conflicting files
            4) Just checkout non-conflicting files
            6) Merge conflicting files
            7) Stage merged conflicting files
            8) Merge logs
         */

        String curBranchName = storage.readCurBranchName();

        Branch mergingBranch = storage.readBranch(branch);
        if (mergingBranch == null) {
            System.err.println("Error: no such branch " + branch);
            return -1;
        }

        // current index, which will be transformed to proper after-merged state
        Index index = storage.readIndex();
        if (!WitUtils.getStagedEntries(index).findAny().equals(Optional.empty())) {
            System.err.println("Error: you have staged changes in your repository, " +
                    "commit them before merging.");
            return -1;
        }

        Snapshot mergingSnapshot = storage.readSnapshot(
                storage.readCommit(mergingBranch.getHeadCommitId()).getSnapshotId());

        // determining conflicting and non-conflicting files
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

        if (toMerge.size() == 0 && toCheckout.size() == 0) {
            System.out.println("Everything is up to date...");
            return 0;
        }

        // simply checking out non-conflicting files (non-conflicting by names =))
        // and staging them for commit
        for (Snapshot.Entry entry : toCheckout) {
            String name = entry.fileName;
            Path pToCheckout = userRepositoryPath.resolve(name);
            storage.checkoutBlob(entry.id, pToCheckout);
            index.add(new Index.Entry(name, pToCheckout.toFile().lastModified(),
                    entry.id, ShaId.EmptyId)); // file is treated as new one
        }

        if (toMerge.size() > 0) {
            System.out.println("Merging...");
        }

        // calculating diff between conflicting files and writing them
        for (MergePair mp : toMerge) {
            System.out.println("    " + mp.fileName);
            List<String> linesA = Files.readAllLines(storage.getBlobFile(mp.blobTo));
            List<String> linesB = Files.readAllLines(storage.getBlobFile(mp.blobFrom));
            List<String> merge = MergeUtils.merge(linesA, linesB, curBranchName, mergingBranch.getName());

            File fileToWrite = userRepositoryPath.resolve(mp.fileName).toFile();
            FileUtils.writeLines(fileToWrite, merge);
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

        // finally writing updated index
        storage.writeIndex(index);

        System.out.println("Merge complete. Now you can commit merge changes using 'wit commit'.");
        if (toMerge.size() > 0) {
            System.out.println("IMPORTANT: fix automatically merged files by hand!");
            System.out.println("           after fixing use 'wit add' to stage files");
        }

        return 0;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
