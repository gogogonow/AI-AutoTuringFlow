-- Database Schema for Optical Modules Management System

-- Drop tables if they exist
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS modules;

-- Create modules table
CREATE TABLE modules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    vendor VARCHAR(100),
    process_status VARCHAR(50),
    enter_time TIMESTAMP NULL,
    exit_time TIMESTAMP NULL,
    ld VARCHAR(100),
    pd VARCHAR(100),
    remarks TEXT,
    INDEX idx_code (code),
    INDEX idx_status (status),
    INDEX idx_vendor (vendor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create history table
CREATE TABLE history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    module_id INT NOT NULL,
    operation VARCHAR(20) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_module_id (module_id),
    INDEX idx_timestamp (timestamp),
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
