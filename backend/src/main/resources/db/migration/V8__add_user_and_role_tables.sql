-- Migration V8: Add User and Role tables for authentication and authorization
-- This migration creates the necessary tables to support role-based access control

-- Create role table (enum-like table with predefined roles)
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at DATETIME(6) NOT NULL,
    UNIQUE INDEX idx_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user table for authentication
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    UNIQUE INDEX idx_user_username (username),
    INDEX idx_user_role_id (role_id),
    FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert predefined roles
INSERT INTO role (name, description, created_at) VALUES
    ('OWNER', '器件owner角色，支持所有操作（增删改查）', NOW(6)),
    ('READER', 'Reader角色，只能阅读查看，不能增删改', NOW(6));

-- Insert default admin user (password: admin123, should be changed in production)
-- Password is BCrypt hash of 'admin123'
INSERT INTO user (username, password, email, role_id, enabled, created_at, updated_at)
SELECT
    'admin',
    '$2a$10$xZwKqUEy7gN4p3P5qOL7K.5hQV5v2Wh8OqG9qX8p0nKxTl8CuJ0zK',
    'admin@example.com',
    (SELECT id FROM role WHERE name = 'OWNER'),
    TRUE,
    NOW(6),
    NOW(6);
