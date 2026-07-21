package com.dat.dateca.importacao.application;

import com.dat.dateca.importacao.domain.ImportJob;
import com.dat.dateca.importacao.domain.ImportStatus;
import com.dat.dateca.importacao.domain.ports.AlternativeExtractionResult;
import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.ExtractedImage;
import com.dat.dateca.importacao.domain.ports.PageContent;
import com.dat.dateca.importacao.domain.ports.PdfTextExtractionPort;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionResult;
import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;
import com.dat.dateca.importacao.infrastructure.persistence.ImportJobRepository;
import com.dat.dateca.shared.storage.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfImportProcessorTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private PdfTextExtractionPort pdfTextExtractionPort;

    @Mock
    private VisionExtractionPort visionExtractionPort;

    @Mock
    private FileStoragePort fileStoragePort;

    private PdfImportProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PdfImportProcessor(importJobRepository, pdfTextExtractionPort, visionExtractionPort, fileStoragePort);
    }

    @Test
    void processDoesNothingWhenJobWasRemoved() {
        UUID jobId = UUID.randomUUID();
        when(importJobRepository.findById(jobId)).thenReturn(Optional.empty());

        processor.process(jobId);

        // não deve lançar exceção nem tentar carregar o PDF de um job inexistente
    }

    @Test
    void processMarksJobAsErroWhenStorageFails() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "storage-key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(fileStoragePort.load("storage-key")).thenThrow(new IllegalStateException("disco indisponível"));

        processor.process(jobId);

        assertThat(job.getStatus()).isEqualTo(ImportStatus.ERRO);
        assertThat(job.getErrorMessage()).contains("disco indisponível");
    }

    @Test
    void processAssemblesDraftAndMergesQuestionSplitAcrossOverlappingChunks() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "storage-key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        byte[] pdfBytes = "pdf-bytes".getBytes();
        when(fileStoragePort.load("storage-key")).thenReturn(pdfBytes);
        when(pdfTextExtractionPort.countPages(pdfBytes)).thenReturn(6);

        for (int page = 1; page <= 6; page++) {
            when(pdfTextExtractionPort.extractPage(pdfBytes, page))
                    .thenReturn(new PageContent(page, "texto da página " + page, true, false, false));
        }
        lenient().when(pdfTextExtractionPort.extractEmbeddedImages(any(), anyInt())).thenReturn(List.of());

        when(visionExtractionPort.extractCover(any()))
                .thenReturn(new CoverExtractionResult("Prova Teste", "Instituição X", 2025, "Ed 1", "Geral", "raw"));

        // bloco 1 (páginas 1-5): questão 1 completa e questão 2 com apenas 1 alternativa (cortada na borda do bloco)
        QuestionExtractionResult question1 = new QuestionExtractionResult(1, "Enunciado da questão 1",
                List.of(new AlternativeExtractionResult('A', "Alt A"), new AlternativeExtractionResult('B', "Alt B")),
                'A', false, false, 1);
        QuestionExtractionResult question2Partial = new QuestionExtractionResult(2, "Enunciado cortado",
                List.of(new AlternativeExtractionResult('A', "Só uma alternativa")), null, false, true, 5);

        // bloco 2 (páginas 5-6, sobreposição): questão 2 completa
        QuestionExtractionResult question2Complete = new QuestionExtractionResult(2, "Enunciado completo da questão 2",
                List.of(new AlternativeExtractionResult('A', "Alt A"), new AlternativeExtractionResult('B', "Alt B")),
                'B', false, false, 5);

        when(visionExtractionPort.extractQuestions(any()))
                .thenReturn(List.of(question1, question2Partial))
                .thenReturn(List.of(question2Complete));

        processor.process(jobId);

        assertThat(job.getStatus()).isEqualTo(ImportStatus.AGUARDANDO_REVISAO);
        assertThat(job.getExamDraft()).isNotNull();
        assertThat(job.getExamDraft().getTitle()).isEqualTo("Prova Teste");
        assertThat(job.getExamDraft().getQuestions()).hasSize(2);

        var mergedQuestion2 = job.getExamDraft().getQuestions().stream()
                .filter(q -> q.getNumber() == 2)
                .findFirst()
                .orElseThrow();
        assertThat(mergedQuestion2.getStatement()).isEqualTo("Enunciado completo da questão 2");
        assertThat(mergedQuestion2.getAlternatives()).hasSize(2);
        assertThat(mergedQuestion2.getCorrectAlternativeLabel()).isEqualTo('B');
    }

    @Test
    void processMergesExternalAnswerKeyWhenGabaritoPageIsFound() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "storage-key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        byte[] pdfBytes = "pdf-bytes".getBytes();
        when(fileStoragePort.load("storage-key")).thenReturn(pdfBytes);
        when(pdfTextExtractionPort.countPages(pdfBytes)).thenReturn(2);
        when(pdfTextExtractionPort.extractPage(pdfBytes, 1))
                .thenReturn(new PageContent(1, "texto da questão", true, false, false));
        when(pdfTextExtractionPort.extractPage(pdfBytes, 2))
                .thenReturn(new PageContent(2, "GABARITO OFICIAL: 1-A", true, false, false));
        lenient().when(pdfTextExtractionPort.extractEmbeddedImages(any(), anyInt())).thenReturn(List.of());

        when(visionExtractionPort.extractCover(any()))
                .thenReturn(new CoverExtractionResult("Prova", "Instituição", 2025, "Ed 1", "Geral", "raw"));

        QuestionExtractionResult questionSemGabarito = new QuestionExtractionResult(1, "Enunciado",
                List.of(new AlternativeExtractionResult('A', "Alt A"), new AlternativeExtractionResult('B', "Alt B")),
                null, false, false, 1);
        when(visionExtractionPort.extractQuestions(any())).thenReturn(List.of(questionSemGabarito));
        when(visionExtractionPort.extractAnswerKey(any())).thenReturn(Map.of(1, 'A'));

        processor.process(jobId);

        var question = job.getExamDraft().getQuestions().get(0);
        assertThat(question.getCorrectAlternativeLabel()).isEqualTo('A');
    }
}
