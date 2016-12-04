package ru.spbau.mit.java.protocol.response;


import java.util.Collection;
import java.util.stream.Collectors;

public class StatResponse {
    private final Collection<Integer> partIds;

    public StatResponse(Collection<Integer> partIds) {
        this.partIds = partIds;
    }

    public Collection<Integer> getPartIds() {
        return partIds;
    }

    @Override
    public String toString() {
        return "{" + partIds.stream().map(Object::toString).collect(Collectors.joining(", ")) + "}";
    }
}
