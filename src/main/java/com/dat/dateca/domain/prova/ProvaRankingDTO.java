package com.dat.dateca.domain.prova;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProvaRankingDTO(
        int posicao,
        UUID studentId,
        String nomeAluno,
        int pontuacao,
        int acertos,
        LocalDateTime respondidoEm
) {
}
