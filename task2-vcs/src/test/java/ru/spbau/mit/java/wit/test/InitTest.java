package ru.spbau.mit.java.wit.test;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.spbau.mit.java.wit.storage.WitInit;

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
        Path witRoot1 = WitInit.init(baseFolder.getRoot().toPath());
        Path witRoot2 = WitInit.findRepositoryRoot(baseFolder.getRoot().toPath());
        Assert.assertEquals(witRoot1, witRoot2);
    }
}
