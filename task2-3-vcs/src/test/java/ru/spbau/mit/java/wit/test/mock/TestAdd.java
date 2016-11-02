package ru.spbau.mit.java.wit.test.mock;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import ru.spbau.mit.java.wit.command.WitAdd;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.repository.WitUtils;
import ru.spbau.mit.java.wit.repository.storage.WitStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class TestAdd {
    @Rule
    public final TemporaryFolder baseFolder = new TemporaryFolder();

    @Test
    public void testAddOneFile() throws IOException {
        File fileToAdd = baseFolder.newFile();
        Path base = baseFolder.getRoot().toPath();
        WitAdd addCmd = new WitAdd();
        addCmd.setFileNames(Collections.singletonList(fileToAdd.toString()));

        WitStorage witStorage = mock(WitStorage.class);
        when(witStorage.getWitRoot())
                .thenReturn(WitUtils.resolveStoragePath(base));
        when(witStorage.readIndex())
                .thenReturn(new Index());

        addCmd.execute(Paths.get(""), witStorage);

        ArgumentCaptor<Index> arg = ArgumentCaptor.forClass(Index.class);
        verify(witStorage).writeIndex(arg.capture());

        Assert.assertEquals(arg.getAllValues().size(), 1);

        Index actualIndex = arg.getValue();
        Assert.assertEquals(actualIndex.size(), 1);
        Assert.assertTrue(actualIndex.contains(base.relativize(fileToAdd.toPath()).toString()));
    }
}
