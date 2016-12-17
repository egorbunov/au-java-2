package ru.spbau.mit.java.shared;

import java.io.IOException;

public interface OneClientRequestServer {
    void serveOneRequest() throws IOException;
    void disconnect() throws IOException;
}
