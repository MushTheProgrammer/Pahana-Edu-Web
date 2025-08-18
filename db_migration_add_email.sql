-- Add email column to customers table if it does not exist
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS email VARCHAR(150) AFTER name; -- MySQL 8+ supports IF NOT EXISTS for columns

-- Backfill example (optional)
-- UPDATE customers SET email = CONCAT(LOWER(REPLACE(name, ' ', '.')), '@example.com') WHERE (email IS NULL OR email = '');
