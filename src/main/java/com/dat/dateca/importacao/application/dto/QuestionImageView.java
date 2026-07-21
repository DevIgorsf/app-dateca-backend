package com.dat.dateca.importacao.application.dto;

import com.dat.dateca.importacao.domain.QuestionImageDraft;

import java.util.UUID;

public record QuestionImageView(UUID id, String contentType) {

    public static QuestionImageView from(QuestionImageDraft image) {
        return new QuestionImageView(image.getId(), image.getContentType());
    }
}
