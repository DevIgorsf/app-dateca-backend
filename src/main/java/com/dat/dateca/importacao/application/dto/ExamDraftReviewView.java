package com.dat.dateca.importacao.application.dto;

import com.dat.dateca.importacao.domain.ExamDraft;
import com.dat.dateca.importacao.domain.ImportJob;

import java.util.List;
import java.util.UUID;

public record ExamDraftReviewView(UUID importJobId, String title, String institution, Integer year, String edition,
                                   String subjectArea, List<QuestionDraftView> questions) {

    public static ExamDraftReviewView from(ImportJob job) {
        ExamDraft draft = job.getExamDraft();
        List<QuestionDraftView> questions = draft.getQuestions().stream()
                .map(QuestionDraftView::from)
                .toList();
        return new ExamDraftReviewView(job.getId(), draft.getTitle(), draft.getInstitution(), draft.getYear(),
                draft.getEdition(), draft.getSubjectArea(), questions);
    }
}
