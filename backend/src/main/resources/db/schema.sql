-- Database Schema for Optical Modules Management System
-- This script is safe to re-run: uses CREATE TABLE IF NOT EXISTS
-- Column definitions match Module.java and History.java JPA entities.

-- Create module table (matches Module.java entity: @Table(name = "module"))
CREATE TABLE IF NOT EXISTS module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    vendor VARCHAR(100) NOT NULL,
    speed VARCHAR(20),
    wavelength VARCHAR(20),
    transmission_distance INT,
    connector_type VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK',
    inbound_time DATETIME(6) NOT NULL,
    remark TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_serial_number (serial_number),
    INDEX idx_status (status),
    INDEX idx_model (model),
    INDEX idx_vendor (vendor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create history table (matches History.java entity: @Table(name = "history"))
CREATE TABLE IF NOT EXISTS history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    operation_type VARCHAR(30) NOT NULL,
    operation_time DATETIME(6) NOT NULL,
    operator VARCHAR(100),
    previous_status VARCHAR(20),
    next_status VARCHAR(20),
    remark TEXT,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_module_id (module_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operation_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
