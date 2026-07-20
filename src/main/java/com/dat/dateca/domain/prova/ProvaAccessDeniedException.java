package com.dat.dateca.domain.prova;

public class ProvaAccessDeniedException extends RuntimeException {
    public ProvaAccessDeniedException() {
        super("Você não tem permissão para acessar esta prova");
    }
}
