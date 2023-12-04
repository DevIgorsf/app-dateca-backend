package com.dat.dateca.domain.question;

public record QuestionAnswerDTO(
        Character answerCorrect,
        Character answer,
        Boolean result
) {
    public QuestionAnswerDTO (Character answerCorrect, Character answer, Boolean result) {
        this.answerCorrect = answerCorrect;
        this.answer = answer;
        this.result = result;
    }
}
