package com.dat.dateca.importacao.application.dto;

import com.dat.dateca.importacao.domain.QuestionDraft;

import java.util.List;
import java.util.UUID;

public record QuestionDraftView(UUID id, Integer number, String statement, boolean annulled,
                                 Character correctAlternativeLabel, Integer sourcePageNumber, boolean needsReview,
                                 List<AlternativeDraftView> alternatives, List<QuestionImageView> images) {

    public static QuestionDraftView from(QuestionDraft question) {
        return new QuestionDraftView(question.getId(), question.getNumber(), question.getStatement(),
                question.isAnnulled(), question.getCorrectAlternativeLabel(), question.getSourcePageNumber(),
                question.isNeedsReview(),
                question.getAlternatives().stream().map(AlternativeDraftView::from).toList(),
                question.getImages().stream().map(QuestionImageView::from).toList());
    }
}
