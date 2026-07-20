package com.dat.dateca.domain.prova;

import java.util.UUID;

public record ProvaQuestaoAlunoDTO(
        UUID id,
        String statement,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE,
        int ordem
) {
    public ProvaQuestaoAlunoDTO(ProvaQuestao questao) {
        this(
                questao.getId(),
                questao.getStatement(),
                questao.getAlternativeA(),
                questao.getAlternativeB(),
                questao.getAlternativeC(),
                questao.getAlternativeD(),
                questao.getAlternativeE(),
                questao.getOrdem()
        );
    }
}
