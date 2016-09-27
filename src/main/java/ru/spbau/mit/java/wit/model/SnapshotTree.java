package ru.spbau.mit.java.wit.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * It's actually not a tree structure, but a list with pairs: Blob ID -> FileName
 * It represents a filesystem snapshot
 */
public class SnapshotTree {
    /**
     * ShaId -> FileName
     * ShaId is blob id -- id of file, where contents are stored
     * FileName is name of the file, witch contents are stored in that blob
     */
    private Map<ShaId, String> filesById;
    private Map<String, ShaId> idsByFile;

    public Set<ShaId> getBlobIds() {
        return Collections.unmodifiableSet(filesById.keySet());
    }

    public String getFileNameByBlobId(ShaId id) {
        return filesById.get(id);
    }

    public ShaId getBlobIdByFileName(String filename) {
        return idsByFile.get(filename);
    }

    public boolean putFile(ShaId blobId, String fileName) {
        if (filesById.containsKey(blobId) || idsByFile.containsKey(fileName)) {
            return false;
        }
        filesById.put(blobId, fileName);
        idsByFile.put(fileName, blobId);
        return true;
    }
}
