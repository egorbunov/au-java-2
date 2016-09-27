package ru.spbau.mit.java.wit.model;


import java.util.*;


/**
 * Represents current repository state
 */
public class Index {
    public static class Entry {
        public final String fileName;
        public final ShaId lastCommitedBlobId;
        public final ShaId curBlobId;
        public final long lastModified;

        public Entry(ShaId blobId,
                      long lastModified,
                      String fileName,
                      ShaId lastCommitedBlobId) {
            this.curBlobId = blobId;
            this.lastModified = lastModified;
            this.fileName = fileName;
            this.lastCommitedBlobId = lastCommitedBlobId;
        }

        @Override
        public int hashCode() {
            return fileName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!Entry.class.isInstance(obj)) {
                return false;
            }
            Entry e = (Entry) obj;
            return e.fileName.equals(fileName);
        }
    }

    private Set<Entry> entries = new HashSet<>();
    private Map<String, Entry> entriesByFileName= new HashMap<>();

    public boolean addEntry(Entry entry) {
        if (entries.contains(entry)) {
            return false;
        }
        entries.add(entry);
        entriesByFileName.put(entry.fileName, entry);
        return true;
    }

    public boolean removeEntry(Entry entry) {
        return entries.remove(entry);
    }

    public Collection<Entry> getEntries() {
        return Collections.unmodifiableCollection(entries);
    }

    public Entry getEntry(String filename) {
        return entriesByFileName.get(filename);
    }
}
