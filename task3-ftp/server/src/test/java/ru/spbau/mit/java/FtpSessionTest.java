package ru.spbau.mit.java;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class FtpSessionTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    private DataInputStream dataIn;
    private ByteArrayOutputStream dataOut;
    private ByteArrayOutputStream statusOut;

    @Before
    public void setup() {
        dataOut = new ByteArrayOutputStream();
        statusOut = new ByteArrayOutputStream();
    }

    @Test
    public void testListDir() throws IOException {
        Path dir = baseFolder.getRoot().toPath().toAbsolutePath();

        List<String> fNames = Arrays.asList("3.txt", "1.txt", "2.txt");
        fNames.sort(null);
        for (String f : fNames) {
            baseFolder.newFile(f);
        }
        List<String> dirNames = Arrays.asList("c", "a", "b");
        dirNames.sort(null);
        for (String d : dirNames) {
            baseFolder.newFolder(d);
        }

        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        DataOutputStream request = new DataOutputStream(requestBytes);
        request.writeInt(Command.LIST_FILES);
        request.writeUTF(dir.toString());
        dataIn = new DataInputStream(new ByteArrayInputStream(requestBytes.toByteArray()));

        FtpSession ftpSession = new FtpSession(dataIn, dataOut, statusOut);
        ftpSession.run();

        DataInputStream responseIn =
                new DataInputStream(new ByteArrayInputStream(dataOut.toByteArray()));
        DataInputStream statusIn =
                new DataInputStream(new ByteArrayInputStream(statusOut.toByteArray()));

        int status = statusIn.readInt();
        Assert.assertEquals(ResponseCode.OK, status);
        int size = responseIn.readInt();
        Assert.assertEquals(fNames.size() + dirNames.size(), size);

        List<String> actualFiles = new ArrayList<>();
        List<String> actualDirs = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            String fname = responseIn.readUTF();
            boolean isDir = responseIn.readBoolean();
            if (isDir) {
                actualDirs.add(Paths.get(fname).getFileName().toString());
            } else {
                actualFiles.add(Paths.get(fname).getFileName().toString());
            }
        }
        actualDirs.sort(null);
        actualFiles.sort(null);

        Assert.assertThat(actualDirs, is(dirNames));
        Assert.assertThat(actualFiles, is(fNames));
    }
}
