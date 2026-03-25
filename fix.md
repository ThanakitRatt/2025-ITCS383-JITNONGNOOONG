# MharRuengSang Food Delivery Application - Fixes Documentation

## Overview
This document outlines all the fixes applied to resolve various issues encountered during the setup and testing of the MharRuengSang food delivery application.

## Issues and Fixes

### 1. Database Permission Issues (Restaurant Service)
**Problem:** Restaurant service failed to start with permission denied errors when Hibernate tried to create/update database tables.

**Error:**
```
ERROR: permission denied for schema public
```

**Root Cause:** The `mhar_user` role lacked CREATE privileges on the `public` schema in the `mhar_restaurants` database.

**Solution:**
```sql
-- Connect to PostgreSQL as postgres user
psql -U postgres

-- Switch to mhar_restaurants database
\c mhar_restaurants

-- Grant necessary permissions
GRANT CREATE ON SCHEMA public TO mhar_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mhar_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mhar_user;
```

### 2. Missing Columns in Restaurants Table
**Problem:** API calls to fetch restaurants returned 500 errors due to missing database columns.

**Error:**
```
ERROR: column "closing_time" does not exist
ERROR: column "is_active" does not exist
```

**Root Cause:** Hibernate ddl-auto:update couldn't add new columns because the user lacked ALTER TABLE permissions.

**Solution:**
```sql
-- Add missing columns to restaurants table
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS closing_time TIME;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS accepts_orders BOOLEAN DEFAULT true;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3,2) DEFAULT 0.0;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS minimum_order_amount DECIMAL(10,2) DEFAULT 0.00;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS delivery_fee DECIMAL(10,2) DEFAULT 0.00;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS estimated_delivery_time INTEGER;
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);
ALTER TABLE restaurants ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(500);
```

### 3. Missing Columns in Menu Items Table
**Problem:** Restaurant menu endpoints returned 500 errors due to missing columns in menu_items table.

**Error:**
```
ERROR: column "display_order" does not exist
ERROR: column "preparation_time" does not exist
```

**Root Cause:** Similar to restaurants table - missing columns that were added to the JPA entity but not reflected in the database.

**Solution:**
```sql
-- Add missing columns to menu_items table
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0;
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS preparation_time INTEGER;
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS calories INTEGER;
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS ingredients TEXT;
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS allergens TEXT;
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS is_available BOOLEAN DEFAULT true;
```

### 4. Order Service Database Issues
**Problem:** Order service failed to start with permission issues and missing tables.

**Error:**
```
ERROR: permission denied for schema public
ERROR: relation "orders" does not exist
```

**Root Cause:** Similar permission issues in the `mhar_orders` database.

**Solution:**
```sql
-- Connect to PostgreSQL as postgres user
psql -U postgres

-- Switch to mhar_orders database
\c mhar_orders

-- Grant necessary permissions
GRANT CREATE ON SCHEMA public TO mhar_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mhar_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mhar_user;
```

### 5. Duplicate Restaurant Data
**Problem:** Frontend displayed duplicate restaurants (6 instead of 3 unique restaurants).

**Root Cause:** The `seed-data.sql` was executed multiple times, creating duplicate entries in the restaurants table.

**Solution:**
```sql
-- Check for duplicates
SELECT id, name, cuisine_type FROM restaurants ORDER BY id;

-- Delete duplicate restaurants (assuming IDs 4-6 are duplicates)
DELETE FROM restaurants WHERE id > 3;

-- Verify only unique restaurants remain
SELECT id, name, cuisine_type FROM restaurants ORDER BY id;
```

## Database Setup Commands

### Initial Database Creation
```sql
-- Create databases
CREATE DATABASE mhar_auth;
CREATE DATABASE mhar_restaurants;
CREATE DATABASE mhar_orders;
CREATE DATABASE mhar_customers;

-- Create user and grant permissions
CREATE USER mhar_user WITH PASSWORD 'mhar_password';
GRANT ALL PRIVILEGES ON DATABASE mhar_auth TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_restaurants TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_orders TO mhar_user;
GRANT ALL PRIVILEGES ON DATABASE mhar_customers TO mhar_user;
```

### Schema Permissions (Required for Each Database)
```sql
-- For each database (mhar_restaurants, mhar_orders, mhar_customers)
\c <database_name>
GRANT CREATE ON SCHEMA public TO mhar_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mhar_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mhar_user;
```

## Service Startup Order

1. **Start PostgreSQL** (if not already running)
2. **Run database initialization scripts:**
   - `init-databases.sql` (creates databases and users)
   - `demo-users.sql` (creates auth users)
   - `seed-data.sql` (populates sample data)
3. **Start services in order:**
   - API Gateway (port 8080)
   - Restaurant Service (port 8082)
   - Order Service (port 8081)
   - Other services as needed
4. **Start frontend:**
   - `npm run dev` (port 5173)

## Testing Commands

### Verify API Endpoints
```bash
# Test restaurant search
curl -s "http://localhost:8080/api/v1/restaurants/search" | jq '.data.content | length'

# Test specific restaurant
curl -s "http://localhost:8080/api/v1/restaurants/1"

# Test restaurant menu
curl -s "http://localhost:8080/api/v1/restaurants/1/menu"
```

### Database Verification
```sql
-- Check restaurants
SELECT id, name, cuisine_type FROM restaurants;

-- Check menu items
SELECT restaurant_id, name, price FROM menu_items ORDER BY restaurant_id;

-- Check permissions
\dp
```

## Prevention Measures

1. **Use idempotent seed scripts:** Modify `seed-data.sql` to use `INSERT ... ON CONFLICT DO NOTHING`
2. **Grant proper permissions upfront:** Ensure all required permissions are granted during database setup
3. **Test with clean database:** Always test fixes on a fresh database to avoid conflicts
4. **Monitor logs:** Check service logs for permission and schema errors during startup

## Common Error Patterns

- **Permission denied:** Grant CREATE and ALL PRIVILEGES on schema/tables
- **Column does not exist:** Add missing columns with appropriate types
- **Relation does not exist:** Ensure tables are created (check ddl-auto settings)
- **Duplicate data:** Clean up duplicates and modify seed scripts to prevent reoccurrence

## Files Modified/Created

- Database schema (added missing columns)
- Database permissions (granted to mhar_user)
- Seed data (removed duplicates)
- This documentation file

## Notes

- All services use Hibernate with `ddl-auto: update` for automatic schema management
- The API Gateway routes requests to appropriate microservices
- Frontend communicates through the API Gateway on port 8080
- Database user `mhar_user` needs explicit permissions for DDL operations</content>
<parameter name="filePath">/Users/thxnk/Downloads/2025-ITCS383-JITNONGNOOONG-master-2/fix.md
