package com.dat.dateca.importacao.domain.ports;

import java.util.List;

public record QuestionExtractionResult(int number, String statement, List<AlternativeExtractionResult> alternatives,
                                        Character correctAnswer, boolean annulled, boolean needsReview,
                                        int sourcePageNumber) {
}
