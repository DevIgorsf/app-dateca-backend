package com.dat.dateca.importacao.domain;

public enum ImportStatus {
    PENDENTE,
    EXTRAINDO_TEXTO,
    EXTRAINDO_IA,
    AGUARDANDO_REVISAO,
    PUBLICADO,
    ERRO,
    CANCELADO
}
