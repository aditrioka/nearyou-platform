-- NearYou ID Database Initialization Script
-- This script enables PostGIS extension and sets up the database

-- Enable PostGIS extension for geospatial queries
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Verify PostGIS installation
SELECT PostGIS_Version();

-- Create custom types
CREATE TYPE subscription_tier AS ENUM ('free', 'premium');
CREATE TYPE message_status AS ENUM ('sent', 'delivered', 'read');
CREATE TYPE report_type AS ENUM ('spam', 'harassment', 'inappropriate_content', 'other');
CREATE TYPE notification_type AS ENUM ('like', 'comment', 'follow', 'new_message');

-- Set timezone to UTC
SET timezone = 'UTC';

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'NearYou ID database initialized successfully';
    RAISE NOTICE 'PostGIS version: %', PostGIS_Version();
END $$;

