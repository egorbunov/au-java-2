package ru.spbau.mit.java.protocol.response;


import java.util.Collection;

public class StatResponse {
    private final Collection<Integer> partIds;

    public StatResponse(Collection<Integer> partIds) {
        this.partIds = partIds;
    }

    public Collection<Integer> getPartIds() {
        return partIds;
    }
}
