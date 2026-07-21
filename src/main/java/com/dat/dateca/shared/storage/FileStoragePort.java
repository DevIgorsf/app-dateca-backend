package com.dat.dateca.shared.storage;

public interface FileStoragePort {

    String store(byte[] content, String suggestedFilename);

    byte[] load(String storageKey);

    void delete(String storageKey);
}
