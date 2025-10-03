-- Bảng lưu đề thi được đóng góp
-- 1. CREATE TYPES (ENUM)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'role_enum') THEN
        CREATE TYPE role_enum AS ENUM ('USER', 'ADMIN');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subject_enum') THEN
        CREATE TYPE subject_enum AS ENUM ('MATH', 'PHYSICS', 'CHEMISTRY', 'BIOLOGY', 'LITERATURE', 'ENGLISH');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'status_enum') THEN
        CREATE TYPE status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'challenge_type') THEN
        CREATE TYPE challenge_type AS ENUM ('CORRECT_ANSWERS', 'STUDY_TIME_MINUTES', 'COMPLETE_QUIZZES');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'difficulty_level') THEN
        CREATE TYPE difficulty_level AS ENUM ('EASY', 'MEDIUM', 'HARD');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'question_type') THEN
        CREATE TYPE question_type AS ENUM ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'ESSAY');
    END IF;
END $$;

-- 2. CREATE TABLES

-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    grade VARCHAR(10),
    goal TEXT,
    name VARCHAR(100),
    provider VARCHAR(50),
    provider_id VARCHAR(100),
    role role_enum NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Verification tokens
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Quiz submissions
CREATE TABLE quiz_submissions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject subject_enum NOT NULL,
    duration_minutes INTEGER NOT NULL,
    contributor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status status_enum DEFAULT 'PENDING',
    admin_feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    approved_at TIMESTAMP,
    approved_by BIGINT REFERENCES users(id) ON DELETE SET NULL
);

-- Bảng câu hỏi trong đề đóng góp
CREATE TABLE submission_questions (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,    
    question_type question_type NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    explanation TEXT,
    difficulty_level difficulty_level DEFAULT 'EASY',
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

-- Bảng template thử thách (định nghĩa các loại thử thách có thể có)
CREATE TABLE challenge_templates (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    challenge_type challenge_type NOT NULL,
    difficulty_level difficulty_level NOT NULL,
    target_value INTEGER NOT NULL,
    reward_points INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Bảng thử thách hàng ngày (instance cụ thể cho từng ngày)
CREATE TABLE daily_challenges (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    challenge_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (template_id) REFERENCES challenge_templates(id) ON DELETE CASCADE,
    UNIQUE(template_id, challenge_date)
);

-- Bảng tiến độ người dùng cho từng thử thách
CREATE TABLE user_challenge_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    daily_challenge_id BIGINT NOT NULL,
    current_progress INTEGER DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    points_earned INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (daily_challenge_id) REFERENCES daily_challenges(id) ON DELETE CASCADE,
    UNIQUE(user_id, daily_challenge_id)
);

-- Bảng tổng điểm người dùng
CREATE TABLE user_rankings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    total_points INTEGER DEFAULT 0,
    daily_points INTEGER DEFAULT 0,
    weekly_points INTEGER DEFAULT 0,
    monthly_points INTEGER DEFAULT 0,
    last_activity_date DATE,
    current_streak INTEGER DEFAULT 0,
    max_streak INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Bảng lịch sử điểm số hàng ngày
CREATE TABLE daily_point_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_rankings(user_id) ON DELETE CASCADE,
    points_earned INTEGER NOT NULL,
    activity_date DATE NOT NULL,
    source VARCHAR(50) NOT NULL, -- 'CHALLENGE', 'QUIZ_COMPLETION'
    source_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Insert default challenge templates (3 levels per day)
INSERT INTO challenge_templates (title, description, challenge_type, difficulty_level, target_value, reward_points) VALUES
-- Easy challenges
('Trả lời đúng 5 câu', 'Trả lời đúng 5 câu hỏi bất kỳ trong ngày', 'CORRECT_ANSWERS', 'EASY', 5, 10),
('Học 5 phút', 'Dành 5 phút để làm bài tập', 'STUDY_TIME_MINUTES', 'EASY', 5, 10),
('Hoàn thành 1 đề thi', 'Hoàn thành 1 đề thi bất kỳ', 'COMPLETE_QUIZZES', 'EASY', 1, 15),

-- Medium challenges  
('Trả lời đúng 10 câu', 'Trả lời đúng 10 câu hỏi bất kỳ trong ngày', 'CORRECT_ANSWERS', 'MEDIUM', 10, 25),
('Học 15 phút', 'Dành 15 phút để làm bài tập', 'STUDY_TIME_MINUTES', 'MEDIUM', 15, 25),
('Hoàn thành 3 đề thi', 'Hoàn thành 3 đề thi bất kỳ', 'COMPLETE_QUIZZES', 'MEDIUM', 3, 30),

-- Hard challenges
('Trả lời đúng 20 câu', 'Trả lời đúng 20 câu hỏi bất kỳ trong ngày', 'CORRECT_ANSWERS', 'HARD', 20, 50),
('Học 30 phút', 'Dành 30 phút để làm bài tập', 'STUDY_TIME_MINUTES', 'HARD', 30, 50),
('Hoàn thành 5 đề thi', 'Hoàn thành 5 đề thi bất kỳ', 'COMPLETE_QUIZZES', 'HARD', 5, 60);

-- Bảng lưu câu trả lời essay của người dùng
CREATE TABLE user_essay_answers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL,
    quiz_attempt_id BIGINT,
    answer_text TEXT NOT NULL,
    score DECIMAL(5,2), -- Điểm số do admin chấm (null = chưa chấm)
    max_score DECIMAL(5,2) DEFAULT 10.0, -- Điểm tối đa
    graded_by BIGINT REFERENCES users(id) ON DELETE SET NULL, -- Admin ID người chấm
    graded_at TIMESTAMP,
    admin_feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Bảng yêu cầu chấm điểm essay
CREATE TABLE essay_grading_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quiz_attempt_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_to BIGINT REFERENCES users(id) ON DELETE SET NULL, -- Admin được phân công
    completed_at TIMESTAMP,
    total_essay_questions INTEGER DEFAULT 0,
    graded_questions INTEGER DEFAULT 0
);

