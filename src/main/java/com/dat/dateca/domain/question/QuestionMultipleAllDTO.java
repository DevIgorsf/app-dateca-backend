package com.dat.dateca.domain.question;

public record QuestionMultipleAllDTO(
    Long id,
    String statement,
    String pointsEnum,
    String course

) {
    public QuestionMultipleAllDTO(QuestionMultipleChoice questionMultipleChoice) {
        this(
                questionMultipleChoice.getId(),
                questionMultipleChoice.getStatement(),
                questionMultipleChoice.getPointsEnum().getDescription(),
                questionMultipleChoice.getCourse().getName()
        );
    }
}