package ru.spbau.mit.java.wit.repository.pack;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import ru.spbau.mit.java.wit.model.id.ShaId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
public class IdListPack {
    private IdListPack() {}

    public static InputStream pack(List<ShaId> ids) throws IOException {
        MessageBufferPacker p = MessagePack.newDefaultBufferPacker();

        p.packArrayHeader(ids.size());
        for (ShaId id : ids) {
            p.packString(id.toString());
        }

        return new ByteArrayInputStream(p.toByteArray());
    }

    public static List<ShaId> unpack(InputStream in) throws IOException {
        MessageUnpacker u = MessagePack.newDefaultUnpacker(in);

        List<ShaId> log = new ArrayList<>();

        int size = u.unpackArrayHeader();
        for (int i = 0; i < size; ++i) {
            log.add(ShaId.create(u.unpackString()));
        }

        return log;
    }
}
