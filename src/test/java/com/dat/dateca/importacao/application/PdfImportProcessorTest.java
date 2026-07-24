package com.dat.dateca.importacao.application;

import com.dat.dateca.importacao.domain.ImportJob;
import com.dat.dateca.importacao.domain.ImportStatus;
import com.dat.dateca.importacao.domain.ports.AlternativeExtractionResult;
import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.ExtractedImage;
import com.dat.dateca.importacao.domain.ports.PageContent;
import com.dat.dateca.importacao.domain.ports.PdfTextExtractionPort;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionBatch;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
                .thenReturn(QuestionExtractionBatch.complete(List.of(question1, question2Partial)))
                .thenReturn(QuestionExtractionBatch.complete(List.of(question2Complete)));

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
        when(visionExtractionPort.extractQuestions(any()))
                .thenReturn(QuestionExtractionBatch.complete(List.of(questionSemGabarito)));
        when(visionExtractionPort.extractAnswerKey(any())).thenReturn(Map.of(1, 'A'));

        processor.process(jobId);

        var question = job.getExamDraft().getQuestions().get(0);
        assertThat(question.getCorrectAlternativeLabel()).isEqualTo('A');
    }

    @Test
    void processDetectsAnswerKeyOnFullyScannedExamWithoutNativeText() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova-escaneada.pdf", 100L, "storage-key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        byte[] pdfBytes = "pdf-bytes".getBytes();
        when(fileStoragePort.load("storage-key")).thenReturn(pdfBytes);
        when(pdfTextExtractionPort.countPages(pdfBytes)).thenReturn(2);

        // Prova 100% escaneada: nenhuma pagina tem texto nativo, entao o filtro antigo (que exigia
        // hasNativeText + palavra "GABARITO") nunca acharia a pagina de gabarito.
        when(pdfTextExtractionPort.extractPage(pdfBytes, 1))
                .thenReturn(new PageContent(1, null, false, true, false));
        when(pdfTextExtractionPort.extractPage(pdfBytes, 2))
                .thenReturn(new PageContent(2, null, false, true, false));
        lenient().when(pdfTextExtractionPort.extractEmbeddedImages(any(), anyInt())).thenReturn(List.of());
        when(pdfTextExtractionPort.rasterizePage(any(), anyInt(), anyInt())).thenReturn("img".getBytes());

        when(visionExtractionPort.extractCover(any()))
                .thenReturn(new CoverExtractionResult("Prova", "Instituição", 2025, "Ed 1", "Geral", "raw"));

        QuestionExtractionResult questionSemGabarito = new QuestionExtractionResult(1, "Enunciado",
                List.of(new AlternativeExtractionResult('A', "Alt A"), new AlternativeExtractionResult('B', "Alt B")),
                null, false, false, 1);
        when(visionExtractionPort.extractQuestions(any()))
                .thenReturn(QuestionExtractionBatch.complete(List.of(questionSemGabarito)));
        // A pagina de gabarito escaneada e alcancada pela janela de fallback e o modelo devolve o par.
        when(visionExtractionPort.extractAnswerKey(any())).thenReturn(Map.of(1, 'C'));

        processor.process(jobId);

        // extractAnswerKey foi chamado mesmo sem texto nativo, e a resposta foi aplicada.
        verify(visionExtractionPort).extractAnswerKey(any());
        assertThat(job.getExamDraft().getQuestions().get(0).getCorrectAlternativeLabel()).isEqualTo('C');
    }

    @Test
    void processSplitsTruncatedBlockAndRecoversAllQuestions() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "storage-key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        byte[] pdfBytes = "pdf-bytes".getBytes();
        when(fileStoragePort.load("storage-key")).thenReturn(pdfBytes);
        when(pdfTextExtractionPort.countPages(pdfBytes)).thenReturn(2);
        when(pdfTextExtractionPort.extractPage(pdfBytes, 1))
                .thenReturn(new PageContent(1, "questão 1", true, false, false));
        when(pdfTextExtractionPort.extractPage(pdfBytes, 2))
                .thenReturn(new PageContent(2, "questão 2", true, false, false));
        lenient().when(pdfTextExtractionPort.extractEmbeddedImages(any(), anyInt())).thenReturn(List.of());

        when(visionExtractionPort.extractCover(any()))
                .thenReturn(new CoverExtractionResult("Prova", "Inst", 2025, "Ed", "Geral", "raw"));

        QuestionExtractionResult q1 = new QuestionExtractionResult(1, "Enunciado 1",
                List.of(new AlternativeExtractionResult('A', "a"), new AlternativeExtractionResult('B', "b")),
                'A', false, false, 1);
        QuestionExtractionResult q2 = new QuestionExtractionResult(2, "Enunciado 2",
                List.of(new AlternativeExtractionResult('A', "a"), new AlternativeExtractionResult('B', "b")),
                'B', false, false, 2);

        // 1ª chamada (bloco de 2 páginas) vem TRUNCADA e só traz a questão 1 (a 2 foi cortada).
        // Depois o processor divide o bloco: página 1 e página 2 são reprocessadas isoladamente.
        when(visionExtractionPort.extractQuestions(any()))
                .thenReturn(QuestionExtractionBatch.truncated(List.of(q1)))
                .thenReturn(QuestionExtractionBatch.complete(List.of(q1)))
                .thenReturn(QuestionExtractionBatch.complete(List.of(q2)));

        processor.process(jobId);

        assertThat(job.getStatus()).isEqualTo(ImportStatus.AGUARDANDO_REVISAO);
        // Sem o split, a questão 2 (cortada na resposta truncada) teria sumido silenciosamente.
        assertThat(job.getExamDraft().getQuestions()).hasSize(2);
        // O bloco foi chamado 3x: 1 truncada + 2 metades.
        verify(visionExtractionPort, times(3)).extractQuestions(any());
    }

    @Test
    void processMarksQuestionForReviewWhenSinglePageStaysTruncated() {
        UUID jobId = UUID.randomUUID();
        ImportJob job = new ImportJob("prova.pdf", 100L, "storage-key", 1L);
        when(importJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        byte[] pdfBytes = "pdf-bytes".getBytes();
        when(fileStoragePort.load("storage-key")).thenReturn(pdfBytes);
        when(pdfTextExtractionPort.countPages(pdfBytes)).thenReturn(1);
        when(pdfTextExtractionPort.extractPage(pdfBytes, 1))
                .thenReturn(new PageContent(1, "questão enorme", true, false, false));
        lenient().when(pdfTextExtractionPort.extractEmbeddedImages(any(), anyInt())).thenReturn(List.of());

        when(visionExtractionPort.extractCover(any()))
                .thenReturn(new CoverExtractionResult("Prova", "Inst", 2025, "Ed", "Geral", "raw"));

        QuestionExtractionResult parcial = new QuestionExtractionResult(1, "Enunciado parcial",
                List.of(new AlternativeExtractionResult('A', "a")), null, false, false, 1);
        // Página única que continua truncada: não há como dividir mais, então marca para revisão.
        when(visionExtractionPort.extractQuestions(any()))
                .thenReturn(QuestionExtractionBatch.truncated(List.of(parcial)));

        processor.process(jobId);

        var question = job.getExamDraft().getQuestions().get(0);
        assertThat(question.isNeedsReview()).isTrue();
        // Uma única página truncada é chamada só uma vez (não há divisão possível).
        verify(visionExtractionPort, times(1)).extractQuestions(any());
    }
}
