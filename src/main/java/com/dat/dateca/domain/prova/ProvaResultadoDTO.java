package com.dat.dateca.domain.prova;

import java.util.List;

public record ProvaResultadoDTO(
        int pontuacao,
        int totalQuestoes,
        int acertos,
        List<ProvaResultadoItemDTO> gabarito
) {
}
