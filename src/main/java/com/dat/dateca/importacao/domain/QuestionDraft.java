package com.dat.dateca.importacao.domain;

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
@Table(name = "question_drafts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDraft {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_draft_id", nullable = false)
    private ExamDraft examDraft;

    private Integer number;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String statement;

    @Column(nullable = false)
    private boolean annulled;

    @Column(name = "correct_alternative_label")
    private Character correctAlternativeLabel;

    @Column(name = "source_page_number")
    private Integer sourcePageNumber;

    @Column(name = "needs_review", nullable = false)
    private boolean needsReview;

    @Column(nullable = false)
    private int ordem;

    @OneToMany(mappedBy = "questionDraft", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<AlternativeDraft> alternatives = new ArrayList<>();

    @OneToMany(mappedBy = "questionDraft", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    private List<QuestionImageDraft> images = new ArrayList<>();

    public QuestionDraft(Integer number, String statement, boolean annulled, Character correctAlternativeLabel,
                          Integer sourcePageNumber, boolean needsReview, int ordem) {
        this.number = number;
        this.statement = statement;
        this.annulled = annulled;
        this.correctAlternativeLabel = correctAlternativeLabel;
        this.sourcePageNumber = sourcePageNumber;
        this.needsReview = needsReview;
        this.ordem = ordem;
        this.alternatives = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    void linkToExamDraft(ExamDraft examDraft) {
        this.examDraft = examDraft;
    }

    public void addAlternative(AlternativeDraft alternative) {
        alternative.linkToQuestionDraft(this);
        this.alternatives.add(alternative);
    }

    public void addImage(QuestionImageDraft image) {
        image.linkToQuestionDraft(this);
        this.images.add(image);
    }

    public void reorder(int ordem) {
        this.ordem = ordem;
    }

    public void applyExternalAnswer(Character letter) {
        if (this.correctAlternativeLabel == null) {
            this.correctAlternativeLabel = letter;
        } else if (!this.correctAlternativeLabel.equals(letter)) {
            this.needsReview = true;
        }
    }

    public void mergeFrom(QuestionDraft other) {
        this.statement = other.statement;
        this.annulled = other.annulled;
        this.correctAlternativeLabel = other.correctAlternativeLabel;
        this.sourcePageNumber = other.sourcePageNumber;
        this.needsReview = other.needsReview;
        this.alternatives.clear();
        for (AlternativeDraft alternative : other.alternatives) {
            addAlternative(new AlternativeDraft(alternative.getLabel(), alternative.getAlternativeText(), alternative.getOrdem()));
        }
        this.images.clear();
        for (QuestionImageDraft image : other.images) {
            addImage(new QuestionImageDraft(image.getStorageKey(), image.getContentType(), image.getOrdem()));
        }
    }

    public int completenessScore() {
        int statementLength = statement != null ? statement.length() : 0;
        return statementLength + (alternatives.size() * 20) + (images.size() * 5);
    }
}
