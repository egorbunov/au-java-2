package ru.spbau.mit.java;

import ru.spbau.mit.java.files.FileBlocksStorage;
import ru.spbau.mit.java.files.SimpleBlockStorage;
import ru.spbau.mit.java.leech.FileBlocksDownloader;
import ru.spbau.mit.java.shared.tracker.ClientId;
import ru.spbau.mit.java.shared.tracker.TrackerFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * Tracker client command line interface, I guess
 */
public class Main {

    public static void printHelp() {
        System.out.println("USAGE: java -jar client.jar <CLIENT_ID>");
        System.out.println("After successful start you will be prompted.");
        System.out.println("Possible commands are: ");
        System.out.println("    * stop");
        System.out.println("            to stop client");
        System.out.println("    * list");
        System.out.println("            to list files available on remote tracker");
        System.out.println("    * source <file id>");
        System.out.println("            to list seeders of the file");
        System.out.println("    * upload <path/to/local/file.txt> <file_pseudonym>");
        System.out.println("            to upload your local file with given pseudonym to tracker");
        System.out.println("    * download <file_id> <destination/file.txt>");
        System.out.println("            to download file with given id to local path, which is also specified");
        System.out.println("WARNING: no spaces in paths supported");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length < 1) {
            printHelp();
            System.exit(1);
        }
        String curClientId = args[0];

        Path fileStoragePath = Paths.get(System.getProperty("java.io.tmpdir"))
                .resolve("file_storage_data_" + curClientId + ".bin");

        FileBlocksStorage fileBlocksStorage;
        if (Files.exists(fileStoragePath)) {
            ObjectInputStream oi = new ObjectInputStream(new FileInputStream(fileStoragePath.toFile()));
            fileBlocksStorage = (FileBlocksStorage) oi.readObject();
            oi.close();
        } else {
            fileBlocksStorage = new SimpleBlockStorage(10485760);
        }

        TrackerClientFacade facade = new TrackerClientFacade(
                "localhost",
                8081,
                fileBlocksStorage,
                5678 + new Random().nextInt(100)
        );

        facade.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        label:
        while (true) {
            try {
                System.out.print("CMD >> ");
                String str = br.readLine();
                if (str == null) {
                    facade.stop();
                    break;
                }
                String[] split = str.split("\\s+");
                if (split[0].length() == 0) {
                    continue;
                }
                switch (split[0]) {
                    case "stop":
                        facade.stop();
                        break label;
                    case "list": {
                        List<TrackerFile<Integer>> files = facade.listTrackerFiles();
                        System.out.format("%10s %10s %30s\n", "id", "size", "name");

                        for (TrackerFile<Integer> f : files) {
                            System.out.format("%10d %10d %30s\n", f.getId(), f.getSize(), f.getName());
                        }
                        break;
                    }
                    case "source": {
                        if (split.length < 2) {
                            System.out.println("ERROR: no file id specified");
                            continue;
                        }
                        int fileId = Integer.valueOf(split[1]);
                        List<ClientId> clientIps = facade.listSeedersForFile(fileId);
                        for (ClientId clientId : clientIps) {
                            System.out.println(clientId);
                        }
                        break;
                    }
                    case "upload":
                        if (split.length < 2) {
                            System.out.println("ERROR: no local path specified");
                            continue;
                        }
                        String locPath = split[1];
                        if (split.length < 3) {
                            System.out.println("ERROR: no pseudonym specified");
                            continue;
                        }
                        String name = split[2];
                        facade.uploadFile(locPath, name);
                        break;
                    case "download": {
                        if (split.length < 2) {
                            System.out.println("ERROR: no file id specified");
                            continue;
                        }
                        int fileId = Integer.valueOf(split[1]);
                        if (split.length < 3) {
                            System.out.println("ERROR: no local file destination specified");
                            continue;
                        }
                        String locDest = split[2];
                        List<TrackerFile<Integer>> files = facade.listTrackerFiles();
                        FileBlocksDownloader downloader = null;
                        for (TrackerFile<Integer> tf : files) {
                            if (tf.getId() == fileId) {
                                downloader = facade.getFileDownloader(tf, locDest);
                                break;
                            }
                        }
                        if (downloader == null) {
                            System.out.println("ERROR: no file with specified id found on tracker");
                            continue;
                        }

                        downloader.start();

                        while (downloader.downloadedBlockNum() < downloader.goalBlockNum()) {
                            System.out.println("Downloaded: " + downloader.downloadedBlockNum() +
                                    " from " + downloader.goalBlockNum());
                            Thread.sleep(100);
                        }
                        System.out.println("Downloaded: " + downloader.downloadedBlockNum() +
                                " from " + downloader.goalBlockNum());
                        downloader.join();

                        break;
                    }
                    default:
                        System.out.println("ERROR: Unknown command...");
                        printHelp();
                }
            } catch (Exception | Error e) {
                System.out.println("ERROR: Exception occurred...");
                e.printStackTrace();
            }

        }

        ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(fileStoragePath.toFile()));
        oo.writeObject(fileBlocksStorage);
    }
}
