package com.dat.dateca.domain.prova;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProvaResponderForm(
        @NotEmpty @Valid List<ProvaRespostaForm> respostas
) {
}
