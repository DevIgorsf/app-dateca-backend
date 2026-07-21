package com.dat.dateca.exam.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "exam_questions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestion {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    private Integer number;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String statement;

    @Column(name = "correct_alternative_label")
    private Character correctAlternativeLabel;

    @Column(name = "source_page_number")
    private Integer sourcePageNumber;

    @Column(nullable = false)
    private int ordem;

    @OneToMany(mappedBy = "examQuestion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<ExamQuestionAlternative> alternatives = new ArrayList<>();

    @OneToMany(mappedBy = "examQuestion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<ExamQuestionImage> images = new ArrayList<>();

    public ExamQuestion(Integer number, String statement, Character correctAlternativeLabel,
                         Integer sourcePageNumber, int ordem) {
        this.number = number;
        this.statement = statement;
        this.correctAlternativeLabel = correctAlternativeLabel;
        this.sourcePageNumber = sourcePageNumber;
        this.ordem = ordem;
        this.alternatives = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    void linkToExam(Exam exam) {
        this.exam = exam;
    }

    public void addAlternative(ExamQuestionAlternative alternative) {
        alternative.linkToExamQuestion(this);
        this.alternatives.add(alternative);
    }

    public void addImage(ExamQuestionImage image) {
        image.linkToExamQuestion(this);
        this.images.add(image);
    }
}
