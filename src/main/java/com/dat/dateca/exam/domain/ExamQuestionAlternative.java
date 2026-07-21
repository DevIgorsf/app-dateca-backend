package com.dat.dateca.exam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "exam_question_alternatives")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionAlternative {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_question_id", nullable = false)
    private ExamQuestion examQuestion;

    @Column(nullable = false)
    private Character label;

    @Lob
    @Column(name = "alternative_text", columnDefinition = "TEXT", nullable = false)
    private String alternativeText;

    @Column(nullable = false)
    private int ordem;

    public ExamQuestionAlternative(Character label, String alternativeText, int ordem) {
        this.label = label;
        this.alternativeText = alternativeText;
        this.ordem = ordem;
    }

    void linkToExamQuestion(ExamQuestion examQuestion) {
        this.examQuestion = examQuestion;
    }
}