-- Cập nhật bảng submission_questions để hỗ trợ essay
ALTER TABLE submission_questions ADD COLUMN max_score DECIMAL(5,2) DEFAULT 10.0;
ALTER TABLE submission_questions ADD COLUMN essay_guidelines TEXT;

-- Thêm cột difficulty_level vào bảng quizzes
ALTER TABLE quiz_submissions ADD COLUMN difficulty_level difficulty_level;

-- Tính toán và cập nhật độ khó cho các quiz hiện có
UPDATE quiz_submissions 
SET difficulty_level = (
    CASE 
        WHEN ( -- Assuming EASY=1, MEDIUM=2, HARD=3 for calculation
            SELECT AVG(CASE difficulty_level WHEN 'EASY' THEN 1.0 WHEN 'MEDIUM' THEN 2.0 ELSE 3.0 END)
            FROM submission_questions 
            WHERE submission_id = quiz_submissions.id
        ) <= 1.5 THEN 'EASY'::difficulty_level
        WHEN (
            SELECT AVG(CASE difficulty_level WHEN 'EASY' THEN 1.0 WHEN 'MEDIUM' THEN 2.0 ELSE 3.0 END)
            FROM submission_questions 
            WHERE submission_id = quiz_submissions.id
        ) <= 2.3 THEN 'MEDIUM'::difficulty_level
        ELSE 'HARD'::difficulty_level
    END
)
WHERE difficulty_level IS NULL;

-- Bảng lưu lại mỗi lần người dùng làm một đề thi
CREATE TABLE quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quiz_submission_id BIGINT NOT NULL REFERENCES quiz_submissions(id) ON DELETE SET NULL,
    start_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    end_time TIMESTAMP,
    score DECIMAL(5, 2) DEFAULT 0.0,
    total_questions INTEGER NOT NULL,
    correct_answers INTEGER DEFAULT 0,
    status VARCHAR(20) NOT NULL, -- 'IN_PROGRESS', 'COMPLETED'
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Bảng lưu chi tiết câu trả lời của người dùng cho mỗi câu hỏi trong một lần làm bài
CREATE TABLE user_answers (
    id BIGSERIAL PRIMARY KEY,
    quiz_attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_option_id BIGINT, -- Dùng cho trắc nghiệm & đúng/sai
    answer_text TEXT,          -- Dùng cho tự luận
    is_correct BOOLEAN,        -- Null nếu là câu tự luận chưa chấm
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES submission_questions(id) ON DELETE CASCADE,
    FOREIGN KEY (selected_option_id) REFERENCES submission_answer_options(id) ON DELETE SET NULL,
    UNIQUE(quiz_attempt_id, question_id) -- Mỗi câu hỏi chỉ được trả lời 1 lần trong 1 attempt
);

