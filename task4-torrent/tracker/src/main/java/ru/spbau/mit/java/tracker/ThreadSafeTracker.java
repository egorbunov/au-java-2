package ru.spbau.mit.java.tracker;


import ru.spbau.mit.protocol.tracker.TrackerFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ThreadSafeTracker<U, F> implements Tracker<U, F> {
    private final IdProducer<F> fileIdProducer;
    private final Map<F, TrackerFile<F>> fileMap;
    private final Map<F, Set<U>> seedsMap;

    public ThreadSafeTracker(IdProducer<F> fileIdProducer) {
        this.fileIdProducer = fileIdProducer;
        this.fileMap = new ConcurrentHashMap<>();
        this.seedsMap = new ConcurrentHashMap<>();
    }

    public Collection<TrackerFile<F>> list() {
        return Collections.unmodifiableCollection(fileMap.values());
    }

    public void update(U clientId, List<F> fileIds) {
        for (F id : fileIds) {
            if (!fileMap.containsKey(id)) {
                throw new IllegalArgumentException("Can't set seed for non existing file");
            }

            if (!seedsMap.containsKey(id)) {
                seedsMap.put(id, new ConcurrentSkipListSet<>());
            }

            seedsMap.get(id).add(clientId);
        }
    }

    public F upload(FileInfo fileInfo) {
        F id = fileIdProducer.nextId();
        if (fileMap.containsKey(id)) {
            throw new RuntimeException("File id duplicate!");
        }
        fileMap.put(id, new TrackerFile<>(id, fileInfo.getName(), fileInfo.getSize()));
        return id;
    }

    public Collection<U> source(F fileId) {
        return null;
    }

    public void removeClient(U clientId) {
        for (Map.Entry<F, Set<U>> id : seedsMap.entrySet()) {
            id.getValue().remove(clientId);
        }
    }
}
