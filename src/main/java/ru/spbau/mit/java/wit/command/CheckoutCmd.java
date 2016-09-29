package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.ShaId;
import ru.spbau.mit.java.wit.model.Snapshot;
import ru.spbau.mit.java.wit.storage.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

@Command(name = "checkout", description = "Switch to branch or revision")
public class CheckoutCmd implements Runnable {
    @Arguments(description = "Name of branch or revision (commit) identifier")
    String ref;

    @Override
    public void run() {
        Path root = WitRepo.findRepositoryRoot();
        if (root == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        // resolving commit reference
        ShaId commitId;
        Branch b = BranchStorage.read(ref);
        if (b != null) {
            commitId = b.getHeadCommitId();
        } else {
            commitId = CommitStorage.resolveCommitByPrefix(ref);
        }

        if (commitId.equals(ShaId.EmptyId)) {
            System.err.println("Error: can't find neither branch nor commit with given identifier");
            return;
        }

        // checking out and creating new index
        Index newIndex = new Index();
        Snapshot snapshot = SnapshotTreeStorage.read(commitId);
        for (ShaId id : snapshot.getBlobIds()) {
            String name = snapshot.getFileNameByBlobId(id);
            Path pToCheckout = root.resolve(name);
            File blobFile = FileStorage.getBlobFile(id);
            Path target;
            try {
                // TODO: do not copy if files are equal
                target = Files.copy(blobFile.toPath(), pToCheckout,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Error: can't checkout file " + pToCheckout);
                return;
            }
            // index update...
            newIndex.add(new Index.Entry(id, target.toFile().lastModified(), name, id));
        }

        // deleting files not existing in target revision
        Index curIndex = IndexStorage.readIndex();
        for (Index.Entry e : curIndex.getEntries()) {
            // deleting not in target revision && only if file is commited
            if (snapshot.getBlobIdByFileName(e.fileName) == null &&
                    e.curBlobId.equals(e.lastCommitedBlobId)) {

                Path p = root.resolve(e.fileName);
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e1) {
                    System.err.println("Error: can't delete file " + p);
                    return;
                }
            }
        }

        // replacing index
        IndexStorage.writeIndex(newIndex);
    }
}
