package com.dat.dateca.importacao.domain.ports;

import java.util.List;

/**
 * Resultado da extração de um bloco de páginas. Além das questões, carrega a informação de que a
 * resposta do modelo foi cortada por limite de tokens — sem isso o pipeline leria uma resposta
 * truncada como se estivesse completa e perderia questões silenciosamente.
 */
public record QuestionExtractionBatch(List<QuestionExtractionResult> questions, boolean truncated) {

    public static QuestionExtractionBatch complete(List<QuestionExtractionResult> questions) {
        return new QuestionExtractionBatch(questions, false);
    }

    public static QuestionExtractionBatch truncated(List<QuestionExtractionResult> questions) {
        return new QuestionExtractionBatch(questions, true);
    }
}
