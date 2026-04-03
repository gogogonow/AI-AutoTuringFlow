-- Database Schema for Optical Modules Management System
-- This script is safe to re-run: uses CREATE TABLE IF NOT EXISTS
-- Column definitions match Module.java and History.java JPA entities.

-- Create modules table
CREATE TABLE IF NOT EXISTS modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    manufacturer VARCHAR(100) NOT NULL,
    model_number VARCHAR(100),
    wavelength DOUBLE,
    transmit_power DOUBLE,
    receive_sensitivity DOUBLE,
    transmission_distance DOUBLE,
    fiber_type VARCHAR(50),
    connector_type VARCHAR(50),
    temperature_range VARCHAR(50),
    voltage DOUBLE,
    power_consumption DOUBLE,
    created_at DATETIME(6) NOT NULL DEFAULT NOW(6),
    updated_at DATETIME(6) NOT NULL DEFAULT NOW(6),
    INDEX idx_serial_number (serial_number),
    INDEX idx_manufacturer (manufacturer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migrate existing tables: add columns missing from older schema versions.
-- These statements are safe to re-run; errors on already-existing columns are
-- suppressed by spring.sql.init.continue-on-error=true.
ALTER TABLE modules ADD COLUMN IF NOT EXISTS created_at DATETIME(6) NOT NULL DEFAULT NOW(6);
ALTER TABLE modules ADD COLUMN IF NOT EXISTS updated_at DATETIME(6) NOT NULL DEFAULT NOW(6);

-- Create history table
CREATE TABLE IF NOT EXISTS history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_module_id (module_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
