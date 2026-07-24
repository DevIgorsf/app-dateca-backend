package com.dat.dateca.importacao.domain.ports;

import java.util.List;
import java.util.Map;

public interface VisionExtractionPort {

    CoverExtractionResult extractCover(List<PageInput> coverPages);

    /**
     * Extrai as questões de um bloco de páginas. O retorno sinaliza truncamento para que o
     * orquestrador possa reprocessar o bloco em partes menores — ver
     * {@link com.dat.dateca.importacao.application.PdfImportProcessor}.
     */
    QuestionExtractionBatch extractQuestions(List<PageInput> chunkPages);

    Map<Integer, Character> extractAnswerKey(List<PageInput> answerKeyPages);
}
