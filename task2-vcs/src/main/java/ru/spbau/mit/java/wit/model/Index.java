package ru.spbau.mit.java.wit.model;


import ru.spbau.mit.java.wit.model.id.ShaId;

import java.util.*;
import java.util.function.Consumer;


/**
 * Represents current repository state
 */
public class Index extends AbstractCollection<Index.Entry> {

    /**
     * Entry describes one file in working tree.
     * {@code fileName}
     * {@code lastCommit}
     * {@code modified}
     * {@code }
     */
    public static class Entry {
        /**
         * Name of file in index; is always relative to root repo dir
         */
        public final String fileName;

        /**
         * id of blob, which was written on most fresh commit, which included that file
         * must not be null (for null use {@code ShaId.EmptyId}
         */
        public final ShaId lastCommittedBlobId;

        /**
         * id of blob, which is either equal to {@code lastCommittedBlobId} if file is not staged
         * for commit, but committed or it is not equal to {@code lastCommittedBlobId} meaning that
         * file is staged for next commit;
         *
         * if this field is equal to {@code ShaId.EmptyId}, when file is treated as staged
         * for deletion in next commit
         */
        public final ShaId curBlobId;

        /**
         * modification timestamp for moment, when file was staged to commit
         * (added by add command)
         */
        public final long modified;

        public Entry(String fileName, long modified,
                     ShaId blobId,
                     ShaId lastCommittedBlobId) {
            this.curBlobId = blobId;
            this.modified = modified;
            this.fileName = fileName;
            this.lastCommittedBlobId = lastCommittedBlobId;
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

        public boolean isStagedForDelete() {
            return curBlobId.equals(ShaId.EmptyId)
                    && !lastCommittedBlobId.equals(ShaId.EmptyId);
        }

        public boolean isStagedForCreate() {
            return lastCommittedBlobId.equals(ShaId.EmptyId)
                    && !curBlobId.equals(ShaId.EmptyId);
        }

        public boolean isStagedForUpdate() {
            return !lastCommittedBlobId.equals(curBlobId)
                    && !curBlobId.equals(ShaId.EmptyId)
                    && !lastCommittedBlobId.equals(ShaId.EmptyId);
        }

        public boolean isStaged() {
            return isStagedForDelete() || isStagedForUpdate() || isStagedForCreate();
        }

        /**
         * Designates that such entry must not be in index
         */
        public boolean isInvalid() {
            return curBlobId.equals(ShaId.EmptyId) && lastCommittedBlobId.equals(ShaId.EmptyId);
        }

    }

    private final Set<Entry> entries = new HashSet<>();
    private final Map<String, Entry> entriesByFileName= new HashMap<>();

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
