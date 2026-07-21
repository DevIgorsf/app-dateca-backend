package com.dat.dateca.importacao.domain.ports;

public interface ExamPublicationPort {

    PublishedExamResult publish(ExamPublicationRequest request);
}
