package ru.spbau.mit.java.wit.model.id;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */

/**
 * Class represents hash identifier (id) for objects,
 * which are stored in repository. Blob files, commits
 * and snapshots have their own ids.
 */
public class ShaId {
    // Use it as null id
    public static final ShaId EmptyId = new ShaId("");

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!ShaId.class.isInstance(obj)) {
            return false;
        }
        ShaId id = (ShaId) obj;
        return id.toString().equals(toString());
    }

    private final String sha;

    private ShaId(String sha) {
        this.sha = sha;
    }

    public static ShaId create(String sha) {
        if (sha == null || sha.isEmpty()) {
            return EmptyId;
        }
        return new ShaId(sha);
    }

    @Override

    public int hashCode() {
        return sha.hashCode();
    }

    @Override
    public String toString() {
        return sha;
    }
}
