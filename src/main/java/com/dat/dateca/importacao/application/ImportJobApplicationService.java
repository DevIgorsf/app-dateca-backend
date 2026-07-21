package com.dat.dateca.importacao.application;

import com.dat.dateca.importacao.application.dto.ExamDraftReviewView;
import com.dat.dateca.importacao.application.dto.ImageContent;
import com.dat.dateca.importacao.application.dto.ImportJobStatusView;
import com.dat.dateca.importacao.application.dto.PublishResult;
import com.dat.dateca.importacao.application.dto.UpdateAlternativeDraftCommand;
import com.dat.dateca.importacao.application.dto.UpdateExamDraftCommand;
import com.dat.dateca.importacao.application.dto.UpdateQuestionDraftCommand;
import com.dat.dateca.importacao.application.dto.UploadImportResult;
import com.dat.dateca.importacao.domain.AlternativeDraft;
import com.dat.dateca.importacao.domain.ExamDraft;
import com.dat.dateca.importacao.domain.ImportJob;
import com.dat.dateca.importacao.domain.QuestionDraft;
import com.dat.dateca.importacao.domain.QuestionImageDraft;
import com.dat.dateca.importacao.domain.exceptions.CorruptedPdfException;
import com.dat.dateca.importacao.domain.exceptions.ImportJobNotFoundException;
import com.dat.dateca.importacao.domain.exceptions.ImportJobStateConflictException;
import com.dat.dateca.importacao.domain.exceptions.PublishValidationException;
import com.dat.dateca.importacao.domain.exceptions.UnsupportedFileTypeException;
import com.dat.dateca.importacao.domain.ports.ExamPublicationPort;
import com.dat.dateca.importacao.domain.ports.ExamPublicationRequest;
import com.dat.dateca.importacao.domain.ports.ImportJobDispatcher;
import com.dat.dateca.importacao.domain.ports.PdfTextExtractionPort;
import com.dat.dateca.importacao.domain.ports.PublishedExamResult;
import com.dat.dateca.importacao.infrastructure.persistence.ImportJobRepository;
import com.dat.dateca.shared.storage.FileStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImportJobApplicationService {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;
    private static final int MAX_PAGES = 60;

    private final ImportJobRepository importJobRepository;
    private final FileStoragePort fileStoragePort;
    private final PdfTextExtractionPort pdfTextExtractionPort;
    private final ImportJobDispatcher importJobDispatcher;
    private final ExamPublicationPort examPublicationPort;

    public ImportJobApplicationService(ImportJobRepository importJobRepository,
                                        FileStoragePort fileStoragePort,
                                        PdfTextExtractionPort pdfTextExtractionPort,
                                        ImportJobDispatcher importJobDispatcher,
                                        ExamPublicationPort examPublicationPort) {
        this.importJobRepository = importJobRepository;
        this.fileStoragePort = fileStoragePort;
        this.pdfTextExtractionPort = pdfTextExtractionPort;
        this.importJobDispatcher = importJobDispatcher;
        this.examPublicationPort = examPublicationPort;
    }

    @Transactional
    public UploadImportResult startImport(MultipartFile file, Long createdByUserId) {
        validateUpload(file);

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new CorruptedPdfException("Não foi possível ler o arquivo enviado");
        }

        int pageCount;
        try {
            pageCount = pdfTextExtractionPort.countPages(content);
        } catch (RuntimeException e) {
            throw new CorruptedPdfException("O arquivo enviado não é um PDF válido");
        }

        if (pageCount > MAX_PAGES) {
            throw new UnsupportedFileTypeException(
                    "A prova excede o limite de " + MAX_PAGES + " páginas suportado nesta versão");
        }

        String storageKey = fileStoragePort.store(content, file.getOriginalFilename());
        ImportJob job = new ImportJob(file.getOriginalFilename(), file.getSize(), storageKey, createdByUserId);
        importJobRepository.save(job);

        importJobDispatcher.dispatch(job.getId());

        return new UploadImportResult(job.getId(), job.getStatus());
    }

    @Transactional(readOnly = true)
    public ImportJobStatusView getStatus(UUID jobId) {
        return ImportJobStatusView.from(getJobOrThrow(jobId));
    }

    @Transactional(readOnly = true)
    public ExamDraftReviewView getDraft(UUID jobId) {
        ImportJob job = getJobOrThrow(jobId);
        if (job.getExamDraft() == null) {
            throw new ImportJobStateConflictException("O rascunho ainda não está pronto para revisão");
        }
        return ExamDraftReviewView.from(job);
    }

    @Transactional
    public ExamDraftReviewView updateDraft(UUID jobId, UpdateExamDraftCommand command) {
        ImportJob job = getJobOrThrow(jobId);
        if (!job.isReadyForReview()) {
            throw new ImportJobStateConflictException("O rascunho só pode ser editado enquanto aguarda revisão");
        }

        ExamDraft draft = job.getExamDraft();
        draft.replaceMetadata(command.title(), command.institution(), command.year(), command.edition(), command.subjectArea());
        draft.replaceQuestions(buildQuestionDrafts(command.questions()));

        importJobRepository.save(job);
        return ExamDraftReviewView.from(job);
    }

    @Transactional
    public PublishResult publish(UUID jobId, Long publishedByUserId) {
        ImportJob job = getJobOrThrow(jobId);
        if (!job.isReadyForReview()) {
            throw new ImportJobStateConflictException("Só é possível publicar um rascunho que está aguardando revisão");
        }

        ExamDraft draft = job.getExamDraft();
        validateDraftForPublication(draft);

        ExamPublicationRequest request = buildPublicationRequest(job, draft, publishedByUserId);
        PublishedExamResult result = examPublicationPort.publish(request);

        job.markPublished(result.examId());
        importJobRepository.save(job);

        return new PublishResult(result.examId());
    }

    @Transactional
    public void cancel(UUID jobId) {
        ImportJob job = getJobOrThrow(jobId);
        if (job.isPublished()) {
            throw new ImportJobStateConflictException("Uma importação já publicada não pode ser cancelada");
        }
        job.markCancelled();
        importJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public ImageContent getDraftImage(UUID jobId, UUID imageId) {
        ImportJob job = getJobOrThrow(jobId);
        if (job.getExamDraft() == null) {
            throw new ImportJobStateConflictException("O rascunho ainda não está pronto para revisão");
        }

        for (QuestionDraft question : job.getExamDraft().getQuestions()) {
            for (QuestionImageDraft image : question.getImages()) {
                if (image.getId().equals(imageId)) {
                    return new ImageContent(fileStoragePort.load(image.getStorageKey()), image.getContentType());
                }
            }
        }
        throw new ImportJobNotFoundException("Imagem não encontrada: " + imageId);
    }

    private List<QuestionDraft> buildQuestionDrafts(List<UpdateQuestionDraftCommand> commands) {
        List<QuestionDraft> questions = new ArrayList<>();
        int ordem = 0;
        for (UpdateQuestionDraftCommand questionCommand : commands) {
            QuestionDraft question = new QuestionDraft(questionCommand.number(), questionCommand.statement(),
                    questionCommand.annulled(), questionCommand.correctAlternativeLabel(),
                    questionCommand.sourcePageNumber(), false, ordem++);

            int altOrdem = 0;
            for (UpdateAlternativeDraftCommand alternativeCommand : questionCommand.alternatives()) {
                question.addAlternative(new AlternativeDraft(alternativeCommand.label(), alternativeCommand.text(), altOrdem++));
            }

            questions.add(question);
        }
        return questions;
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UnsupportedFileTypeException("Nenhum arquivo enviado");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new UnsupportedFileTypeException("O arquivo excede o tamanho máximo de 20MB");
        }
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean looksLikePdf = "application/pdf".equals(contentType)
                || (filename != null && filename.toLowerCase().endsWith(".pdf"));
        if (!looksLikePdf) {
            throw new UnsupportedFileTypeException("Apenas arquivos PDF são suportados");
        }
    }

    private void validateDraftForPublication(ExamDraft draft) {
        if (draft.getQuestions().isEmpty()) {
            throw new PublishValidationException("A prova precisa ter ao menos uma questão para ser publicada");
        }
        for (QuestionDraft question : draft.getQuestions()) {
            if (question.getStatement() == null || question.getStatement().isBlank()) {
                throw new PublishValidationException("A questão " + question.getNumber() + " está com o enunciado vazio");
            }
            if (question.getAlternatives().size() < 2) {
                throw new PublishValidationException("A questão " + question.getNumber() + " precisa de ao menos 2 alternativas");
            }
        }
    }

    private ExamPublicationRequest buildPublicationRequest(ImportJob job, ExamDraft draft, Long publishedByUserId) {
        List<ExamPublicationRequest.QuestionPublicationData> questions = new ArrayList<>();
        for (QuestionDraft question : draft.getQuestions()) {
            List<ExamPublicationRequest.AlternativePublicationData> alternatives = question.getAlternatives().stream()
                    .map(a -> new ExamPublicationRequest.AlternativePublicationData(a.getLabel(), a.getAlternativeText(), a.getOrdem()))
                    .toList();

            List<ExamPublicationRequest.ImagePublicationData> images = question.getImages().stream()
                    .map(img -> new ExamPublicationRequest.ImagePublicationData(img.getStorageKey(), img.getContentType(), img.getOrdem()))
                    .toList();

            questions.add(new ExamPublicationRequest.QuestionPublicationData(
                    question.getNumber(), question.getStatement(), question.getCorrectAlternativeLabel(),
                    question.getSourcePageNumber(), question.getOrdem(), alternatives, images));
        }

        return new ExamPublicationRequest(job.getId(), draft.getTitle(), draft.getInstitution(), draft.getYear(),
                draft.getEdition(), draft.getSubjectArea(), publishedByUserId, questions);
    }

    private ImportJob getJobOrThrow(UUID jobId) {
        return importJobRepository.findById(jobId)
                .orElseThrow(() -> new ImportJobNotFoundException("Importação não encontrada: " + jobId));
    }
}
