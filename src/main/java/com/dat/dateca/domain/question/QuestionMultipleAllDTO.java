package com.dat.dateca.domain.question;

public record QuestionMultipleAllDTO(
    Long id,
    String statement,
    PointsEnum pointsEnum,
    QuestionTypeEnum questionTypeEnum,
    String course

) {
    public QuestionMultipleAllDTO(QuestionMultipleChoice questionMultipleChoice) {
        this(
                questionMultipleChoice.getId(),
                questionMultipleChoice.getStatement(),
                questionMultipleChoice.getPointsEnum(),
                questionMultipleChoice.getQuestionTypeEnum(),
                questionMultipleChoice.getCourse().getName()
        );
    }
}