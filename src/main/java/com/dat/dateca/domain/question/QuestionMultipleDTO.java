package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.course.CourseDTO;

public record QuestionMultipleDTO(
        Long id,
        String statement,
        PointsEnum pointsEnum,
        QuestionTypeEnum questionTypeEnum,
        String courseName,
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
                questionMultipleChoice.getStatement(),
                questionMultipleChoice.getPointsEnum(),
                questionMultipleChoice.getQuestionTypeEnum(),
                questionMultipleChoice.getCourse().getName(),
                questionMultipleChoice.getCorrectAnswer(),
                questionMultipleChoice.getAlternativeA(),
                questionMultipleChoice.getAlternativeB(),
                questionMultipleChoice.getAlternativeC(),
                questionMultipleChoice.getAlternativeD(),
                questionMultipleChoice.getAlternativeE()
        );
    }
}
