package com.dat.dateca.shared.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FilesystemFileStorageAdapter implements FileStoragePort {

    private final Path baseDirectory;

    public FilesystemFileStorageAdapter(@Value("${dateca.storage.base-dir:./dateca-storage}") String baseDir) {
        this.baseDirectory = Paths.get(baseDir);
        try {
            Files.createDirectories(baseDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível preparar o diretório de armazenamento: " + baseDirectory, e);
        }
    }

    @Override
    public String store(byte[] content, String suggestedFilename) {
        String storageKey = UUID.randomUUID() + extractExtension(suggestedFilename);
        Path target = baseDirectory.resolve(storageKey);
        try {
            Files.write(target, content);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar o arquivo: " + storageKey, e);
        }
        return storageKey;
    }

    @Override
    public byte[] load(String storageKey) {
        Path target = baseDirectory.resolve(storageKey);
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path target = baseDirectory.resolve(storageKey);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível remover o arquivo: " + storageKey, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx) : "";
    }
}
