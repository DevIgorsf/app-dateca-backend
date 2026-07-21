package com.dat.dateca.exam.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "exams")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

    @Id
    @UuidGenerator
    private UUID id;

    private String title;

    private String institution;

    private Integer year;

    private String edition;

    @Column(name = "subject_area")
    private String subjectArea;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "source_import_job_id")
    private UUID sourceImportJobId;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<ExamQuestion> questions = new ArrayList<>();

    public Exam(String title, String institution, Integer year, String edition, String subjectArea,
                Long createdByUserId, UUID sourceImportJobId) {
        this.title = title;
        this.institution = institution;
        this.year = year;
        this.edition = edition;
        this.subjectArea = subjectArea;
        this.createdByUserId = createdByUserId;
        this.sourceImportJobId = sourceImportJobId;
        this.publishedAt = LocalDateTime.now();
        this.questions = new ArrayList<>();
    }

    public void addQuestion(ExamQuestion question) {
        question.linkToExam(this);
        this.questions.add(question);
    }
}
