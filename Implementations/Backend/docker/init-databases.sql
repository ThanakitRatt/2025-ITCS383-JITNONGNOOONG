-- Initialize all databases for MharRuengSang microservices
-- This script runs when PostgreSQL container starts

-- Create databases for each service
CREATE DATABASE mhar_orders;
CREATE DATABASE mhar_restaurants; 
CREATE DATABASE mhar_auth;
CREATE DATABASE mhar_payments;
CREATE DATABASE mhar_riders;
CREATE DATABASE mhar_notifications;

-- Create user for services (if not exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'mhar_user') THEN
        CREATE USER mhar_user WITH PASSWORD 'mhar_password';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mhar_orders TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_restaurants TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_auth TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_payments TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_riders TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_notifications TO mhar_user;

-- Connect to each database and set up schemas
\c mhar_orders;
GRANT ALL ON SCHEMA public TO mhar_user;

\c mhar_restaurants;
GRANT ALL ON SCHEMA public TO mhar_user;

\c mhar_auth;
GRANT ALL ON SCHEMA public TO mhar_user;

\c mhar_payments;
GRANT ALL ON SCHEMA public TO mhar_user;

\c mhar_riders;
GRANT ALL ON SCHEMA public TO mhar_user;

\c mhar_notifications;
GRANT ALL ON SCHEMA public TO mhar_user;
