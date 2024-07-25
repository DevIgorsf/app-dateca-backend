package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.CourseDTO;
import com.dat.dateca.domain.enade.ImageEnadeDTO;

import java.util.List;

public record QuestionMultipleChoiceRandDTO(
        Long id,
        String statement,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE,
        List<ImageQuestionDTO> images
) {
    public QuestionMultipleChoiceRandDTO(QuestionMultipleChoice questionMultipleChoice) {
        this(
                questionMultipleChoice.getId(),
                questionMultipleChoice.getStatement(),
                questionMultipleChoice.getAlternativeA(),
                questionMultipleChoice.getAlternativeB(),
                questionMultipleChoice.getAlternativeC(),
                questionMultipleChoice.getAlternativeD(),
                questionMultipleChoice.getAlternativeE(),
                questionMultipleChoice.getImages().stream().map(ImageQuestionDTO::new).toList()
        );
    }
}