package com.dat.dateca.importacao.application;

import com.dat.dateca.importacao.application.dto.PublishResult;
import com.dat.dateca.importacao.application.dto.UpdateExamDraftCommand;
import com.dat.dateca.importacao.domain.AlternativeDraft;
import com.dat.dateca.importacao.domain.ExamDraft;
import com.dat.dateca.importacao.domain.ImportJob;
import com.dat.dateca.importacao.domain.QuestionDraft;
import com.dat.dateca.importacao.domain.exceptions.ImportJobStateConflictException;
import com.dat.dateca.importacao.domain.exceptions.PublishValidationException;
import com.dat.dateca.importacao.domain.exceptions.UnsupportedFileTypeException;
import com.dat.dateca.importacao.domain.ports.ExamPublicationPort;
import com.dat.dateca.importacao.domain.ports.ImportJobDispatcher;
import com.dat.dateca.importacao.domain.ports.PdfTextExtractionPort;
import com.dat.dateca.importacao.domain.ports.PublishedExamResult;
import com.dat.dateca.importacao.infrastructure.persistence.ImportJobRepository;
import com.dat.dateca.shared.storage.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportJobApplicationServiceTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private PdfTextExtractionPort pdfTextExtractionPort;

    @Mock
    private ImportJobDispatcher importJobDispatcher;

    @Mock
    private ExamPublicationPort examPublicationPort;

    private ImportJobApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ImportJobApplicationService(
                importJobRepository, fileStoragePort, pdfTextExtractionPort, importJobDispatcher, examPublicationPort);
    }

    @Test
    void startImportRejectsNonPdfFile() {
        MockMultipartFile file = new MockMultipartFile("file", "prova.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "conteúdo".getBytes());

        assertThatThrownBy(() -> service.startImport(file, 1L))
                .isInstanceOf(UnsupportedFileTypeException.class);

        verify(importJobDispatcher, never()).dispatch(any());
    }

    @Test
    void startImportRejectsFileLargerThan20Mb() {
        byte[] tooLarge = new byte[21 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "prova.pdf", "application/pdf", tooLarge);

        assertThatThrownBy(() -> service.startImport(file, 1L))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    void startImportRejectsPdfWithTooManyPages() {
        MockMultipartFile file = new MockMultipartFile("file", "prova.pdf", "application/pdf", "conteudo-pdf".getBytes());
        lenient().when(pdfTextExtractionPort.countPages(any())).thenReturn(61);

        assertThatThrownBy(() -> service.startImport(file, 1L))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    void startImportStoresFileCreatesJobAndDispatchesProcessing() {
        MockMultipartFile file = new MockMultipartFile("file", "prova.pdf", "application/pdf", "conteudo-pdf".getBytes());
        when(pdfTextExtractionPort.countPages(any())).thenReturn(10);
        when(fileStoragePort.store(any(), any())).thenReturn("storage-key-123");

        var result = service.startImport(file, 7L);

        assertThat(result.status()).isEqualTo(com.dat.dateca.importacao.domain.ImportStatus.PENDENTE);
        verify(importJobRepository).save(any(ImportJob.class));
        verify(importJobDispatcher).dispatch(any());
    }

    @Test
    void getDraftThrowsWhenNotYetReadyForReview() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.getDraft(jobId))
                .isInstanceOf(ImportJobStateConflictException.class);
    }

    @Test
    void publishThrowsWhenJobNotReadyForReview() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.publish(jobId, 1L))
                .isInstanceOf(ImportJobStateConflictException.class);

        verify(examPublicationPort, never()).publish(any());
    }

    @Test
    void publishThrowsWhenDraftHasNoQuestions() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "key", 1L);
        job.attachDraft(new ExamDraft("Prova", "Instituição", 2025, "Ed 1", "Geral", null));
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.publish(jobId, 1L))
                .isInstanceOf(PublishValidationException.class);
    }

    @Test
    void publishThrowsWhenQuestionHasFewerThanTwoAlternatives() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "key", 1L);
        ExamDraft draft = new ExamDraft("Prova", "Instituição", 2025, "Ed 1", "Geral", null);
        QuestionDraft question = new QuestionDraft(1, "Enunciado", false, 'A', 1, false, 0);
        question.addAlternative(new AlternativeDraft('A', "Única alternativa", 0));
        draft.addQuestion(question);
        job.attachDraft(draft);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.publish(jobId, 1L))
                .isInstanceOf(PublishValidationException.class);
    }

    @Test
    void publishSucceedsAndMarksJobAsPublicado() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "key", 1L);
        ExamDraft draft = new ExamDraft("Prova", "Instituição", 2025, "Ed 1", "Geral", null);
        QuestionDraft question = new QuestionDraft(1, "Enunciado válido", false, 'A', 1, false, 0);
        question.addAlternative(new AlternativeDraft('A', "Primeira", 0));
        question.addAlternative(new AlternativeDraft('B', "Segunda", 1));
        draft.addQuestion(question);
        job.attachDraft(draft);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        UUID examId = UUID.randomUUID();
        when(examPublicationPort.publish(any())).thenReturn(new PublishedExamResult(examId));

        PublishResult result = service.publish(jobId, 9L);

        assertThat(result.examId()).isEqualTo(examId);
        assertThat(job.getStatus()).isEqualTo(com.dat.dateca.importacao.domain.ImportStatus.PUBLICADO);
        assertThat(job.getPublishedExamId()).isEqualTo(examId);
    }

    @Test
    void updateDraftThrowsWhenJobNotReadyForReview() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        UpdateExamDraftCommand command = new UpdateExamDraftCommand("Título", "Inst", 2025, "Ed", "Geral", List.of());

        assertThatThrownBy(() -> service.updateDraft(jobId, command))
                .isInstanceOf(ImportJobStateConflictException.class);
    }

    @Test
    void cancelThrowsWhenJobAlreadyPublished() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "key", 1L);
        job.markPublished(UUID.randomUUID());
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.cancel(jobId))
                .isInstanceOf(ImportJobStateConflictException.class);
    }
}
