package ru.spbau.mit.java.wit.model;

/**
 * Created by: Egor Gorbunov
 * Date: 9/26/16
 * Email: egor-mailbox@ya.com
 */
public class Id {
    public final String sha;

    public Id(String sha) {
        this.sha = sha;
    }

    @Override
    public int hashCode() {
        return sha.hashCode();
    }
}
