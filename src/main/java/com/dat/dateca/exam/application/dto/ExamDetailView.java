package com.dat.dateca.exam.application.dto;

import com.dat.dateca.exam.domain.Exam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExamDetailView(UUID id, String title, String institution, Integer year, String edition,
                              String subjectArea, LocalDateTime publishedAt, List<ExamQuestionView> questions) {

    public static ExamDetailView from(Exam exam) {
        return new ExamDetailView(exam.getId(), exam.getTitle(), exam.getInstitution(), exam.getYear(),
                exam.getEdition(), exam.getSubjectArea(), exam.getPublishedAt(),
                exam.getQuestions().stream().map(ExamQuestionView::from).toList());
    }
}
