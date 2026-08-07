-- ============================================================
-- AgriLink AI — PostgreSQL Initialization Script
-- Runs once when the postgres container is first created.
-- ============================================================

-- Enable UUID generation (needed for gen_random_uuid())
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create the application database user if running outside Docker
-- (Docker already creates user/db from env vars)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'agrilink') THEN
        CREATE ROLE agrilink WITH LOGIN PASSWORD 'agrilink_password';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE agrilink_db TO agrilink;
GRANT ALL ON SCHEMA public TO agrilink;

-- Enum types are created by SQLAlchemy migrations — no manual creation needed here.

-- Log that init is complete
DO $$ BEGIN RAISE NOTICE 'AgriLink AI DB initialized ✓'; END $$;
