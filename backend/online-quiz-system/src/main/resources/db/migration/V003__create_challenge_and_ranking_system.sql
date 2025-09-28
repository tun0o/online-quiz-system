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