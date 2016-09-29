package ru.spbau.mit.java.wit.storage.pack;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import ru.spbau.mit.java.wit.model.ShaId;
import ru.spbau.mit.java.wit.model.Snapshot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
public class SnapshotStore {
    private SnapshotStore() {}

    public static InputStream pack(Snapshot snapshot) throws IOException {
        MessageBufferPacker p = MessagePack.newDefaultBufferPacker();

        p.packArrayHeader(snapshot.size());
        for (Snapshot.Entry e : snapshot) {
            p.packString(e.id.toString());
            p.packString(e.fileName);
        }

        return new ByteArrayInputStream(p.toByteArray());
    }

    public static Snapshot unpack(InputStream in) throws IOException {
        MessageUnpacker u = MessagePack.newDefaultUnpacker(in);

        Snapshot snapshot = new Snapshot();

        int size = u.unpackArrayHeader();
        for (int i = 0; i < size; ++i) {
            ShaId id = new ShaId(u.unpackString());
            String f = u.unpackString();
            snapshot.add(new Snapshot.Entry(id, f));
        }

        return snapshot;
    }
}
