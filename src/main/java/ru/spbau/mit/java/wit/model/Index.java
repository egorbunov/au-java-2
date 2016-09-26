package ru.spbau.mit.java.wit.model;


import java.util.*;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class Index {
    public static class Entry {
        final Id blobId;
        final Date modificationDate;
        final String fileName;
        final boolean isCommited;

        private Entry(Id blobId,
                      Date modificationDate,
                      String fileName,
                      boolean isCommited) {
            this.blobId = blobId;
            this.modificationDate = modificationDate;
            this.fileName = fileName;
            this.isCommited = isCommited;
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

    private Set<Entry> entries;

    public Index() {
        entries = new HashSet<>();
    }

    boolean addEntry(Entry entry) {
        return entries.add(entry);
    }

    Collection<Entry> getEntries() {
        return Collections.unmodifiableCollection(entries);
    }
}
