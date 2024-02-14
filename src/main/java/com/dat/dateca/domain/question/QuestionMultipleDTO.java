package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.course.CourseDTO;

import java.util.List;

public record QuestionMultipleDTO(
        Long id,

        List<Long> idImages,
        String statement,
        String pointsEnum,
        CourseDTO course,
        Character correctAnswer,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE
) {
    public QuestionMultipleDTO(QuestionMultipleChoice questionMultipleChoice) {
        this(
                questionMultipleChoice.getId(),
                questionMultipleChoice.getIdImages(),
                questionMultipleChoice.getStatement(),
                questionMultipleChoice.getPointsEnum().getDescription(),
                questionMultipleChoice.getCourseDTO(),
                questionMultipleChoice.getCorrectAnswer(),
                questionMultipleChoice.getAlternativeA(),
                questionMultipleChoice.getAlternativeB(),
                questionMultipleChoice.getAlternativeC(),
                questionMultipleChoice.getAlternativeD(),
                questionMultipleChoice.getAlternativeE()
        );
    }
}
