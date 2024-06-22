package com.dat.dateca.domain.question;

import java.util.List;

public record QuestionResultDTO(
        long questionTotal,
        long questionCorrect
) {
    public QuestionResultDTO(List<QuestionAnswerResult> questionAnswerResult) {
        this(
                questionAnswerResult.stream().count(),
                questionAnswerResult.stream().filter(QuestionAnswerResult::getCorrect).count()
        );
    }

}
