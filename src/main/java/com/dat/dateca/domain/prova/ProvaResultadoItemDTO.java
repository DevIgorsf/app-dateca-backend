package com.dat.dateca.domain.prova;

import java.util.UUID;

public record ProvaResultadoItemDTO(
        UUID provaQuestaoId,
        Character correctAnswer,
        Character respostaEscolhida,
        boolean correta,
        String comment
) {
}
