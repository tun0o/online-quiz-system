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
CREATE TABLE IF NOT EXISTS quiz_submissions (
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
    approved_at TIMESTAMPTZ,
    approved_by BIGINT REFERENCES users(id)
    );

-- Submission questions
CREATE TABLE IF NOT EXISTS submission_questions (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    submission_id BIGINT NOT NULL REFERENCES quiz_submissions(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) DEFAULT 'MULTIPLE_CHOICE',
    explanation TEXT,
    difficulty_level INTEGER DEFAULT 1
    );

-- Submission answer options
CREATE TABLE IF NOT EXISTS submission_answer_options (
                                                         id BIGSERIAL PRIMARY KEY,
                                                         question_id BIGINT NOT NULL REFERENCES submission_questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE
    );

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
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger t
        JOIN pg_class c ON t.tgrelid = c.oid
        WHERE t.tgname = 'trg_users_updated_at' AND c.relname = 'users'
    ) THEN
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger t
        JOIN pg_class c ON t.tgrelid = c.oid
        WHERE t.tgname = 'trg_quiz_submissions_updated_at' AND c.relname = 'quiz_submissions'
    ) THEN
CREATE TRIGGER trg_quiz_submissions_updated_at
    BEFORE UPDATE ON quiz_submissions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
END IF;
END
$$;

-- 5. SAMPLE DATA (upsert using ON CONFLICT)
INSERT INTO users (email, password_hash, is_verified, name, role)
VALUES ('admin@quiz.com', '$2a$10$hashedpassword', TRUE, 'Admin User', 'ADMIN')
    ON CONFLICT (email) DO UPDATE SET updated_at = now();

INSERT INTO users (email, password_hash, is_verified, name, grade, goal)
VALUES ('user@quiz.com', '$2a$10$hashedpassword', TRUE, 'Test User', '10', 'Improve math skills')
    ON CONFLICT (email) DO UPDATE SET updated_at = now();
