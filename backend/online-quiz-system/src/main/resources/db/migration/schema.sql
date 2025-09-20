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

-- Enum types
CREATE TYPE challenge_type AS ENUM ('CORRECT_ANSWERS', 'STUDY_TIME_MINUTES', 'COMPLETE_QUIZZES');
CREATE TYPE difficulty_level AS ENUM ('EASY', 'MEDIUM', 'HARD');

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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng thử thách hàng ngày (instance cụ thể cho từng ngày)
CREATE TABLE daily_challenges (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    challenge_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES challenge_templates(id),
    UNIQUE(template_id, challenge_date)
);

-- Bảng tiến độ người dùng cho từng thử thách
CREATE TABLE user_challenge_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    daily_challenge_id BIGINT NOT NULL,
    current_progress INTEGER DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    points_earned INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (daily_challenge_id) REFERENCES daily_challenges(id) ON DELETE CASCADE,
    UNIQUE(user_id, daily_challenge_id)
);

-- Bảng tổng điểm người dùng
CREATE TABLE user_rankings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_points INTEGER DEFAULT 0,
    daily_points INTEGER DEFAULT 0,
    weekly_points INTEGER DEFAULT 0,
    monthly_points INTEGER DEFAULT 0,
    last_activity_date DATE,
    current_streak INTEGER DEFAULT 0,
    max_streak INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lịch sử điểm số hàng ngày
CREATE TABLE daily_point_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points_earned INTEGER NOT NULL,
    activity_date DATE NOT NULL,
    source VARCHAR(50) NOT NULL, -- 'CHALLENGE', 'QUIZ_COMPLETION'
    source_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_rankings(user_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_daily_challenges_date ON daily_challenges(challenge_date);
CREATE INDEX idx_user_challenge_progress_user_date ON user_challenge_progress(user_id, daily_challenge_id);
CREATE INDEX idx_user_rankings_total_points ON user_rankings(total_points DESC);
CREATE INDEX idx_user_rankings_weekly_points ON user_rankings(weekly_points DESC);
CREATE INDEX idx_daily_point_history_user_date ON daily_point_history(user_id, activity_date);

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


-- Thêm cột difficulty_level vào bảng quizzes
ALTER TABLE quiz_submissions ADD COLUMN difficulty_level difficulty_level;

-- Tính toán và cập nhật độ khó cho các quiz hiện có
UPDATE quiz_submissions 
SET difficulty_level = (
    CASE 
        WHEN (
            SELECT AVG(difficulty_level::integer) 
            FROM submission_questions 
            WHERE submission_id = quiz_submissions.id
        ) <= 1.3 THEN 'EASY'::difficulty_level
        WHEN (
            SELECT AVG(difficulty_level::integer) 
            FROM submission_questions 
            WHERE submission_id = quiz_submissions.id
        ) <= 2.3 THEN 'MEDIUM'::difficulty_level
        ELSE 'HARD'::difficulty_level
    END
)
WHERE difficulty_level IS NULL;

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
