package com.dat.dateca.domain.question;

public record QuestionMultipleAllDTO(
    Long id,
    String statement,
    String pointsEnum,
    QuestionTypeEnum questionTypeEnum,
    String course

) {
    public QuestionMultipleAllDTO(QuestionMultipleChoice questionMultipleChoice) {
        this(
                questionMultipleChoice.getId(),
                questionMultipleChoice.getStatement(),
                questionMultipleChoice.getPointsEnum().getDescription(),
                questionMultipleChoice.getQuestionTypeEnum(),
                questionMultipleChoice.getCourse().getName()
        );
    }
}