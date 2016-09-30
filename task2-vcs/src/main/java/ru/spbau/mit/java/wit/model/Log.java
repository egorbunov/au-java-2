package ru.spbau.mit.java.wit.model;

import ru.spbau.mit.java.wit.model.id.ShaId;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

public class Log extends AbstractCollection<Log.Entry> {
    public static class Entry {
        public final ShaId commitId;
        public final String msg;

        public Entry(ShaId commitId, String msg) {
            this.commitId = commitId;
            this.msg = msg;
        }

        @Override
        public boolean equals(Object obj) {
            if (!Entry.class.isInstance(obj)) {
                return false;
            }
            Entry l = (Entry) obj;
            return l.msg.equals(msg) && l.commitId.equals(commitId);
        }
    }

    private ArrayList<Entry> log = new ArrayList<>();

    @Override
    public boolean add(Entry entry) {
        log.add(entry);
        return true;
    }

    @Override
    public Iterator<Entry> iterator() {
        return log.iterator();
    }

    @Override
    public int size() {
        return log.size();
    }

    @Override
    public void forEach(Consumer<? super Entry> action) {
        log.forEach(action);
    }

    @Override
    public Spliterator<Entry> spliterator() {
        return log.spliterator();
    }
}
