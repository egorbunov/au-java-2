package ru.spbau.mit.java.shared.response;


import ru.spbau.mit.java.shared.tracker.ClientId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SourcesResponse {
    private final List<ClientId> clients;

    public SourcesResponse(List<ClientId> clients) {
        this.clients = clients;
    }

    public List<ClientId> getClients() {
        return clients;
    }

    @Override
    public String toString() {
        return "[" + clients.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
