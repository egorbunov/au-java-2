package ru.spbau.mit.java.wit.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitInit;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */

public class InitTest {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    @Test
    public void testInit() throws IOException {
        Path baseDir = baseFolder.getRoot().toPath();
        WitInit init = new WitInit();
        init.execute(baseDir, null);
    }
}
