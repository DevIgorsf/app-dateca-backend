package com.dat.dateca.importacao.domain.exceptions;

public class CorruptedPdfException extends RuntimeException {
    public CorruptedPdfException(String message) {
        super(message);
    }
}
