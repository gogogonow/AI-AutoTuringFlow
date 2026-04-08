-- Migration V10: Fix admin user password
-- The password hash in V8 was incorrect. This migration updates it to the correct BCrypt hash for 'admin123'

-- Update admin user password to correct BCrypt hash of 'admin123'
UPDATE user
SET password = '$2a$10$N86mf7KqGXKMAzCQNZQfOeJVF7K0p.M2qGX5JqK8gYKZC6Z8Jz8R2',
    updated_at = NOW(6)
WHERE username = 'admin';
