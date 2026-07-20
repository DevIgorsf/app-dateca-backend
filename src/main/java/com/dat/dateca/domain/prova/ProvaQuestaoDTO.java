package com.dat.dateca.domain.prova;

import java.util.UUID;

public record ProvaQuestaoDTO(
        UUID id,
        String statement,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE,
        Character correctAnswer,
        String comment,
        int ordem
) {
    public ProvaQuestaoDTO(ProvaQuestao questao) {
        this(
                questao.getId(),
                questao.getStatement(),
                questao.getAlternativeA(),
                questao.getAlternativeB(),
                questao.getAlternativeC(),
                questao.getAlternativeD(),
                questao.getAlternativeE(),
                questao.getCorrectAnswer(),
                questao.getComment(),
                questao.getOrdem()
        );
    }
}
