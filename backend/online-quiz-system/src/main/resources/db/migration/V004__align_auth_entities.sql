-- Align database schema with updated auth entities
-- Idempotent: uses IF EXISTS/IF NOT EXISTS and safe ALTERs

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    grade VARCHAR(100),
    goal VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    provider VARCHAR(20),
    provider_id VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    name VARCHAR(150),
    version BIGINT NOT NULL DEFAULT 0
);

-- Ensure columns exist with proper defaults and constraints
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS grade VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS goal VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(150);
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Add enum-like checks (safe: create if not present)
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_users_provider_enum'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT chk_users_provider_enum
            CHECK (provider IN ('LOCAL'));
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_users_role_enum'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT chk_users_role_enum
            CHECK (role IN ('USER','ADMIN'));
    END IF;
END $$;

-- VERIFICATION TOKENS
CREATE TABLE IF NOT EXISTS verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(128) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE verification_tokens ADD COLUMN IF NOT EXISTS token_hash VARCHAR(128);
ALTER TABLE verification_tokens ALTER COLUMN token_hash SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_verification_tokens_token_hash ON verification_tokens(token_hash);
ALTER TABLE verification_tokens ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE verification_tokens ALTER COLUMN user_id SET NOT NULL;
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_verification_tokens_users' AND table_name = 'verification_tokens'
    ) THEN
        ALTER TABLE verification_tokens
            ADD CONSTRAINT fk_verification_tokens_users
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;
ALTER TABLE verification_tokens ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE verification_tokens ALTER COLUMN expires_at SET NOT NULL;
ALTER TABLE verification_tokens ADD COLUMN IF NOT EXISTS used BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE verification_tokens ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE verification_tokens ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- REFRESH TOKENS
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    device_fingerprint TEXT NOT NULL,
    device_name VARCHAR(100),
    ip_address INET,
    location VARCHAR(100),
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Ensure NOT NULL and defaults
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS user_id BIGINT;
DO $$ BEGIN
    -- backfill NULL user_id to a safe value? No-op: enforce only if all rows non-null
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='refresh_tokens' AND column_name='user_id') THEN
        BEGIN
            ALTER TABLE refresh_tokens ALTER COLUMN user_id SET NOT NULL;
        EXCEPTION WHEN others THEN
            -- Leave as-is if existing data prevents NOT NULL; handle manually
            NULL;
        END;
    END IF;
END $$;

ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255);
ALTER TABLE refresh_tokens ALTER COLUMN token_hash SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_tokens_token_hash ON refresh_tokens(token_hash);
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS device_fingerprint TEXT;
ALTER TABLE refresh_tokens ALTER COLUMN device_fingerprint SET NOT NULL;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS device_name VARCHAR(100);
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS ip_address INET;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS location VARCHAR(100);

-- Convert timestamptz -> timestamp (store local timestamps) safely if needed
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='refresh_tokens' AND column_name='expires_at' AND data_type='timestamp with time zone'
    ) THEN
        ALTER TABLE refresh_tokens
            ALTER COLUMN expires_at TYPE timestamp USING expires_at AT TIME ZONE 'UTC';
    END IF;
END $$;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE refresh_tokens ALTER COLUMN expires_at SET NOT NULL;

DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='refresh_tokens' AND column_name='last_used_at' AND data_type='timestamp with time zone'
    ) THEN
        ALTER TABLE refresh_tokens
            ALTER COLUMN last_used_at TYPE timestamp USING last_used_at AT TIME ZONE 'UTC';
    END IF;
END $$;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP;

DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='refresh_tokens' AND column_name='created_at' AND data_type='timestamp with time zone'
    ) THEN
        ALTER TABLE refresh_tokens
            ALTER COLUMN created_at TYPE timestamp USING created_at AT TIME ZONE 'UTC';
    END IF;
END $$;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE refresh_tokens ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS is_revoked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- LOGIN SESSIONS
CREATE TABLE IF NOT EXISTS login_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_fingerprint TEXT,
    ip_address INET,
    user_agent VARCHAR(512),
    location VARCHAR(100),
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    risk_score NUMERIC(5,2),
    flagged_reasons TEXT[],
    version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE login_sessions ADD COLUMN IF NOT EXISTS user_id BIGINT;
DO $$ BEGIN
    -- Enforce NOT NULL if data allows
    BEGIN
        ALTER TABLE login_sessions ALTER COLUMN user_id SET NOT NULL;
    EXCEPTION WHEN others THEN
        NULL;
    END;
END $$;
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_login_sessions_users' AND table_name = 'login_sessions'
    ) THEN
        ALTER TABLE login_sessions
            ADD CONSTRAINT fk_login_sessions_users
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- risk_score precision change to NUMERIC(5,2) if previously different
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='login_sessions' AND column_name='risk_score' AND
              (numeric_precision, numeric_scale) IS DISTINCT FROM (5, 2)
    ) THEN
        ALTER TABLE login_sessions ALTER COLUMN risk_score TYPE NUMERIC(5,2);
    END IF;
END $$;

-- Convert timestamptz -> timestamp for login/logout times
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='login_sessions' AND column_name='login_time' AND data_type='timestamp with time zone'
    ) THEN
        ALTER TABLE login_sessions
            ALTER COLUMN login_time TYPE timestamp USING login_time AT TIME ZONE 'UTC';
    END IF;
END $$;
ALTER TABLE login_sessions ADD COLUMN IF NOT EXISTS login_time TIMESTAMP;
ALTER TABLE login_sessions ALTER COLUMN login_time SET NOT NULL;
ALTER TABLE login_sessions ALTER COLUMN login_time SET DEFAULT CURRENT_TIMESTAMP;

DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='login_sessions' AND column_name='logout_time' AND data_type='timestamp with time zone'
    ) THEN
        ALTER TABLE login_sessions
            ALTER COLUMN logout_time TYPE timestamp USING logout_time AT TIME ZONE 'UTC';
    END IF;
END $$;
ALTER TABLE login_sessions ADD COLUMN IF NOT EXISTS logout_time TIMESTAMP;

ALTER TABLE login_sessions ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;





