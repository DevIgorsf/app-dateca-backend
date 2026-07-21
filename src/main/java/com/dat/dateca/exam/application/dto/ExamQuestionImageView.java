package com.dat.dateca.exam.application.dto;

import com.dat.dateca.exam.domain.ExamQuestionImage;

import java.util.UUID;

public record ExamQuestionImageView(UUID id, String contentType) {

    public static ExamQuestionImageView from(ExamQuestionImage image) {
        return new ExamQuestionImageView(image.getId(), image.getContentType());
    }
}
