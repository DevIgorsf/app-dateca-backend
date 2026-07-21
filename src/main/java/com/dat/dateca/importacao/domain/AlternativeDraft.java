package com.dat.dateca.importacao.domain;

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
@Table(name = "alternative_drafts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlternativeDraft {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_draft_id", nullable = false)
    private QuestionDraft questionDraft;

    @Column(nullable = false)
    private Character label;

    @Lob
    @Column(name = "alternative_text", columnDefinition = "TEXT", nullable = false)
    private String alternativeText;

    @Column(nullable = false)
    private int ordem;

    public AlternativeDraft(Character label, String alternativeText, int ordem) {
        this.label = label;
        this.alternativeText = alternativeText;
        this.ordem = ordem;
    }

    void linkToQuestionDraft(QuestionDraft questionDraft) {
        this.questionDraft = questionDraft;
    }
}
