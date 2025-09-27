-- Bảng lưu đề thi được đóng góp
create TABLE quiz_submissions (
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
create TABLE submission_questions (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(20) DEFAULT 'MULTIPLE_CHOICE',
    explanation TEXT,
    difficulty_level INTEGER DEFAULT 1,
    FOREIGN KEY (submission_id) REFERENCES quiz_submissions(id) ON delete CASCADE
);

-- Bảng đáp án cho câu hỏi
create TABLE submission_answer_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES submission_questions(id) ON delete CASCADE
);

-- Index để tăng performance
create index idx_quiz_submissions_status on quiz_submissions(status);
create index idx_quiz_submissions_contributor on quiz_submissions(contributor_id);