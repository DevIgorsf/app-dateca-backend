package com.dat.dateca.importacao.application;

import com.dat.dateca.importacao.domain.AlternativeDraft;
import com.dat.dateca.importacao.domain.ExamDraft;
import com.dat.dateca.importacao.domain.ImportJob;
import com.dat.dateca.importacao.domain.ImportStatus;
import com.dat.dateca.importacao.domain.QuestionDraft;
import com.dat.dateca.importacao.domain.QuestionImageDraft;
import com.dat.dateca.importacao.domain.ports.AlternativeExtractionResult;
import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.ExtractedImage;
import com.dat.dateca.importacao.domain.ports.PageContent;
import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.PdfTextExtractionPort;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionResult;
import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;
import com.dat.dateca.importacao.infrastructure.persistence.ImportJobRepository;
import com.dat.dateca.shared.storage.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pipeline real de importação: decide por página se usa texto nativo ou visão, extrai a capa,
 * extrai questões em blocos (com sobreposição) e faz merge do gabarito quando encontrado.
 * Roda fora de uma requisição HTTP — ver {@link com.dat.dateca.importacao.domain.ports.ImportJobDispatcher}.
 */
@Service
public class PdfImportProcessor {

    private static final Logger log = LoggerFactory.getLogger(PdfImportProcessor.class);

    private static final int COVER_PAGE_WINDOW = 3;
    private static final int CHUNK_SIZE = 5;
    private static final int CHUNK_OVERLAP = 1;
    private static final int RASTER_DPI = 180;

    private final ImportJobRepository importJobRepository;
    private final PdfTextExtractionPort pdfTextExtractionPort;
    private final VisionExtractionPort visionExtractionPort;
    private final FileStoragePort fileStoragePort;

    public PdfImportProcessor(ImportJobRepository importJobRepository,
                               PdfTextExtractionPort pdfTextExtractionPort,
                               VisionExtractionPort visionExtractionPort,
                               FileStoragePort fileStoragePort) {
        this.importJobRepository = importJobRepository;
        this.pdfTextExtractionPort = pdfTextExtractionPort;
        this.visionExtractionPort = visionExtractionPort;
        this.fileStoragePort = fileStoragePort;
    }

    @Transactional
    public void process(UUID importJobId) {
        ImportJob job = importJobRepository.findById(importJobId).orElse(null);
        if (job == null) {
            log.warn("Importação {} não encontrada ao iniciar o processamento (possivelmente cancelada)", importJobId);
            return;
        }

        try {
            byte[] pdfBytes = fileStoragePort.load(job.getStorageKey());
            int pageCount = pdfTextExtractionPort.countPages(pdfBytes);

            List<PageContent> pages = readAllPages(job, pdfBytes, pageCount);

            job.advanceTo(ImportStatus.EXTRAINDO_IA, "identificando capa da prova");
            importJobRepository.save(job);
            CoverExtractionResult cover = visionExtractionPort.extractCover(
                    buildPageInputs(pdfBytes, pages.subList(0, Math.min(COVER_PAGE_WINDOW, pages.size()))));

            List<QuestionDraft> extractedQuestions = extractQuestionsInChunks(job, pdfBytes, pages);
            applyAnswerKeyIfPresent(pdfBytes, pages, extractedQuestions);

            extractedQuestions.sort(Comparator.comparing(QuestionDraft::getNumber));
            for (int i = 0; i < extractedQuestions.size(); i++) {
                extractedQuestions.get(i).reorder(i);
            }

            ExamDraft draft = new ExamDraft(cover.title(), cover.institution(), cover.year(), cover.edition(),
                    cover.subjectArea(), cover.rawText());
            for (QuestionDraft question : extractedQuestions) {
                draft.addQuestion(question);
            }

            job.attachDraft(draft);
            importJobRepository.save(job);
        } catch (Exception e) {
            log.error("Falha ao processar importação {}", importJobId, e);
            job.markError(describeFailure(e));
            importJobRepository.save(job);
        }
    }

    private List<PageContent> readAllPages(ImportJob job, byte[] pdfBytes, int pageCount) {
        List<PageContent> pages = new ArrayList<>();
        job.advanceTo(ImportStatus.EXTRAINDO_TEXTO, "lendo texto nativo (0/" + pageCount + ")");
        importJobRepository.save(job);

        for (int page = 1; page <= pageCount; page++) {
            pages.add(pdfTextExtractionPort.extractPage(pdfBytes, page));
            job.advanceTo(ImportStatus.EXTRAINDO_TEXTO, "lendo texto nativo (" + page + "/" + pageCount + ")");
            importJobRepository.save(job);
        }
        return pages;
    }

