package com.dat.dateca.importacao.domain.ports;

import java.util.List;
import java.util.Map;

public interface VisionExtractionPort {

    CoverExtractionResult extractCover(List<PageInput> coverPages);

    List<QuestionExtractionResult> extractQuestions(List<PageInput> chunkPages);

    Map<Integer, Character> extractAnswerKey(List<PageInput> answerKeyPages);
}
