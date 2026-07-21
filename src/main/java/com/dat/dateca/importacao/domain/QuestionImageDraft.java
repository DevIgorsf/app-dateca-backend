package com.dat.dateca.importacao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "question_image_drafts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImageDraft {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_draft_id", nullable = false)
    private QuestionDraft questionDraft;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private int ordem;

    public QuestionImageDraft(String storageKey, String contentType, int ordem) {
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.ordem = ordem;
    }

    void linkToQuestionDraft(QuestionDraft questionDraft) {
        this.questionDraft = questionDraft;
    }
}
