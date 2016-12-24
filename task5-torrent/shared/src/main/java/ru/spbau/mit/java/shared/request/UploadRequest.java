package ru.spbau.mit.java.shared.request;

import ru.spbau.mit.java.shared.response.UploadResponse;

import java.util.Objects;

/**
 * Request, with which clients tells to tracker, that
 * he want's to make available specified file for downloading
 */
public class UploadRequest {
    public static final byte code = 2;

    private final String name;
    private final int size;

    public UploadRequest(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "upload { name: " + name + ", size = " + size + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UploadRequest) {
            UploadRequest r = (UploadRequest) obj;
            return r.size == size && Objects.equals(r.name, name);
        }
        return false;
    }
}
