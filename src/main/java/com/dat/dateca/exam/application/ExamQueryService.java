package com.dat.dateca.exam.application;

import com.dat.dateca.exam.application.dto.ExamDetailView;
import com.dat.dateca.exam.application.dto.ExamSummaryView;
import com.dat.dateca.exam.domain.Exam;
import com.dat.dateca.exam.domain.ExamNotFoundException;
import com.dat.dateca.exam.infrastructure.persistence.ExamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ExamQueryService {

    private final ExamRepository examRepository;

    public ExamQueryService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Transactional(readOnly = true)
    public ExamDetailView getById(UUID examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Prova não encontrada: " + examId));
        return ExamDetailView.from(exam);
    }

    @Transactional(readOnly = true)
    public Page<ExamSummaryView> list(Pageable pageable) {
        return examRepository.findAllByOrderByPublishedAtDesc(pageable).map(ExamSummaryView::from);
    }
}
