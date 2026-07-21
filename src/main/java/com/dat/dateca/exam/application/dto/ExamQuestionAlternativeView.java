package com.dat.dateca.exam.application.dto;

import com.dat.dateca.exam.domain.ExamQuestionAlternative;

public record ExamQuestionAlternativeView(Character label, String text) {

    public static ExamQuestionAlternativeView from(ExamQuestionAlternative alternative) {
        return new ExamQuestionAlternativeView(alternative.getLabel(), alternative.getAlternativeText());
    }
}
