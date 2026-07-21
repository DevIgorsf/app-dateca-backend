package com.dat.dateca.importacao.domain.ports;

public record CoverExtractionResult(String title, String institution, Integer year,
                                     String edition, String subjectArea, String rawText) {
}
