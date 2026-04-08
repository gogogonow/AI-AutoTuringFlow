-- Migration V12: Add default reader user account
-- This migration creates a default reader account for read-only access to the system

-- Insert default reader user (password: reader123)
-- Password is BCrypt hash of 'reader123'
-- The reader role has read-only permissions and cannot create, update, or delete modules
INSERT INTO user (username, password, email, role_id, enabled, created_at, updated_at)
SELECT
    'reader',
    '$2b$12$jHZx3Sn3v9Uc7JnJnWhESuFJvZcykIKT1YRRptIxMq7m3mkHOcwtq',
    'reader@example.com',
    (SELECT id FROM role WHERE name = 'READER'),
    TRUE,
    NOW(6),
    NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'reader');
