package com.dat.dateca.importacao.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "exam_drafts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExamDraft {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_job_id", nullable = false, unique = true)
    private ImportJob importJob;

    private String title;

    private String institution;

    private Integer year;

    private String edition;

    @Column(name = "subject_area")
    private String subjectArea;

    @Lob
    @Column(name = "raw_cover_text", columnDefinition = "TEXT")
    private String rawCoverText;

    @OneToMany(mappedBy = "examDraft", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<QuestionDraft> questions = new ArrayList<>();

    public ExamDraft(String title, String institution, Integer year, String edition, String subjectArea, String rawCoverText) {
        this.title = title;
        this.institution = institution;
        this.year = year;
        this.edition = edition;
        this.subjectArea = subjectArea;
        this.rawCoverText = rawCoverText;
        this.questions = new ArrayList<>();
    }

    void linkToImportJob(ImportJob importJob) {
        this.importJob = importJob;
    }

    public void addQuestion(QuestionDraft question) {
        question.linkToExamDraft(this);
        this.questions.add(question);
    }

    public void replaceMetadata(String title, String institution, Integer year, String edition, String subjectArea) {
        this.title = title;
        this.institution = institution;
        this.year = year;
        this.edition = edition;
        this.subjectArea = subjectArea;
    }

    public void replaceQuestions(List<QuestionDraft> newQuestions) {
        this.questions.clear();
        for (QuestionDraft question : newQuestions) {
            addQuestion(question);
        }
    }
}
