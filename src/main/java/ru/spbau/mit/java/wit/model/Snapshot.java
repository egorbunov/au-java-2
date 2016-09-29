package ru.spbau.mit.java.wit.model;

import java.util.*;
import java.util.function.Consumer;

/**
 * It's actually not a tree structure, but a list with pairs: Blob ID -> FileName
 * It represents a filesystem snapshot
 */
public class Snapshot extends AbstractCollection<Snapshot.Entry> {
    public static class Entry {
        public final ShaId id;
        public final String fileName;

        public Entry(ShaId id, String fileName) {
            this.id = id;
            this.fileName = fileName;
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

    private Set<Entry> records = new HashSet<>();


    @Override
    public boolean add(Entry entry) {
        return records.add(entry);
    }

    @Override
    public Iterator<Entry> iterator() {
        return records.iterator();
    }

    @Override
    public int size() {
        return records.size();
    }

    @Override
    public void forEach(Consumer<? super Entry> action) {
        records.forEach(action);
    }

    @Override
    public Spliterator<Entry> spliterator() {
        return records.spliterator();
    }
}
