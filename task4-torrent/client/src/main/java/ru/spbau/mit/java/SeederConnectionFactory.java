package ru.spbau.mit.java;

public interface SeederConnectionFactory<T> {
    SeederConnection getSeederConnection(T clientId);
}
