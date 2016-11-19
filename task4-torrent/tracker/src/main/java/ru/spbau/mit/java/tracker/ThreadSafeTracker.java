package ru.spbau.mit.java.tracker;


import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * Tracker implemented with lock-free data structures
 * This tracker class is serializable because we have to
 * save tracker state to restore it on every new server start
 *
 * TODO: is it efficient?
 */
public class ThreadSafeTracker<U, F> implements Tracker<U, F>, Serializable {
    private final IdProducer<F> fileIdProducer;
    private final Map<F, TrackerFile<F>> fileMap;
    private final Map<F, Set<U>> seedsMap;

    public ThreadSafeTracker(IdProducer<F> fileIdProducer) {
        this.fileIdProducer = fileIdProducer;
        this.fileMap = new ConcurrentHashMap<>();
        this.seedsMap = new ConcurrentHashMap<>();
    }

    public Collection<TrackerFile<F>> list() {
        return fileMap.entrySet().stream()
                // filtering files which are not seeded
                .filter(it -> !seedsMap.get(it.getKey()).isEmpty()).map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public void update(U clientId, List<F> fileIds) {
        for (F id : fileIds) {
            if (fileMap.getOrDefault(id, null) == null) {
                throw new IllegalArgumentException(
                        "Can't set seed for non existing file " + id + "; client: " + clientId);
            }
            seedsMap.get(id).add(clientId);
        }
    }

    public F upload(FileInfo fileInfo) {
        F id = fileIdProducer.nextId();
        if (fileMap.containsKey(id)) {
            throw new RuntimeException("File id duplicate: " + id);
        }
        fileMap.put(id, new TrackerFile<>(id, fileInfo.getName(), fileInfo.getSize()));
        seedsMap.put(id, new ConcurrentSkipListSet<>());
        return id;
    }

    public Collection<U> source(F fileId) {
        if (!fileMap.containsKey(fileId)) {
            throw new IllegalArgumentException("No such file");
        }
        return seedsMap.get(fileId);
    }

    public void removeClient(U clientId) {
        for (Map.Entry<F, Set<U>> id : seedsMap.entrySet()) {
            id.getValue().remove(clientId);
        }
    }
}
