package com.dat.dateca.exam.application.dto;

import com.dat.dateca.exam.domain.ExamQuestion;

import java.util.List;
import java.util.UUID;

public record ExamQuestionView(UUID id, Integer number, String statement, Character correctAlternativeLabel,
                                List<ExamQuestionAlternativeView> alternatives, List<ExamQuestionImageView> images) {

    public static ExamQuestionView from(ExamQuestion question) {
        return new ExamQuestionView(question.getId(), question.getNumber(), question.getStatement(),
                question.getCorrectAlternativeLabel(),
                question.getAlternatives().stream().map(ExamQuestionAlternativeView::from).toList(),
                question.getImages().stream().map(ExamQuestionImageView::from).toList());
    }
}
