package ru.spbau.mit.java.wit.model;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class ShaId {
    public static ShaId EmptyId = new ShaId("");

    public final String sha;

    public ShaId(String sha) {
        this.sha = sha;
    }

    @Override
    public int hashCode() {
        return sha.hashCode();
    }
}
