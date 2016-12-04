package ru.spbau.mit.java.shared.response;


import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListResponse {
    private final List<TrackerFile<Integer>> files;

    public ListResponse(List<TrackerFile<Integer>> files) {
        this.files = files;
    }

    public List<TrackerFile<Integer>> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return "[" + files.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
