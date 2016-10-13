package ru.spbau.mit.java.wit.test;

import org.junit.Assert;
import org.junit.Test;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;

/**
 * Created by: Egor Gorbunov
 * Date: 10/13/16
 * Email: egor-mailbox@ya.com
 */
public class IndexTest {
    @Test
    public void testAdd() {
        Index index = new Index();
        Index.Entry e = new Index.Entry("f", 1, ShaId.EmptyId, ShaId.EmptyId);
        index.add(e);
        Assert.assertTrue(index.contains(e));
        Assert.assertTrue(index.contains(e.fileName));
    }

    @Test
    public void testAddUpdate() {
        Index index = new Index();
        Index.Entry was = new Index.Entry("f", 1, ShaId.EmptyId, ShaId.EmptyId);
        index.add(was);
        ShaId newId = ShaId.create("2");
        index.addUpdate(was.fileName, was.modified + 1, newId);
        Index.Entry cur = index.getEntryByFile(was.fileName);
        Assert.assertEquals(was.modified + 1, cur.modified);
        Assert.assertEquals(newId, cur.curBlobId);
    }
}
