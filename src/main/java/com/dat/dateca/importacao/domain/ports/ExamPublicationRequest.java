package com.dat.dateca.importacao.domain.ports;

import java.util.List;
import java.util.UUID;

public record ExamPublicationRequest(UUID sourceImportJobId, String title, String institution, Integer year,
                                      String edition, String subjectArea, Long publishedByUserId,
                                      List<QuestionPublicationData> questions) {

    public record QuestionPublicationData(int number, String statement, Character correctAlternativeLabel,
                                           Integer sourcePageNumber, int ordem,
                                           List<AlternativePublicationData> alternatives,
                                           List<ImagePublicationData> images) {
    }

    public record AlternativePublicationData(char label, String text, int ordem) {
    }

    public record ImagePublicationData(String storageKey, String contentType, int ordem) {
    }
}
