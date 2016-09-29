package ru.spbau.mit.java.wit.storage.pack;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import ru.spbau.mit.java.wit.model.Log;
import ru.spbau.mit.java.wit.model.ShaId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
public class LogStore {
    private LogStore() {}

    public static InputStream pack(Log log) throws IOException {
        MessageBufferPacker p = MessagePack.newDefaultBufferPacker();

        p.packArrayHeader(log.size());
        for (Log.Entry e : log) {
            p.packString(e.commitId.toString());
            p.packString(e.msg);
        }

        return new ByteArrayInputStream(p.toByteArray());
    }

    public static Log unpack(InputStream in) throws IOException {
        MessageUnpacker u = MessagePack.newDefaultUnpacker(in);

        Log log = new Log();

        int size = u.unpackArrayHeader();
        for (int i = 0; i < size; ++i) {
            ShaId id = new ShaId(u.unpackString());
            String msg = u.unpackString();
            log.add(new Log.Entry(id, msg));
        }

        return log;
    }
}
