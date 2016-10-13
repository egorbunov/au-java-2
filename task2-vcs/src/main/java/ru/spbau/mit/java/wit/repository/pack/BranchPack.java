package ru.spbau.mit.java.wit.repository.pack;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import ru.spbau.mit.java.wit.model.Branch;
import ru.spbau.mit.java.wit.model.id.ShaId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by: Egor Gorbunov
 * Date: 9/29/16
 * Email: egor-mailbox@ya.com
 */
public class BranchPack {
    private BranchPack() {}

    public static InputStream pack(Branch branch) throws IOException {
        MessageBufferPacker p = MessagePack.newDefaultBufferPacker();

        p.packString(branch.getName());
        p.packString(branch.getHeadCommitId().toString());

        return new ByteArrayInputStream(p.toByteArray());

    }

    public static Branch unpack(InputStream in) throws IOException {
        MessageUnpacker u = MessagePack.newDefaultUnpacker(in);

        Branch b = new Branch();

        b.setName(u.unpackString());
        b.setHeadCommitId(ShaId.create(u.unpackString()));

        return b;
    }
}
