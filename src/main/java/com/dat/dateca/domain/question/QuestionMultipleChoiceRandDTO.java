package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.CourseDTO;

import java.util.List;

public record QuestionMultipleChoiceRandDTO(
        Long id,
        List<Long> idImages,
        String statement,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE
) {
    public QuestionMultipleChoiceRandDTO(QuestionMultipleChoice questionMultipleChoice) {
        this(
                questionMultipleChoice.getId(),
                questionMultipleChoice.getIdImages(),
                questionMultipleChoice.getStatement(),
                questionMultipleChoice.getAlternativeA(),
                questionMultipleChoice.getAlternativeB(),
                questionMultipleChoice.getAlternativeC(),
                questionMultipleChoice.getAlternativeD(),
                questionMultipleChoice.getAlternativeE()
        );
    }
}