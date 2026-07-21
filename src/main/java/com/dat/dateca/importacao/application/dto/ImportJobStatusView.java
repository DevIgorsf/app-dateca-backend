package com.dat.dateca.importacao.application.dto;

import com.dat.dateca.importacao.domain.ImportJob;
import com.dat.dateca.importacao.domain.ImportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ImportJobStatusView(UUID id, ImportStatus status, String currentStep, String errorMessage,
                                   UUID publishedExamId, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static ImportJobStatusView from(ImportJob job) {
        return new ImportJobStatusView(job.getId(), job.getStatus(), job.getCurrentStep(), job.getErrorMessage(),
                job.getPublishedExamId(), job.getCreatedAt(), job.getUpdatedAt());
    }
}
