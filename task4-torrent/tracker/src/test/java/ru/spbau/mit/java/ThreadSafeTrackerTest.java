package ru.spbau.mit.java;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.shared.tracker.TrackerFile;
import ru.spbau.mit.java.tracker.ThreadSafeIntIdProducer;
import ru.spbau.mit.java.tracker.ThreadSafeTracker;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ThreadSafeTrackerTest {
    Tracker<Integer, Integer> tracker;

    @Before
    public void setup() {
        tracker = new ThreadSafeTracker<>(new ThreadSafeIntIdProducer(0));
    }

    @Test
    public void testListIsEmptyBeforeUpdate() {
        tracker.upload(new FileInfo(10, "file1"));
        tracker.upload(new FileInfo(10, "file2"));
        Collection<TrackerFile<Integer>> list = tracker.list();
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testOneThreaded() {
        int fileId = tracker.upload(new FileInfo(10, "file1"));
        tracker.update(1, Collections.singletonList(fileId));
        Collection<TrackerFile<Integer>> list = tracker.list();

        Assert.assertEquals(1, list.size());
        TrackerFile<Integer> file = list.iterator().next();
        Assert.assertEquals(fileId, (int) file.getId());
        Assert.assertEquals("file1", file.getName());
        Assert.assertEquals(10, file.getSize());

        tracker.removeClient(1);
        Assert.assertTrue(tracker.list().isEmpty());
    }

    @Test
    public void testMultiThreaded() throws InterruptedException {
        final int numThreads = 100;
        List<FileInfo> fileInfos = new ArrayList<>();
        for (int i = 0; i < numThreads; ++i) {
            fileInfos.add(new FileInfo(i, "filename" + i));
        }
        Integer[] fileIds = new Integer[numThreads];

        List<Thread> clientThreads = new ArrayList<>();
        for (int i = 0; i < numThreads; ++i) {
            final int idx = i;
            clientThreads.add(new Thread(() -> {
                fileIds[idx] = tracker.upload(fileInfos.get(idx));
                tracker.update(idx, Collections.singletonList(fileIds[idx]));
            }));
        }
        clientThreads.forEach(Thread::start);
        for (Thread t : clientThreads) {
            t.join();
        }

        Integer[] sortedFileIds = Arrays.copyOf(fileIds, fileIds.length);
        Arrays.sort(sortedFileIds);

        Collection<TrackerFile<Integer>> list = tracker.list();
        Assert.assertArrayEquals(
                sortedFileIds,
                list.stream().map(TrackerFile::getId)
                        .sorted().collect(Collectors.toList()).toArray(new Integer[]{}));

        for (int clientId = 0; clientId < numThreads; ++clientId) {
            Collection<Integer> source = tracker.source(fileIds[clientId]);
            Assert.assertEquals(1, source.size());
            Assert.assertEquals(clientId, (int) source.iterator().next());
        }
    }

}
