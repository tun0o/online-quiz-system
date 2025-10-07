-- =============================================
-- ONLINE QUIZ SYSTEM - COMPLETE DATABASE SCHEMA
-- Version: 1.0 - Clean & Complete
-- Description: Complete database schema with OAuth2Account support
-- Features: Multiple OAuth2 providers per user, comprehensive indexing
-- =============================================

-- =============================================
-- 1. CORE TABLES
-- =============================================

-- Users table (core authentication data only)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255), -- Can be null for OAuth2 users
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    grade VARCHAR(50),
    goal VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- OAuth2 accounts table (supports multiple providers per user)
CREATE TABLE IF NOT EXISTS oauth2_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    provider_name VARCHAR(255),
    provider_picture TEXT,
    provider_email VARCHAR(255),
    provider_phone VARCHAR(20),
    provider_birthday VARCHAR(20),
    provider_gender VARCHAR(10),
    provider_locale VARCHAR(10),
    is_primary BOOLEAN DEFAULT FALSE,
    last_used_at TIMESTAMP,
    linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, provider),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User profiles table (extended profile information)
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY,
    full_name VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(10),
    province VARCHAR(100),
    school VARCHAR(255),
    grade VARCHAR(50),
    goal VARCHAR(255),
    emergency_phone VARCHAR(20),
    avatar_url VARCHAR(500),
    bio TEXT,
    email VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    
    -- OAuth2 extended info (synced from oauth2_accounts)
    oauth2_phone VARCHAR(20),
    oauth2_birthday VARCHAR(50),
    oauth2_gender VARCHAR(20),
    oauth2_locale VARCHAR(10),
    oauth2_provider VARCHAR(50),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- 2. AUTHENTICATION TABLES
-- =============================================

-- Verification tokens table
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(128) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================
-- 3. QUIZ SYSTEM TABLES
-- =============================================

-- Quiz submissions table
CREATE TABLE IF NOT EXISTS quiz_submissions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject VARCHAR(100) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    contributor_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    admin_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by BIGINT,
    
    FOREIGN KEY (contributor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Submission questions table
CREATE TABLE IF NOT EXISTS submission_questions (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) DEFAULT 'MULTIPLE_CHOICE',
    explanation TEXT,
    difficulty_level INTEGER DEFAULT 1,
    
    FOREIGN KEY (submission_id) REFERENCES quiz_submissions(id) ON DELETE CASCADE
);

-- Submission answer options table
CREATE TABLE IF NOT EXISTS submission_answer_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (question_id) REFERENCES submission_questions(id) ON DELETE CASCADE
);

-- =============================================
-- 4. PERFORMANCE INDEXES
-- =============================================

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_verified ON users(is_verified);

-- OAuth2 accounts indexes
CREATE INDEX IF NOT EXISTS idx_oauth2_accounts_user_id ON oauth2_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_oauth2_accounts_provider ON oauth2_accounts(provider);
CREATE INDEX IF NOT EXISTS idx_oauth2_accounts_provider_id ON oauth2_accounts(provider_id);
CREATE INDEX IF NOT EXISTS idx_oauth2_accounts_is_primary ON oauth2_accounts(is_primary);
CREATE INDEX IF NOT EXISTS idx_oauth2_accounts_last_used_at ON oauth2_accounts(last_used_at);
CREATE INDEX IF NOT EXISTS idx_oauth2_accounts_linked_at ON oauth2_accounts(linked_at);

-- User profiles indexes
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_grade ON user_profiles(grade);
CREATE INDEX IF NOT EXISTS idx_user_profiles_oauth2_provider ON user_profiles(oauth2_provider);

-- Token indexes
CREATE INDEX IF NOT EXISTS idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_expires_at ON verification_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_used ON verification_tokens(used);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_used ON password_reset_tokens(used);

-- Quiz submission indexes
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_contributor_id ON quiz_submissions(contributor_id);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_status ON quiz_submissions(status);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_subject ON quiz_submissions(subject);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_created_at ON quiz_submissions(created_at);

-- Question indexes
CREATE INDEX IF NOT EXISTS idx_submission_questions_submission_id ON submission_questions(submission_id);
CREATE INDEX IF NOT EXISTS idx_submission_questions_question_type ON submission_questions(question_type);
CREATE INDEX IF NOT EXISTS idx_submission_questions_difficulty_level ON submission_questions(difficulty_level);

-- Answer option indexes
CREATE INDEX IF NOT EXISTS idx_submission_answer_options_question_id ON submission_answer_options(question_id);
CREATE INDEX IF NOT EXISTS idx_submission_answer_options_is_correct ON submission_answer_options(is_correct);

-- =============================================
-- 5. TRIGGERS AND FUNCTIONS
-- =============================================

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at
CREATE TRIGGER users_updated_at_trigger
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER oauth2_accounts_updated_at_trigger
    BEFORE UPDATE ON oauth2_accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER user_profiles_updated_at_trigger
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER quiz_submissions_updated_at_trigger
    BEFORE UPDATE ON quiz_submissions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 6. SAMPLE DATA FOR TESTING
