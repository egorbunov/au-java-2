package ru.spbau.mit.java.shared.response;


import ru.spbau.mit.java.shared.tracker.ClientId;

import java.util.Collection;

public class SourcesResponse {
    private final Collection<ClientId> clients;

    public SourcesResponse(Collection<ClientId> clients) {
        this.clients = clients;
    }

    public Collection<ClientId> getClients() {
        return clients;
    }
}
