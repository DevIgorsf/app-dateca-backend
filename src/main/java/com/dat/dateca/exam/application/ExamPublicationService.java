package com.dat.dateca.exam.application;

import com.dat.dateca.exam.domain.Exam;
import com.dat.dateca.exam.domain.ExamQuestion;
import com.dat.dateca.exam.domain.ExamQuestionAlternative;
import com.dat.dateca.exam.domain.ExamQuestionImage;
import com.dat.dateca.exam.infrastructure.persistence.ExamRepository;
import com.dat.dateca.importacao.domain.ports.ExamPublicationPort;
import com.dat.dateca.importacao.domain.ports.ExamPublicationRequest;
import com.dat.dateca.importacao.domain.ports.PublishedExamResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa o contrato de publicação definido pelo módulo {@code importacao}
 * ({@link ExamPublicationPort}). A importação é a consumidora do caso de uso "publicar";
 * o módulo exam apenas fornece a implementação, mantendo os dois bounded contexts isolados.
 */
@Service
public class ExamPublicationService implements ExamPublicationPort {

    private final ExamRepository examRepository;

    public ExamPublicationService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    @Transactional
    public PublishedExamResult publish(ExamPublicationRequest request) {
        Exam exam = new Exam(request.title(), request.institution(), request.year(), request.edition(),
                request.subjectArea(), request.publishedByUserId(), request.sourceImportJobId());

        for (ExamPublicationRequest.QuestionPublicationData questionData : request.questions()) {
            ExamQuestion question = new ExamQuestion(questionData.number(), questionData.statement(),
                    questionData.correctAlternativeLabel(), questionData.sourcePageNumber(), questionData.ordem());

            for (ExamPublicationRequest.AlternativePublicationData alternativeData : questionData.alternatives()) {
                question.addAlternative(new ExamQuestionAlternative(alternativeData.label(), alternativeData.text(), alternativeData.ordem()));
            }

            for (ExamPublicationRequest.ImagePublicationData imageData : questionData.images()) {
                question.addImage(new ExamQuestionImage(imageData.storageKey(), imageData.contentType(), imageData.ordem()));
            }

            exam.addQuestion(question);
        }

        examRepository.save(exam);
        return new PublishedExamResult(exam.getId());
    }
}
