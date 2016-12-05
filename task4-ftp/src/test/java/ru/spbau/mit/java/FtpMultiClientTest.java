package ru.spbau.mit.java;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by: Egor Gorbunov
 * Date: 12/5/16
 * Email: egor-mailbox@ya.com
 */
public class FtpMultiClientTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    private FtpServer server;
    private List<FtpClient> clients = new ArrayList<>();
    private int numClients = 100;

    @Before
    public void setup() throws InterruptedException, IOException {
        server = new FtpServer(2000);
        server.start();

        Thread.sleep(100);

        for (int i = 0; i < numClients; ++i) {
            clients.add(new FtpClient("127.0.0.1", 2000));
            clients.get(clients.size() - 1).connect();
        }
    }

    @After
    public void destroy() throws IOException {
        for (FtpClient c : clients) {
            c.disconnect();
        }
        server.stop();
    }


    @Test
    public void testListFiles() throws IOException, InterruptedException {
        Path dir = baseFolder.getRoot().toPath().toAbsolutePath();
        List<String> fNames = Stream.of("3.txt", "1.txt", "2.txt")
                .sorted().collect(Collectors.toList());
        for (String f : fNames) {
            baseFolder.newFile(f);
        }
        List<String> dirNames = Stream.of("c", "a", "b")
                .sorted().collect(Collectors.toList());
        for (String d : dirNames) {
            baseFolder.newFolder(d);
        }

        Map<Integer, List<FileInfo>> fileInfosMap = new ConcurrentHashMap<>();

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numClients; ++i) {
            FtpClient c = clients.get(i);
            int finalI = i;
            threads.add(new Thread(() -> {
                try {
                    List<FileInfo> fileInfos = c.executeList(dir.toString());
                    fileInfosMap.put(finalI, fileInfos);

                } catch (IOException e) {
                    throw new IOError(e);
                }
            }));
            threads.get(threads.size() - 1).start();
        }

        for (Thread t : threads) {
            t.join();
        }

        for (int i = 0; i < numClients; ++i) {
            List<FileInfo> fileInfos = fileInfosMap.get(i);
            List<String> actualDirs = fileInfos.stream().filter(FileInfo::isDirectory)
                    .map(f -> Paths.get(f.getName()).getFileName().toString())
                    .sorted().collect(Collectors.toList());
            List<String> actualFiles = fileInfos.stream().filter(x -> !x.isDirectory())
                    .map(f -> Paths.get(f.getName()).getFileName().toString())
                    .sorted().collect(Collectors.toList());
            Assert.assertThat(actualDirs, is(dirNames));
            Assert.assertThat(actualFiles, is(fNames));
        }
    }

    @Test
    public void testGetFile() throws IOException, InterruptedException {
        File file = baseFolder.newFile("new_file_ho");
        String expectedStr = "HOLY SHIT!";
        DataOutputStream dout = new DataOutputStream(new FileOutputStream(file));
        dout.writeUTF(expectedStr);
        dout.close();
        final long expSize = FileUtils.sizeOf(file);

        Map<Integer, FtpFile> ftpFileMap = new ConcurrentHashMap<>();

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numClients; ++i) {
            FtpClient c = clients.get(i);
            int finalI = i;
            threads.add(new Thread(() -> {
                try {
                    FtpFile ftpFile = c.executeGet(file.getAbsolutePath());
                    ftpFileMap.put(finalI, ftpFile);
                } catch (IOException e) {
                    throw new IOError(e);
                }
            }));
            threads.get(threads.size() - 1).start();
        }

        for (Thread t : threads) {
            t.join();
        }

        for (int i = 0; i < numClients; ++i) {
            FtpFile ftpFile = ftpFileMap.get(i);
            Assert.assertEquals(expSize, ftpFile.getSize());
            DataInputStream in = new DataInputStream(ftpFile.getInputStream());
            String actual = in.readUTF();
            Assert.assertEquals(expectedStr, actual);
        }
    }
}
