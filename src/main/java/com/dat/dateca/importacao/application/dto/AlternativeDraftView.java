package com.dat.dateca.importacao.application.dto;

import com.dat.dateca.importacao.domain.AlternativeDraft;

import java.util.UUID;

public record AlternativeDraftView(UUID id, Character label, String text) {

    public static AlternativeDraftView from(AlternativeDraft alternative) {
        return new AlternativeDraftView(alternative.getId(), alternative.getLabel(), alternative.getAlternativeText());
    }
}
