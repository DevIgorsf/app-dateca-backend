package com.dat.dateca.importacao.domain.exceptions;

public class ImportJobStateConflictException extends RuntimeException {
    public ImportJobStateConflictException(String message) {
        super(message);
    }
}
