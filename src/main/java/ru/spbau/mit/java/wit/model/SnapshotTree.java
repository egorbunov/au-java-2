package ru.spbau.mit.java.wit.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

/**
 * It's actually not a tree structure, but a list with pairs: Blob ID -> FileName
 * It represents a filesystem snapshot
 */
public class SnapshotTree {
    /**
     * Id -> FileName
     * Id is blob id -- id of file, where contents are stored
     * FileName is name of the file, witch contents are stored in that blob
     */
    private Map<Id, String> files;

    public Set<Id> getBlobIds() {
        return Collections.unmodifiableSet(files.keySet());
    }

    public String getFileNameByBlobId(Id id) {
        return files.get(id);
    }

    public boolean putFile(Id blobId, String fileName) {
        if (files.containsKey(blobId)) {
            return false;
        }
        files.put(blobId, fileName);
        return true;
    }
}
