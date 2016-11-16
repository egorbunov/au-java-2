package ru.spbau.mit.java;

import ru.spbau.mit.java.protocol.TrackerProtocol;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.util.Collection;
import java.util.List;

/**
 * Interface to tracker which resides on tracker-server side.
 * So every method performs request sending to the tracker and
 * waits for them being answered
 */
public class RemoteTracker implements Tracker<ClientId, Integer> {
    private TrackerProtocol trackerProtocol;

    public RemoteTracker(TrackerProtocol trackerProtocol) {
        this.trackerProtocol = trackerProtocol;
    }

    @Override
    public Collection<TrackerFile<Integer>> list() {
        return null;
    }

    @Override
    public void update(ClientId clientId, List<Integer> fileIds) {

    }

    @Override
    public Integer upload(FileInfo fileInfo) {
        return null;
    }

    @Override
    public Collection<ClientId> source(Integer fileId) {
        return null;
    }

    @Override
    public void removeClient(ClientId clientId) {

    }
}
