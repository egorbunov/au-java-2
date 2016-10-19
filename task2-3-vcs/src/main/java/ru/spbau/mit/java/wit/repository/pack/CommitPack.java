package ru.spbau.mit.java.wit.repository.pack;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import ru.spbau.mit.java.wit.model.Commit;
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
public class CommitPack {
    private CommitPack() {}

    public static InputStream pack(Commit commit) throws IOException {
        MessageBufferPacker p = MessagePack.newDefaultBufferPacker();

        p.packArrayHeader(commit.getParentCommitsIds().size());
        for (ShaId id : commit.getParentCommitsIds()) {
            p.packString(id.toString());
        }
        p.packString(commit.getSnapshotId().toString());
        p.packString(commit.getMsg());
        p.packLong(commit.getTimestamp());

        return new ByteArrayInputStream(p.toByteArray());
    }

    public static Commit unpack(InputStream in) throws IOException {
        MessageUnpacker u = MessagePack.newDefaultUnpacker(in);

        Commit commit = new Commit();

        int size = u.unpackArrayHeader();
        List<ShaId> parentIds = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            parentIds.add(ShaId.create(u.unpackString()));
        }
        commit.setParentCommitsIds(parentIds);
        commit.setSnapshotId(ShaId.create(u.unpackString()));
        commit.setMsg(u.unpackString());
        commit.setTimestamp(u.unpackLong());

        return commit;
    }
}
