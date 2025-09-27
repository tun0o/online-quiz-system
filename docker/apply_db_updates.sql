-- Safe database update script to align DB schema with Entities (non-destructive)
DO $$
BEGIN
  -- Add device column to audit_logs if missing
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='audit_logs' AND column_name='device') THEN
    ALTER TABLE audit_logs ADD COLUMN device VARCHAR(100);
  END IF;

  -- Rename details -> description if exists
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='audit_logs' AND column_name='details')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='audit_logs' AND column_name='description') THEN
    ALTER TABLE audit_logs RENAME COLUMN details TO description;
  END IF;

  -- Create auth_audit_logs table if not exists
  IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='auth_audit_logs') THEN
    CREATE TABLE auth_audit_logs (
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
  END IF;

  -- Create missing indexes if not present
  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename='refresh_tokens' AND indexname='idx_refresh_tokens_token_hash') THEN
    CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename='refresh_tokens' AND indexname='idx_refresh_tokens_user_device_active') THEN
    BEGIN
      -- Try to create unique partial index; if fails due to existing duplicates, skip
      EXECUTE 'CREATE UNIQUE INDEX idx_refresh_tokens_user_device_active ON refresh_tokens(user_id, device_fingerprint) WHERE is_revoked = false';
    EXCEPTION WHEN others THEN
      -- ignore
      RAISE NOTICE 'Could not create unique partial index idx_refresh_tokens_user_device_active (may already exist or duplicates present)';
    END;
  END IF;
END
$$;

-- End of script

