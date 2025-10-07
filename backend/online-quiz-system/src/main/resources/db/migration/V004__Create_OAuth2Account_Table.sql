-- Create OAuth2Account Table and Migrate Data (Safe Version)
-- Version: V004
-- Description: Create OAuth2Account table and migrate OAuth2 data from User table
-- This enables multiple OAuth2 providers per user

-- =============================================
-- 1. CREATE OAUTH2_ACCOUNTS TABLE (IF NOT EXISTS)
-- =============================================

-- Create table only if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'oauth2_accounts') THEN
        CREATE TABLE oauth2_accounts (
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
        
        RAISE NOTICE 'Created oauth2_accounts table';
    ELSE
        RAISE NOTICE 'oauth2_accounts table already exists, skipping creation';
    END IF;
END $$;

-- =============================================
-- 2. CREATE INDEXES FOR PERFORMANCE (IF NOT EXISTS)
-- =============================================

-- Create indexes only if they don't exist
DO $$
BEGIN
    -- Check and create user_id index
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_oauth2_accounts_user_id') THEN
        CREATE INDEX idx_oauth2_accounts_user_id ON oauth2_accounts (user_id);
        RAISE NOTICE 'Created index idx_oauth2_accounts_user_id';
    END IF;
    
    -- Check and create provider index
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_oauth2_accounts_provider') THEN
        CREATE INDEX idx_oauth2_accounts_provider ON oauth2_accounts (provider);
        RAISE NOTICE 'Created index idx_oauth2_accounts_provider';
    END IF;
    
    -- Check and create provider_id index
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_oauth2_accounts_provider_id') THEN
        CREATE INDEX idx_oauth2_accounts_provider_id ON oauth2_accounts (provider_id);
        RAISE NOTICE 'Created index idx_oauth2_accounts_provider_id';
    END IF;
    
    -- Check and create is_primary index
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_oauth2_accounts_is_primary') THEN
        CREATE INDEX idx_oauth2_accounts_is_primary ON oauth2_accounts (is_primary);
        RAISE NOTICE 'Created index idx_oauth2_accounts_is_primary';
    END IF;
    
    -- Check and create last_used_at index
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_oauth2_accounts_last_used_at') THEN
        CREATE INDEX idx_oauth2_accounts_last_used_at ON oauth2_accounts (last_used_at);
        RAISE NOTICE 'Created index idx_oauth2_accounts_last_used_at';
    END IF;
    
    -- Check and create linked_at index
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_oauth2_accounts_linked_at') THEN
        CREATE INDEX idx_oauth2_accounts_linked_at ON oauth2_accounts (linked_at);
        RAISE NOTICE 'Created index idx_oauth2_accounts_linked_at';
    END IF;
END $$;

-- =============================================
-- 3. MIGRATE EXISTING OAUTH2 DATA (IF NEEDED)
-- =============================================

-- Only migrate if there are users with OAuth2 data and no OAuth2 accounts exist
DO $$
DECLARE
    oauth2_users_count INTEGER;
    oauth2_accounts_count INTEGER;
    has_oauth2_columns BOOLEAN;
BEGIN
    -- Check if OAuth2 columns exist in users table
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'provider'
    ) INTO has_oauth2_columns;
    
    -- Count users with OAuth2 data (only if columns exist)
    IF has_oauth2_columns THEN
        SELECT COUNT(*) INTO oauth2_users_count FROM users 
        WHERE provider IS NOT NULL AND provider_id IS NOT NULL;
    ELSE
        oauth2_users_count := 0;
    END IF;
    
    -- Count existing OAuth2 accounts
    SELECT COUNT(*) INTO oauth2_accounts_count FROM oauth2_accounts;
    
    -- Only migrate if there are OAuth2 users but no OAuth2 accounts
    IF oauth2_users_count > 0 AND oauth2_accounts_count = 0 THEN
        RAISE NOTICE 'Migrating OAuth2 data for % users', oauth2_users_count;
        
        -- Migrate OAuth2 data from users table to oauth2_accounts table
        INSERT INTO oauth2_accounts (
            user_id, 
            provider, 
            provider_id, 
            provider_name, 
            provider_picture, 
            provider_email,
            is_primary, 
            linked_at,
            last_used_at
        )
        SELECT 
            id, 
            provider, 
            provider_id, 
            oauth2_name, 
            oauth2_picture, 
            email,
            true, -- All existing accounts are primary
            created_at,
            updated_at
        FROM users 
        WHERE provider IS NOT NULL 
          AND provider_id IS NOT NULL;
        
        RAISE NOTICE 'Migrated % OAuth2 accounts', oauth2_users_count;
    ELSE
        RAISE NOTICE 'Skipping OAuth2 data migration - users: %, accounts: %, has_oauth2_columns: %', 
                     oauth2_users_count, oauth2_accounts_count, has_oauth2_columns;
    END IF;
END $$;

-- =============================================
-- 4. UPDATE USER_PROFILES WITH OAUTH2 DATA (IF NEEDED)
-- =============================================

-- Update UserProfile with OAuth2 data from OAuth2Account if needed
DO $$
DECLARE
    updated_count INTEGER;
