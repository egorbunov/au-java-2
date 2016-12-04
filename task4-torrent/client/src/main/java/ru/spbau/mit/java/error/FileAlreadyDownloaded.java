package ru.spbau.mit.java.error;

import ru.spbau.mit.java.shared.tracker.TrackerFile;

public class FileAlreadyDownloaded extends Error {
    private final TrackerFile<Integer> tf;
    private final String localPath;

    /**
     * @param tf tracker file, which is already locally available
     * @param localPath local path to file, where it can be found
     */
    public FileAlreadyDownloaded(TrackerFile<Integer> tf, String localPath) {

        this.tf = tf;
        this.localPath = localPath;
    }

    public TrackerFile<Integer> getTf() {
        return tf;
    }

    public String getLocalPath() {
        return localPath;
    }
}
