package com.dat.dateca.importacao.domain.exceptions;

public class ExtractionFailedException extends RuntimeException {
    public ExtractionFailedException(String message) {
        super(message);
    }
}
