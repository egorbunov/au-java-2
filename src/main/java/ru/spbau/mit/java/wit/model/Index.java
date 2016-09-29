package ru.spbau.mit.java.wit.model;


import java.util.*;
import java.util.function.Consumer;


/**
 * Represents current repository state
 */
public class Index extends AbstractCollection<Index.Entry> {
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

    @Override
    public boolean add(Entry entry) {
        if (entries.contains(entry)) {
            return false;
        }
        entries.add(entry);
        entriesByFileName.put(entry.fileName, entry);
        return true;
    }

    @Override
    public boolean remove(Object entry) {
        return entries.remove(entry);
    }

    public Entry getEntryByFile(String filename) {
        return entriesByFileName.get(filename);
    }

    public boolean contains(String filename) {
        return entriesByFileName.containsKey(filename);
    }

    // iterator stuff
    @Override
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void forEach(Consumer<? super Entry> action) {
        entries.forEach(action);
    }

    @Override
    public Spliterator<Entry> spliterator() {
        return entries.spliterator();
    }

}
