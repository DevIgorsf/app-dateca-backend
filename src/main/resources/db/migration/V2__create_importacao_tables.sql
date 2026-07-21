CREATE TABLE import_jobs (
    id BINARY(16) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_step VARCHAR(255) NULL,
    error_message TEXT NULL,
    created_by_user_id BIGINT NOT NULL,
    published_exam_id BINARY(16) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_import_jobs_status ON import_jobs (status);
CREATE INDEX idx_import_jobs_created_by ON import_jobs (created_by_user_id);

CREATE TABLE exam_drafts (
    id BINARY(16) NOT NULL,
    import_job_id BINARY(16) NOT NULL,
    title VARCHAR(255) NULL,
    institution VARCHAR(255) NULL,
    year INT NULL,
    edition VARCHAR(100) NULL,
    subject_area VARCHAR(255) NULL,
    raw_cover_text TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_exam_draft_import_job UNIQUE (import_job_id),
    CONSTRAINT fk_exam_draft_import_job FOREIGN KEY (import_job_id) REFERENCES import_jobs (id)
);

CREATE TABLE question_drafts (
    id BINARY(16) NOT NULL,
    exam_draft_id BINARY(16) NOT NULL,
    number INT NULL,
    statement TEXT NOT NULL,
    annulled BOOLEAN NOT NULL DEFAULT FALSE,
    correct_alternative_label CHAR(1) NULL,
    source_page_number INT NULL,
    needs_review BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_draft_exam_draft FOREIGN KEY (exam_draft_id) REFERENCES exam_drafts (id)
);

CREATE INDEX idx_question_drafts_exam_draft_ordem ON question_drafts (exam_draft_id, ordem);

CREATE TABLE alternative_drafts (
    id BINARY(16) NOT NULL,
    question_draft_id BINARY(16) NOT NULL,
    label CHAR(1) NOT NULL,
    alternative_text TEXT NOT NULL,
    ordem INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alternative_draft_question_draft FOREIGN KEY (question_draft_id) REFERENCES question_drafts (id)
);

CREATE INDEX idx_alternative_drafts_question_draft_ordem ON alternative_drafts (question_draft_id, ordem);

CREATE TABLE question_image_drafts (
    id BINARY(16) NOT NULL,
    question_draft_id BINARY(16) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    ordem INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_image_draft_question_draft FOREIGN KEY (question_draft_id) REFERENCES question_drafts (id)
);

CREATE INDEX idx_question_image_drafts_question_draft ON question_image_drafts (question_draft_id);
