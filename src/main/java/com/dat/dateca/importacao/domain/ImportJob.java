package com.dat.dateca.importacao.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "import_jobs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImportJob {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;

    @Column(name = "current_step")
    private String currentStep;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "published_exam_id")
    private UUID publishedExamId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "importJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ExamDraft examDraft;

    public ImportJob(String originalFilename, long fileSizeBytes, String storageKey, Long createdByUserId) {
        this.originalFilename = originalFilename;
        this.fileSizeBytes = fileSizeBytes;
        this.storageKey = storageKey;
        this.createdByUserId = createdByUserId;
        this.status = ImportStatus.PENDENTE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void advanceTo(ImportStatus status, String currentStep) {
        this.status = status;
        this.currentStep = currentStep;
        this.updatedAt = LocalDateTime.now();
    }

    public void markError(String message) {
        this.status = ImportStatus.ERRO;
        this.errorMessage = message;
        this.updatedAt = LocalDateTime.now();
    }

    public void attachDraft(ExamDraft draft) {
        this.examDraft = draft;
        draft.linkToImportJob(this);
        this.status = ImportStatus.AGUARDANDO_REVISAO;
        this.currentStep = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPublished(UUID examId) {
        this.status = ImportStatus.PUBLICADO;
        this.publishedExamId = examId;
        this.updatedAt = LocalDateTime.now();
    }

    public void markCancelled() {
        this.status = ImportStatus.CANCELADO;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isReadyForReview() {
        return status == ImportStatus.AGUARDANDO_REVISAO;
    }

    public boolean isPublished() {
        return status == ImportStatus.PUBLICADO;
    }
}
