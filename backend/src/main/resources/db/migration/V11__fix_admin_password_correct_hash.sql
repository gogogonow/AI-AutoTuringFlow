-- Migration V11: Fix admin user password with correct BCrypt hash
-- Previous migrations V8 and V10 had incorrect BCrypt hashes that didn't match 'admin123'
-- This migration sets a verified correct BCrypt hash for the password 'admin123'

-- Update admin user password to verified correct BCrypt hash of 'admin123'
UPDATE user
SET password = '$2a$10$mWwzDGRhMZ4gy8adz40vW.zlTOBJtLmfzbiUUGd6kDMrXbwv4BYm6',
    updated_at = NOW(6)
WHERE username = 'admin';
