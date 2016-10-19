package ru.spbau.mit.java;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by: Egor Gorbunov
 * Date: 10/19/16
 * Email: egor-mailbox@ya.com
 */
public class FtpOneClientTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();


    private FtpServer server;
    private FtpClient client;

    @Before
    public void setup() throws InterruptedException, IOException {
        server = new FtpServer(2000);
        server.start();

        Thread.sleep(100);

        client = new FtpClient("127.0.0.1", 2000);
        client.connect();
    }

    @After
    public void destroy() throws IOException {
        client.disconnect();
        server.stop();
    }


    @Test
    public void testConnectDisconnectStop() throws InterruptedException, IOException {
    }

    @Test
    public void testListFiles() throws IOException {
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

        List<FileInfo> fileInfos = client.executeList(dir.toString());

        List<String> actualDirs = fileInfos.stream().filter(FileInfo::isDirectory)
                        .map(f -> Paths.get(f.getName()).getFileName().toString())
                        .sorted().collect(Collectors.toList());
        List<String> actualFiles = fileInfos.stream().filter(x -> !x.isDirectory())
                .map(f -> Paths.get(f.getName()).getFileName().toString())
                .sorted().collect(Collectors.toList());


        Assert.assertThat(actualDirs, is(dirNames));
        Assert.assertThat(actualFiles, is(fNames));
    }

    @Test
    public void testGetFile() throws IOException {
        File file = baseFolder.newFile("new_file_ho");

        String expectedStr = "HOLY SHIT!";
        DataOutputStream dout = new DataOutputStream(new FileOutputStream(file));
        dout.writeUTF(expectedStr);
        dout.close();

        long expSize = FileUtils.sizeOf(file);

        FtpFile ftpFile = client.executeGet(file.getAbsolutePath());
        Assert.assertEquals(expSize, ftpFile.getSize());

        DataInputStream in = new DataInputStream(ftpFile.getInputStream());
        String actual = in.readUTF();
        Assert.assertEquals(expectedStr, actual);
    }
}
