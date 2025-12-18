-- Cập nhật enum question_type
CREATE TYPE question_type AS ENUM ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'ESSAY');

-- Bảng lưu câu trả lời essay của người dùng
CREATE TABLE user_essay_answers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    quiz_attempt_id BIGINT,
    answer_text TEXT NOT NULL,
    score DECIMAL(5,2), -- Điểm số do admin chấm (null = chưa chấm)
    max_score DECIMAL(5,2) DEFAULT 10.0, -- Điểm tối đa
    graded_by BIGINT, -- Admin ID người chấm
    graded_at TIMESTAMP,
    admin_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng yêu cầu chấm điểm essay
CREATE TABLE essay_grading_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quiz_attempt_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_to BIGINT, -- Admin được phân công
    completed_at TIMESTAMP,
    total_essay_questions INTEGER DEFAULT 0,
    graded_questions INTEGER DEFAULT 0
);

-- Indexes
CREATE INDEX idx_user_essay_answers_user_question ON user_essay_answers(user_id, question_id);
CREATE INDEX idx_essay_grading_requests_status ON essay_grading_requests(status);
CREATE INDEX idx_essay_grading_requests_assigned ON essay_grading_requests(assigned_to);

-- Cập nhật bảng submission_questions để hỗ trợ essay
ALTER TABLE submission_questions ADD COLUMN max_score DECIMAL(5,2) DEFAULT 10.0;
ALTER TABLE submission_questions ADD COLUMN essay_guidelines TEXT;

ALTER TABLE submission_questions 
ALTER COLUMN question_type TYPE question_type 
USING question_type::question_type;

ALTER TABLE submission_questions 
ALTER COLUMN question_type SET NOT NULL
