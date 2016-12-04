package ru.spbau.mit.java;

import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.FileInfo;
import ru.spbau.mit.java.shared.tracker.Tracker;
import ru.spbau.mit.java.tracker.ThreadSafeIntIdProducer;
import ru.spbau.mit.java.tracker.ThreadSafeTracker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Path trackerDataPath
            = Paths.get(System.getProperty("java.io.tmpdir")).resolve("tracker_data.bin");

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Tracker<ClientId, Integer> tracker = null;
        if (Files.exists(trackerDataPath)) {
            ObjectInputStream oi = new ObjectInputStream(new FileInputStream(trackerDataPath.toFile()));
            tracker = (Tracker<ClientId, Integer>) oi.readObject();
            oi.close();
        } else {
            tracker = new ThreadSafeTracker<>(new ThreadSafeIntIdProducer(0));
        }

        TrackerServer server = new TrackerServer(8081, tracker, TimeUnit.MINUTES.toMillis(5));

        server.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("CMD >> ");
            String str = br.readLine();
            if (str == null) {
                server.stop();
                break;
            }
            if (str.equals("stop")) {
                server.stop();
                break;
            }
        }

        ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(trackerDataPath.toFile()));
        oo.writeObject(tracker);
    }
}
