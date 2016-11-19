package ru.spbau.mit.java.shared;

import java.io.IOException;

public interface OneClientRequestServer {
    void serveOneRequest();
    void disconnect() throws IOException;
}
