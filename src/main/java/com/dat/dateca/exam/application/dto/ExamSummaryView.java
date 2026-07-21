package com.dat.dateca.exam.application.dto;

import com.dat.dateca.exam.domain.Exam;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExamSummaryView(UUID id, String title, String institution, Integer year, String edition,
                               String subjectArea, int questionCount, LocalDateTime publishedAt) {

    public static ExamSummaryView from(Exam exam) {
        return new ExamSummaryView(exam.getId(), exam.getTitle(), exam.getInstitution(), exam.getYear(),
                exam.getEdition(), exam.getSubjectArea(), exam.getQuestions().size(), exam.getPublishedAt());
    }
}
