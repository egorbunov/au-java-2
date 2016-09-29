package ru.spbau.mit.java.wit.model;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class ShaId {
    // Use it as null id
    public static ShaId EmptyId = new ShaId("");

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