    private List<QuestionDraft> extractQuestionsInChunks(ImportJob job, byte[] pdfBytes, List<PageContent> pages) {
        List<QuestionDraft> extractedQuestions = new ArrayList<>();
        List<List<PageContent>> chunks = buildChunks(pages, CHUNK_SIZE, CHUNK_OVERLAP);

        int chunkIndex = 0;
        for (List<PageContent> chunk : chunks) {
            chunkIndex++;
            job.advanceTo(ImportStatus.EXTRAINDO_IA, "extraindo questões (bloco " + chunkIndex + "/" + chunks.size() + ")");
            importJobRepository.save(job);

            List<QuestionExtractionResult> chunkQuestions =
                    visionExtractionPort.extractQuestions(buildPageInputs(pdfBytes, chunk));

            for (QuestionExtractionResult extracted : chunkQuestions) {
                mergeQuestion(extractedQuestions, extracted, pdfBytes);
            }
        }
        return extractedQuestions;
    }

    private void mergeQuestion(List<QuestionDraft> existing, QuestionExtractionResult extracted, byte[] pdfBytes) {
        QuestionDraft candidate = toQuestionDraft(extracted, pdfBytes);

        for (int i = 0; i < existing.size(); i++) {
            QuestionDraft current = existing.get(i);
            if (current.getNumber() != null && current.getNumber().equals(candidate.getNumber())) {
                if (candidate.completenessScore() > current.completenessScore()) {
                    current.mergeFrom(candidate);
                }
                return;
            }
        }
        existing.add(candidate);
    }

    private QuestionDraft toQuestionDraft(QuestionExtractionResult extracted, byte[] pdfBytes) {
        QuestionDraft question = new QuestionDraft(extracted.number(), extracted.statement(), extracted.annulled(),
                extracted.annulled() ? null : extracted.correctAnswer(), extracted.sourcePageNumber(),
                extracted.needsReview(), 0);

        int altOrdem = 0;
        for (AlternativeExtractionResult alternative : extracted.alternatives()) {
            question.addAlternative(new AlternativeDraft(alternative.label(), alternative.text(), altOrdem++));
        }

        int imgOrdem = 0;
        for (ExtractedImage image : pdfTextExtractionPort.extractEmbeddedImages(pdfBytes, extracted.sourcePageNumber())) {
            String storageKey = fileStoragePort.store(image.content(), "questao-" + extracted.number() + "-" + imgOrdem + ".png");
            question.addImage(new QuestionImageDraft(storageKey, image.contentType(), imgOrdem++));
        }

        return question;
    }

    private void applyAnswerKeyIfPresent(byte[] pdfBytes, List<PageContent> pages, List<QuestionDraft> questions) {
        List<PageContent> answerKeyPages = pages.stream()
                .filter(p -> p.hasNativeText() && p.nativeText().toUpperCase().contains("GABARITO"))
                .toList();

        if (answerKeyPages.isEmpty()) {
            return;
        }

        Map<Integer, Character> answerKey = visionExtractionPort.extractAnswerKey(buildPageInputs(pdfBytes, answerKeyPages));
        for (QuestionDraft question : questions) {
            Character letter = answerKey.get(question.getNumber());
            if (letter != null) {
                question.applyExternalAnswer(letter);
            }
        }
    }

    private List<PageInput> buildPageInputs(byte[] pdfBytes, List<PageContent> pages) {
        List<PageInput> inputs = new ArrayList<>();
        for (PageContent page : pages) {
            if (page.needsVisionExtraction()) {
                byte[] rendered = pdfTextExtractionPort.rasterizePage(pdfBytes, page.pageNumber(), RASTER_DPI);
                inputs.add(PageInput.ofImage(page.pageNumber(), rendered, "image/png"));
            } else {
                inputs.add(PageInput.ofText(page.pageNumber(), page.nativeText()));
            }
        }
        return inputs;
    }

    private List<List<PageContent>> buildChunks(List<PageContent> pages, int size, int overlap) {
        List<List<PageContent>> chunks = new ArrayList<>();
        int step = Math.max(1, size - overlap);
        for (int start = 0; start < pages.size(); start += step) {
            int end = Math.min(pages.size(), start + size);
            chunks.add(pages.subList(start, end));
            if (end == pages.size()) {
                break;
            }
        }
        return chunks;
    }

    private String describeFailure(Exception e) {
        String message = e.getMessage();
        return "Falha ao processar a prova: " + (message != null ? message : e.getClass().getSimpleName());
    }
}
