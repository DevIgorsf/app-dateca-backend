CREATE TABLE exams (
    id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    institution VARCHAR(255) NULL,
    year INT NULL,
    edition VARCHAR(100) NULL,
    subject_area VARCHAR(255) NULL,
    created_by_user_id BIGINT NOT NULL,
    published_at DATETIME(6) NOT NULL,
    source_import_job_id BINARY(16) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_exams_published_at ON exams (published_at);
CREATE INDEX idx_exams_source_import_job ON exams (source_import_job_id);

CREATE TABLE exam_questions (
    id BINARY(16) NOT NULL,
    exam_id BINARY(16) NOT NULL,
    number INT NULL,
    statement TEXT NOT NULL,
    correct_alternative_label CHAR(1) NULL,
    source_page_number INT NULL,
    ordem INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exam_question_exam FOREIGN KEY (exam_id) REFERENCES exams (id)
);

CREATE INDEX idx_exam_questions_exam_ordem ON exam_questions (exam_id, ordem);

CREATE TABLE exam_question_alternatives (
    id BINARY(16) NOT NULL,
    exam_question_id BINARY(16) NOT NULL,
    label CHAR(1) NOT NULL,
    alternative_text TEXT NOT NULL,
    ordem INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exam_question_alternative_exam_question FOREIGN KEY (exam_question_id) REFERENCES exam_questions (id)
);

CREATE INDEX idx_exam_question_alternatives_exam_question_ordem ON exam_question_alternatives (exam_question_id, ordem);

CREATE TABLE exam_question_images (
    id BINARY(16) NOT NULL,
    exam_question_id BINARY(16) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    ordem INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exam_question_image_exam_question FOREIGN KEY (exam_question_id) REFERENCES exam_questions (id)
);

CREATE INDEX idx_exam_question_images_exam_question ON exam_question_images (exam_question_id);
