-- Bảng lưu đề thi được đóng góp
CREATE TABLE quiz_submissions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject VARCHAR(50) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    contributor_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    admin_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by BIGINT
);

-- Bảng câu hỏi trong đề đóng góp
CREATE TABLE submission_questions (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(20) DEFAULT 'MULTIPLE_CHOICE',
    explanation TEXT,
    difficulty_level INTEGER DEFAULT 1,
    FOREIGN KEY (submission_id) REFERENCES quiz_submissions(id) ON DELETE CASCADE
);

-- Bảng đáp án cho câu hỏi
CREATE TABLE submission_answer_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES submission_questions(id) ON DELETE CASCADE
);

-- Index để tăng performance
CREATE INDEX idx_quiz_submissions_status ON quiz_submissions(status);
CREATE INDEX idx_quiz_submissions_contributor ON quiz_submissions(contributor_id);