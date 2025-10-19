-- PostgreSQL Initialization Script for Payroll Management System
-- This script runs when the PostgreSQL container starts for the first time

-- Create additional extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone
SET timezone = 'Asia/Dhaka';

-- Create application-specific user (if not already created by POSTGRES_USER)
-- This is for production environments where you might want separate users
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'payroll_app') THEN
        CREATE ROLE payroll_app WITH LOGIN PASSWORD 'payroll_app_pass';
    END IF;
END
$$;

-- Grant necessary permissions
GRANT CONNECT ON DATABASE payroll_db TO payroll_app;
GRANT USAGE ON SCHEMA public TO payroll_app;
GRANT CREATE ON SCHEMA public TO payroll_app;

-- Create audit function for tracking changes (Optional)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Log the initialization
SELECT 'PostgreSQL initialized for Payroll Management System' AS status;