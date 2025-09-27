-- Refresh tokens table for device management
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    device_fingerprint TEXT NOT NULL,
    device_name VARCHAR(100),
    ip_address VARCHAR(45),
    location VARCHAR(100),
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    last_used_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Login sessions table for analytics
CREATE TABLE IF NOT EXISTS login_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    device_fingerprint TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    location VARCHAR(100),
    login_time TIMESTAMPTZ DEFAULT NOW(),
    logout_time TIMESTAMPTZ,
    risk_score NUMERIC(3,2) DEFAULT 0.0,
    flagged_reasons TEXT[]
);