-- =============================================

-- Insert admin user
INSERT INTO users (email, password_hash, is_verified, role, grade, goal) VALUES
('admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', true, 'ADMIN', '12', 'Quản lý hệ thống')
ON CONFLICT (email) DO NOTHING;

-- Insert test user
INSERT INTO users (email, password_hash, is_verified, role, grade, goal) VALUES
('user@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', true, 'USER', '11', 'Học tập và phát triển')
ON CONFLICT (email) DO NOTHING;

-- Insert user profiles
INSERT INTO user_profiles (user_id, full_name, email, email_verified, grade, goal) VALUES
(1, 'Admin User', 'admin@example.com', true, '12', 'Quản lý hệ thống'),
(2, 'Test User', 'user@example.com', true, '11', 'Học tập và phát triển')
ON CONFLICT (user_id) DO NOTHING;

-- Insert sample OAuth2 accounts
INSERT INTO oauth2_accounts (user_id, provider, provider_id, provider_name, provider_picture, provider_email, is_primary, linked_at) VALUES
(2, 'google', '1234567890123456789', 'Test User', 'https://lh3.googleusercontent.com/test.jpg', 'user@example.com', true, CURRENT_TIMESTAMP),
(2, 'facebook', '9876543210987654321', 'Test User', 'https://graph.facebook.com/test.jpg', 'user@example.com', false, CURRENT_TIMESTAMP)
ON CONFLICT (user_id, provider) DO NOTHING;

-- =============================================
-- 7. DOCUMENTATION COMMENTS
-- =============================================

-- Table comments
COMMENT ON TABLE users IS 'Core user table (OAuth2 data moved to oauth2_accounts)';
COMMENT ON TABLE oauth2_accounts IS 'OAuth2 authentication accounts - supports multiple providers per user';
COMMENT ON TABLE user_profiles IS 'Extended user profile information with OAuth2 extended data';
COMMENT ON TABLE verification_tokens IS 'Email verification tokens for user registration';
COMMENT ON TABLE password_reset_tokens IS 'Password reset tokens for password recovery';
COMMENT ON TABLE quiz_submissions IS 'Quiz submissions from contributors';
COMMENT ON TABLE submission_questions IS 'Questions within quiz submissions';
COMMENT ON TABLE submission_answer_options IS 'Answer options for questions';

-- Column comments
COMMENT ON COLUMN users.password_hash IS 'Can be null for OAuth2 users';
COMMENT ON COLUMN oauth2_accounts.provider IS 'OAuth2 provider name (google, facebook, github, etc.)';
COMMENT ON COLUMN oauth2_accounts.provider_id IS 'User ID from OAuth2 provider';
COMMENT ON COLUMN oauth2_accounts.provider_name IS 'Display name from OAuth2 provider';
COMMENT ON COLUMN oauth2_accounts.provider_picture IS 'Profile picture URL from OAuth2 provider';
COMMENT ON COLUMN oauth2_accounts.is_primary IS 'Whether this is the primary OAuth2 account for display';
COMMENT ON COLUMN user_profiles.oauth2_phone IS 'Phone number from OAuth2 provider (synced from oauth2_accounts)';
COMMENT ON COLUMN user_profiles.oauth2_birthday IS 'Birthday from OAuth2 provider (synced from oauth2_accounts)';
COMMENT ON COLUMN user_profiles.oauth2_gender IS 'Gender from OAuth2 provider (synced from oauth2_accounts)';
COMMENT ON COLUMN user_profiles.oauth2_locale IS 'Locale from OAuth2 provider (synced from oauth2_accounts)';
COMMENT ON COLUMN user_profiles.oauth2_provider IS 'Primary OAuth2 provider name (synced from oauth2_accounts)';

-- =============================================
-- 8. VALIDATION QUERIES
-- =============================================

-- Verify schema creation
DO $$
DECLARE
    table_count INTEGER;
    index_count INTEGER;
BEGIN
    -- Count tables
    SELECT COUNT(*) INTO table_count FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
    
    -- Count indexes
    SELECT COUNT(*) INTO index_count FROM pg_indexes 
    WHERE schemaname = 'public';
    
    -- Log results
    RAISE NOTICE 'Schema creation completed successfully:';
    RAISE NOTICE 'Tables created: %', table_count;
    RAISE NOTICE 'Indexes created: %', index_count;
    
    -- Validate critical tables exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        RAISE EXCEPTION 'Critical table "users" not found';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'oauth2_accounts') THEN
        RAISE EXCEPTION 'Critical table "oauth2_accounts" not found';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_profiles') THEN
        RAISE EXCEPTION 'Critical table "user_profiles" not found';
    END IF;
    
    RAISE NOTICE 'All critical tables validated successfully';
END $$;