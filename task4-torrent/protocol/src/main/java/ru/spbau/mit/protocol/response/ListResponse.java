package ru.spbau.mit.protocol.response;


import ru.spbau.mit.protocol.tracker.TrackerFile;

import java.util.Collection;

public class ListResponse {
    private final Collection<TrackerFile<Integer>> files;

    public ListResponse(Collection<TrackerFile<Integer>> files) {
        this.files = files;
    }

    public Collection<TrackerFile<Integer>> getFiles() {
        return files;
    }
}
