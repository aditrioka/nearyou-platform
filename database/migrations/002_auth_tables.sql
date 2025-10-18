-- Migration 002: Authentication Tables
-- Created: 2025-10-17
-- Description: Create tables for OTP codes and refresh tokens

-- ============================================================================
-- OTP CODES TABLE
-- ============================================================================
-- Stores one-time passwords for email/phone verification
-- OTPs expire after 5 minutes
CREATE TABLE IF NOT EXISTS otp_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_identifier VARCHAR(255) NOT NULL, -- email or phone number
    code VARCHAR(6) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('email', 'phone')),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    is_used BOOLEAN DEFAULT FALSE
);

-- Indexes for otp_codes table
CREATE INDEX idx_otp_codes_identifier ON otp_codes(user_identifier, type) WHERE is_used = FALSE;
CREATE INDEX idx_otp_codes_expires ON otp_codes(expires_at) WHERE is_used = FALSE;
CREATE INDEX idx_otp_codes_created ON otp_codes(created_at DESC);

-- ============================================================================
-- REFRESH TOKENS TABLE
-- ============================================================================
-- Stores refresh tokens for JWT authentication
-- Refresh tokens expire after 30 days
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(512) UNIQUE NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP
);

-- Indexes for refresh_tokens table
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token) WHERE is_revoked = FALSE;
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at) WHERE is_revoked = FALSE;

-- ============================================================================
-- CLEANUP FUNCTION
-- ============================================================================
-- Function to clean up expired OTP codes and refresh tokens
-- Should be run periodically (e.g., daily via cron job)
CREATE OR REPLACE FUNCTION cleanup_expired_auth_data()
RETURNS void AS $$
BEGIN
    -- Delete expired OTP codes older than 1 day
    DELETE FROM otp_codes
    WHERE expires_at < NOW() - INTERVAL '1 day';

    -- Delete expired refresh tokens older than 7 days
    DELETE FROM refresh_tokens
    WHERE expires_at < NOW() - INTERVAL '7 days';

    RAISE NOTICE 'Expired auth data cleaned up successfully';
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- VERIFICATION
-- ============================================================================
DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name IN ('otp_codes', 'refresh_tokens');
    
    IF table_count = 2 THEN
        RAISE NOTICE 'Migration 002 completed successfully';
        RAISE NOTICE 'Created tables: otp_codes, refresh_tokens';
    ELSE
        RAISE EXCEPTION 'Migration 002 failed: Expected 2 tables, found %', table_count;
    END IF;
END $$;

