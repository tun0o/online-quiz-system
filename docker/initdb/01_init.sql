-- Initialize schema for Online Quiz System (idempotent)
-- Will run only on first container init (empty data dir)

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    grade VARCHAR(100),
    goal VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(20) CHECK (provider IN ('LOCAL')),
    provider_id VARCHAR(255),
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    name VARCHAR(150),
    version BIGINT NOT NULL DEFAULT 0
);

-- Email verification tokens (hash stored)
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(128) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Quiz submissions
CREATE TABLE IF NOT EXISTS quiz_submissions (
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

CREATE TABLE IF NOT EXISTS submission_questions (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES quiz_submissions(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(20) DEFAULT 'MULTIPLE_CHOICE',
    explanation TEXT,
    difficulty_level INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS submission_answer_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES submission_questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_quiz_submissions_status ON quiz_submissions(status);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_contributor ON quiz_submissions(contributor_id);

-- Refresh tokens for device management
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    device_fingerprint TEXT NOT NULL,
    device_name VARCHAR(100),
    ip_address VARCHAR(45) NOT NULL,
    location VARCHAR(100),
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Login sessions for analytics
CREATE TABLE IF NOT EXISTS login_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_fingerprint TEXT,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512),
    location VARCHAR(100),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    risk_score NUMERIC(5,2),
    flagged_reasons TEXT,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    device VARCHAR(100),
    device_fingerprint VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Auth-specific audit logs (separate table used by AuthAuditLog entity)
CREATE TABLE IF NOT EXISTS auth_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    device_fingerprint VARCHAR(64),
    ip_address VARCHAR(45),
    user_agent VARCHAR(200),
    success BOOLEAN NOT NULL DEFAULT FALSE,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Essential performance indexes for auth tables
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_expires ON verification_tokens(expires_at) WHERE used = false;

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_revoked ON refresh_tokens(expires_at, is_revoked) WHERE is_revoked = false;
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_device ON refresh_tokens(user_id, device_fingerprint);

CREATE INDEX IF NOT EXISTS idx_login_sessions_user_time ON login_sessions(user_id, login_time DESC);
CREATE INDEX IF NOT EXISTS idx_login_sessions_login_time ON login_sessions(login_time DESC);

-- Composite unique constraint for active device tokens (one active token per device)
CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_tokens_user_device_active
ON refresh_tokens(user_id, device_fingerprint)
WHERE is_revoked = false;