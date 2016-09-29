package ru.spbau.mit.java.wit.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.Commit;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;
import ru.spbau.mit.java.wit.model.Snapshot;
import ru.spbau.mit.java.wit.storage.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path witRoot = WitRepo.findRepositoryRoot(baseDir);
        if (witRoot == null) {
            System.err.println("Error: You are not under WIT repository");
            return;
        }

        WitStorage storage = new WitStorage(witRoot);

        // resolving commit reference
        // WARNING: super ugly try/catch code =(
        ShaId commitId;
        try {
            Branch b = storage.readBranch(ref);
            commitId = b.getHeadCommitId();
        } catch (FileNotFoundException e) {
            try {
                List<ShaId> cmts = storage.resolveCommitIdsByPrefix(ref);
                if (cmts.size() > 1) {
                    System.err.println("Error: can't resolve commit, " +
                            "too short prefix: " + ref);
                    return;
                } else if (cmts.isEmpty()) {
                    System.err.println("Error: can't find commit or branch!");
                    return;
                }
                commitId = cmts.get(0);
            } catch (IOException e1) {
                System.err.println("Error: failed to resolve commit" + ref);
                e1.printStackTrace();
                return;
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to read branch file");
            e.printStackTrace();
            return;
        }

        // checking out and creating new index
        Index newIndex = new Index();

        Snapshot snapshot;
        try {
            Commit commit = storage.readCommit(commitId);
            snapshot = storage.readSnapshot(commit.getSnapshotId());
        } catch (IOException e) {
            System.err.println("Error: Can't read snapshot!");
            e.printStackTrace();
            return;
        }

        for (Snapshot.Entry entry : snapshot) {
            String name = entry.fileName;
            Path pToCheckout = baseDir.resolve(name);

            try {
                // TODO: do not copy if files are equal
                storage.checkoutBlob(entry.id, pToCheckout);
            } catch (IOException e) {
                System.err.println("Error: can't checkout file " + pToCheckout);
                return;
            }

            // index update...
            newIndex.add(new Index.Entry(
                    entry.id,
                    pToCheckout.toFile().lastModified(),
                    name,
                    entry.id));
        }

        // deleting files not existing in target revision
        Index curIndex;
        try {
            curIndex = storage.readIndex();
        } catch (IOException e) {
            System.err.println("Can't read index");
            e.printStackTrace();
            return;
        }

        for (Index.Entry e : curIndex) {
            // deleting not in target revision && only if file is commited
            if (snapshot.getBlobIdByFileName(e.fileName) == null &&
                    e.curBlobId.equals(e.lastCommitedBlobId)) {

                Path p = baseDir.resolve(e.fileName);
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e1) {
                    System.err.println("Error: can't delete file " + p);
                    return;
                }
            }
        }

        // replacing index
        try {
            storage.writeIndex(newIndex);
        } catch (IOException e) {
            System.err.println("Error: can't write index");
            e.printStackTrace();
        }
    }
}