-- Thêm các cột cần thiết để chấm điểm trực tiếp trên bảng user_answers
ALTER TABLE user_answers ADD COLUMN score DECIMAL(5, 2);
ALTER TABLE user_answers ADD COLUMN admin_feedback TEXT;

-- 3. INDEXES
CREATE INDEX IF NOT EXISTS idx_users_provider ON users(provider, provider_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_verified ON users(is_verified);

CREATE INDEX IF NOT EXISTS idx_verification_tokens_hash ON verification_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_user ON verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_expires ON verification_tokens(expires_at);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_hash ON password_reset_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires ON password_reset_tokens(expires_at);

CREATE INDEX IF NOT EXISTS idx_quiz_submissions_contributor ON quiz_submissions(contributor_id);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_status ON quiz_submissions(status);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_subject ON quiz_submissions(subject);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_created ON quiz_submissions(created_at);

CREATE INDEX IF NOT EXISTS idx_submission_questions_submission ON submission_questions(submission_id);
CREATE INDEX IF NOT EXISTS idx_submission_questions_type ON submission_questions(question_type);

CREATE INDEX IF NOT EXISTS idx_submission_answer_options_question ON submission_answer_options(question_id);
CREATE INDEX IF NOT EXISTS idx_submission_answer_options_correct ON submission_answer_options(is_correct);

CREATE INDEX IF NOT EXISTS idx_daily_challenges_date ON daily_challenges(challenge_date);
CREATE INDEX IF NOT EXISTS idx_user_challenge_progress_user_date ON user_challenge_progress(user_id, daily_challenge_id);
CREATE INDEX IF NOT EXISTS idx_user_rankings_total_points ON user_rankings(total_points DESC);
CREATE INDEX IF NOT EXISTS idx_user_rankings_weekly_points ON user_rankings(weekly_points DESC);
CREATE INDEX IF NOT EXISTS idx_daily_point_history_user_date ON daily_point_history(user_id, activity_date);

CREATE INDEX IF NOT EXISTS idx_user_essay_answers_user_question ON user_essay_answers(user_id, question_id);
CREATE INDEX IF NOT EXISTS idx_essay_grading_requests_status ON essay_grading_requests(status);
CREATE INDEX IF NOT EXISTS idx_essay_grading_requests_assigned ON essay_grading_requests(assigned_to);

CREATE INDEX IF NOT EXISTS idx_quiz_attempts_user ON quiz_attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_user_answers_attempt ON user_answers(quiz_attempt_id);

-- 4. TRIGGER FUNCTION to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach trigger to tables that have updated_at
DO $$
BEGIN
    -- Attach to users
    IF NOT EXISTS (SELECT 1 FROM pg_trigger t JOIN pg_class c ON t.tgrelid = c.oid WHERE t.tgname = 'trg_users_updated_at' AND c.relname = 'users') THEN
        CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    -- Attach to quiz_submissions
    IF NOT EXISTS (SELECT 1 FROM pg_trigger t JOIN pg_class c ON t.tgrelid = c.oid WHERE t.tgname = 'trg_quiz_submissions_updated_at' AND c.relname = 'quiz_submissions') THEN
        CREATE TRIGGER trg_quiz_submissions_updated_at BEFORE UPDATE ON quiz_submissions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    -- Attach to user_challenge_progress
    IF NOT EXISTS (SELECT 1 FROM pg_trigger t JOIN pg_class c ON t.tgrelid = c.oid WHERE t.tgname = 'trg_user_challenge_progress_updated_at' AND c.relname = 'user_challenge_progress') THEN
        CREATE TRIGGER trg_user_challenge_progress_updated_at BEFORE UPDATE ON user_challenge_progress FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    -- Attach to user_rankings
    IF NOT EXISTS (SELECT 1 FROM pg_trigger t JOIN pg_class c ON t.tgrelid = c.oid WHERE t.tgname = 'trg_user_rankings_updated_at' AND c.relname = 'user_rankings') THEN
        CREATE TRIGGER trg_user_rankings_updated_at BEFORE UPDATE ON user_rankings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END;
$$;