BEGIN
    -- Update UserProfile with OAuth2 data from OAuth2Account
    UPDATE user_profiles 
    SET 
        full_name = COALESCE(oa.provider_name, user_profiles.full_name),
        avatar_url = COALESCE(oa.provider_picture, user_profiles.avatar_url),
        oauth2_provider = oa.provider,
        oauth2_phone = oa.provider_phone,
        oauth2_birthday = oa.provider_birthday,
        oauth2_gender = oa.provider_gender,
        oauth2_locale = oa.provider_locale
    FROM oauth2_accounts oa
    WHERE user_profiles.user_id = oa.user_id 
      AND oa.is_primary = true
      AND (user_profiles.full_name IS NULL OR user_profiles.avatar_url IS NULL);
    
    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RAISE NOTICE 'Updated % user profiles with OAuth2 data', updated_count;
END $$;

-- =============================================
-- 5. DROP OLD OAUTH2 COLUMNS FROM USERS TABLE (IF EXISTS)
-- =============================================

-- Drop OAuth2 columns from users table if they exist
DO $$
BEGIN
    -- Check and drop provider column
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'provider') THEN
        ALTER TABLE users DROP COLUMN provider;
        RAISE NOTICE 'Dropped provider column from users table';
    END IF;
    
    -- Check and drop provider_id column
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'provider_id') THEN
        ALTER TABLE users DROP COLUMN provider_id;
        RAISE NOTICE 'Dropped provider_id column from users table';
    END IF;
    
    -- Check and drop oauth2_name column
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'oauth2_name') THEN
        ALTER TABLE users DROP COLUMN oauth2_name;
        RAISE NOTICE 'Dropped oauth2_name column from users table';
    END IF;
    
    -- Check and drop oauth2_picture column
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'oauth2_picture') THEN
        ALTER TABLE users DROP COLUMN oauth2_picture;
        RAISE NOTICE 'Dropped oauth2_picture column from users table';
    END IF;
END $$;

-- =============================================
-- 6. CREATE TRIGGER FOR UPDATED_AT (IF NOT EXISTS)
-- =============================================

-- Create trigger function if it doesn't exist
CREATE OR REPLACE FUNCTION update_oauth2_accounts_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for updated_at if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'oauth2_accounts_updated_at_trigger') THEN
        CREATE TRIGGER oauth2_accounts_updated_at_trigger
            BEFORE UPDATE ON oauth2_accounts
            FOR EACH ROW
            EXECUTE FUNCTION update_oauth2_accounts_updated_at_column();
        RAISE NOTICE 'Created oauth2_accounts_updated_at_trigger';
    ELSE
        RAISE NOTICE 'oauth2_accounts_updated_at_trigger already exists';
    END IF;
END $$;

-- =============================================
-- 7. ADD COMMENTS FOR DOCUMENTATION
-- =============================================

-- Add comments only if they don't exist
DO $$
BEGIN
    -- Add table comment
    IF NOT EXISTS (SELECT 1 FROM pg_description WHERE objoid = 'oauth2_accounts'::regclass AND objsubid = 0) THEN
        COMMENT ON TABLE oauth2_accounts IS 'OAuth2 authentication accounts - supports multiple providers per user';
    END IF;
    
    -- Add column comments
    COMMENT ON COLUMN oauth2_accounts.user_id IS 'Foreign key reference to users.id';
    COMMENT ON COLUMN oauth2_accounts.provider IS 'OAuth2 provider name (google, facebook, github, etc.)';
    COMMENT ON COLUMN oauth2_accounts.provider_id IS 'User ID from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.provider_name IS 'Display name from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.provider_picture IS 'Profile picture URL from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.provider_email IS 'Email from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.provider_phone IS 'Phone number from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.provider_birthday IS 'Birthday from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.provider_gender IS 'Gender from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.provider_locale IS 'Locale from OAuth2 provider';
    COMMENT ON COLUMN oauth2_accounts.is_primary IS 'Whether this is the primary OAuth2 account for display';
    COMMENT ON COLUMN oauth2_accounts.last_used_at IS 'Last time this OAuth2 account was used';
    COMMENT ON COLUMN oauth2_accounts.linked_at IS 'When this OAuth2 account was linked';
END $$;

-- =============================================
-- 8. VALIDATION QUERIES
-- =============================================

-- Verify migration was successful
DO $$
DECLARE
    user_count INTEGER;
    oauth2_count INTEGER;
    profile_count INTEGER;
BEGIN
    -- Count users with OAuth2 data
    SELECT COUNT(*) INTO user_count FROM users WHERE id IN (
        SELECT DISTINCT user_id FROM oauth2_accounts
    );
    
    -- Count OAuth2 accounts
    SELECT COUNT(*) INTO oauth2_count FROM oauth2_accounts;
    
    -- Count user profiles
    SELECT COUNT(*) INTO profile_count FROM user_profiles WHERE user_id IN (
        SELECT DISTINCT user_id FROM oauth2_accounts
    );
    
    -- Log results
    RAISE NOTICE 'Migration completed successfully:';
    RAISE NOTICE 'Users with OAuth2: %', user_count;
    RAISE NOTICE 'OAuth2 accounts: %', oauth2_count;
    RAISE NOTICE 'User profiles: %', profile_count;
    
    -- Validate data integrity
    IF oauth2_count = 0 THEN
        RAISE WARNING 'No OAuth2 accounts found - migration may have failed';
    END IF;
    
    IF user_count != profile_count AND profile_count > 0 THEN
        RAISE WARNING 'User count (%) does not match profile count (%)', user_count, profile_count;
    END IF;
END $$;