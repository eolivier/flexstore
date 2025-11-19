-- Add password column to user table
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS password TEXT NOT NULL DEFAULT '';

-- Update existing users with default passwords
UPDATE "user" SET password = 'password123' WHERE id = 'u-1';
UPDATE "user" SET password = 'password456' WHERE id = 'u-2';
UPDATE "user" SET password = 'password789' WHERE id = 'u-3';
