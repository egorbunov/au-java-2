package ru.spbau.mit.java.wit.model.id;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class ShaId {
    @Override
    public boolean equals(Object obj) {
        if (!ShaId.class.isInstance(obj)) {
            return false;
        }
        ShaId id = (ShaId) obj;
        return id.toString().equals(toString());
    }

    // Use it as null id
    public static final ShaId EmptyId = new ShaId("");

    private final String sha;

    public ShaId(String sha) {
        this.sha = sha;
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
