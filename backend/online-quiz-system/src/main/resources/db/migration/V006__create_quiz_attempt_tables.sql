-- File: V006__create_quiz_attempt_tables.sql

-- Bảng lưu lại mỗi lần người dùng làm một đề thi
CREATE TABLE quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_submission_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    score DECIMAL(5, 2) DEFAULT 0.0,
    total_questions INTEGER NOT NULL,
    correct_answers INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL, -- 'IN_PROGRESS', 'COMPLETED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_submission_id) REFERENCES quiz_submissions(id) ON DELETE SET NULL
);

-- Bảng lưu chi tiết câu trả lời của người dùng cho mỗi câu hỏi trong một lần làm bài
CREATE TABLE user_answers (
    id BIGSERIAL PRIMARY KEY,
    quiz_attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT, -- Dùng cho trắc nghiệm & đúng/sai
    answer_text TEXT,          -- Dùng cho tự luận
    is_correct BOOLEAN,        -- Null nếu là câu tự luận chưa chấm
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES submission_questions(id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES submission_answer_options(id) ON DELETE SET NULL,
    UNIQUE(quiz_attempt_id, question_id) -- Mỗi câu hỏi chỉ được trả lời 1 lần trong 1 attempt
);

-- Indexes để tăng tốc độ truy vấn
CREATE INDEX idx_quiz_attempts_user ON quiz_attempts(user_id);
CREATE INDEX idx_user_answers_attempt ON user_answers(quiz_attempt_id);
