package com.dat.dateca.importacao.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ImportJobTest {

    @Test
    void newImportJobStartsAsPendente() {
        ImportJob job = new ImportJob("prova.pdf", 1024L, "storage-key", 42L);

        assertThat(job.getStatus()).isEqualTo(ImportStatus.PENDENTE);
        assertThat(job.isReadyForReview()).isFalse();
        assertThat(job.isPublished()).isFalse();
    }

    @Test
    void attachDraftMovesJobToAguardandoRevisao() {
        ImportJob job = new ImportJob("prova.pdf", 1024L, "storage-key", 42L);
        ExamDraft draft = new ExamDraft("Prova Teste", "Instituição", 2025, "Ed 1", "Geral", "raw");

        job.attachDraft(draft);

        assertThat(job.getStatus()).isEqualTo(ImportStatus.AGUARDANDO_REVISAO);
        assertThat(job.isReadyForReview()).isTrue();
        assertThat(job.getExamDraft()).isSameAs(draft);
        assertThat(draft.getImportJob()).isSameAs(job);
    }

    @Test
    void markErrorMovesJobToErroWithMessage() {
        ImportJob job = new ImportJob("prova.pdf", 1024L, "storage-key", 42L);

        job.markError("falha ao ler o PDF");

        assertThat(job.getStatus()).isEqualTo(ImportStatus.ERRO);
        assertThat(job.getErrorMessage()).isEqualTo("falha ao ler o PDF");
    }

    @Test
    void markPublishedStoresExamIdAndMarksPublished() {
        ImportJob job = new ImportJob("prova.pdf", 1024L, "storage-key", 42L);
        UUID examId = UUID.randomUUID();

        job.markPublished(examId);

        assertThat(job.getStatus()).isEqualTo(ImportStatus.PUBLICADO);
        assertThat(job.getPublishedExamId()).isEqualTo(examId);
        assertThat(job.isPublished()).isTrue();
    }

    @Test
    void markCancelledSetsDeletedAt() {
        ImportJob job = new ImportJob("prova.pdf", 1024L, "storage-key", 42L);

        job.markCancelled();

        assertThat(job.getStatus()).isEqualTo(ImportStatus.CANCELADO);
        assertThat(job.getDeletedAt()).isNotNull();
    }
}
