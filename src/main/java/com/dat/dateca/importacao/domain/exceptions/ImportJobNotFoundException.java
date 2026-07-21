package com.dat.dateca.importacao.domain.exceptions;

public class ImportJobNotFoundException extends RuntimeException {
    public ImportJobNotFoundException(String message) {
        super(message);
    }
}
