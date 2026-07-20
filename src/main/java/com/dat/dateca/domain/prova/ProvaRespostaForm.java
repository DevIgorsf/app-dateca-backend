package com.dat.dateca.domain.prova;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProvaRespostaForm(
        @NotNull UUID provaQuestaoId,
        @NotNull Character respostaEscolhida
) {
}
