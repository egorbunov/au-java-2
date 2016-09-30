package ru.spbau.mit.java.wit.repository.pack;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import ru.spbau.mit.java.wit.model.Index;
import ru.spbau.mit.java.wit.model.id.ShaId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
public class IndexPack {
    private IndexPack() {}

    public static InputStream pack(Index index) throws IOException {
        MessageBufferPacker p = MessagePack.newDefaultBufferPacker();

        p.packArrayHeader(index.size());
        for (Index.Entry e : index) {
            p.packString(e.fileName);
            p.packString(e.lastCommitedBlobId.toString());
            p.packString(e.curBlobId.toString());
            p.packLong(e.lastModified);
        }

        return new ByteArrayInputStream(p.toByteArray());
    }

    public static Index unpack(InputStream in) throws IOException {
        MessageUnpacker u = MessagePack.newDefaultUnpacker(in);

        Index index = new Index();

        int size = u.unpackArrayHeader();
        for (int i = 0; i < size; ++i) {
            String f = u.unpackString();
            ShaId lid = new ShaId(u.unpackString());
            ShaId cid = new ShaId(u.unpackString());
            long lm = u.unpackLong();
            index.add(new Index.Entry(f, lm, cid, lid));
        }

        return index;
    }
}
