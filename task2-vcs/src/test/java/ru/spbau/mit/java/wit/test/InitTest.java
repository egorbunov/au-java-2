package ru.spbau.mit.java.wit.test;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.command.WitInit;

import java.nio.file.Path;

/**
 * Created by: Egor Gorbunov
 * Date: 9/30/16
 * Email: egor-mailbox@ya.com
 */

public class InitTest {
    @Rule
    public TemporaryFolder baseFolder = new TemporaryFolder();

    @Test
    public void testInit() {
        Path baseDir = baseFolder.getRoot().toPath();
        WitInit init = new WitInit();
        init.execute(baseDir, null);
    }
}
