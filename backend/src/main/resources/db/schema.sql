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
    -- Comprehensive fields for optical module specifications
    lifecycle_status VARCHAR(20),
    package_form VARCHAR(50),
    fiber_count INT,
    light_type VARCHAR(20),
    speed_set TEXT,
    fiber_type VARCHAR(20),
    max_power_consumption DECIMAL(10, 2),
    min_case_temp INT,
    max_case_temp INT,
    last_shipment_time DATETIME(6),
    total_shipment_volume BIGINT DEFAULT 0,
    recent_5year_shipment_volume BIGINT DEFAULT 0,
    shipment_regions TEXT,
    is_mainstream_shipment BOOLEAN DEFAULT FALSE,
    spec_template_version VARCHAR(50),
    current_shipping_vendors TEXT,
    -- Soft delete columns
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    -- Audit columns
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_serial_number (serial_number),
    INDEX idx_status (status),
    INDEX idx_model (model),
    INDEX idx_vendor (vendor),
    INDEX idx_lifecycle_status (lifecycle_status),
    INDEX idx_package_form (package_form),
    INDEX idx_fiber_type (fiber_type),
    INDEX idx_light_type (light_type),
    INDEX idx_is_mainstream_shipment (is_mainstream_shipment),
    INDEX idx_module_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create module_vendor_info table (matches ModuleVendorInfo.java entity)
CREATE TABLE IF NOT EXISTS module_vendor_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    vendor VARCHAR(100) NOT NULL,
    process_status VARCHAR(50),
    version_batch VARCHAR(100),
    entry_time DATETIME(6),
    exit_time DATETIME(6),
    ld VARCHAR(200),
    pd VARCHAR(200),
    la_ldo VARCHAR(200),
    tia VARCHAR(200),
    mcu VARCHAR(200),
    pcn_changes TEXT,
    high_speed_test_recommended BOOLEAN,
    availability VARCHAR(100),
    photodetector_data TEXT,
    covered_boards TEXT,
    test_report_link VARCHAR(500),
    remark TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_mvi_module_id (module_id),
    INDEX idx_vendor_info_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create history table (matches History.java entity: @Table(name = "history"))
CREATE TABLE IF NOT EXISTS history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
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

-- Fix legacy 'operation' column that may exist from a previous schema version.
-- If the column exists without a default value it causes INSERT errors;
-- making it nullable + adding a DEFAULT NULL allows existing rows to remain intact.
-- continue-on-error=true handles the case where the column does not exist.
ALTER TABLE history MODIFY COLUMN operation VARCHAR(100) NULL DEFAULT NULL;

-- Fix legacy 'timestamp' column that may exist from a previous schema version.
-- This column has no default value in the old schema, causing INSERT failures
-- when JPA inserts a new history row without including this column.
ALTER TABLE history MODIFY COLUMN `timestamp` TIMESTAMP NULL DEFAULT NULL;

-- Expand operation_type column to accommodate new vendor operation types:
-- VENDOR_ADD (10 chars), VENDOR_UPDATE (13 chars), VENDOR_DELETE (13 chars).
-- The original column may have been created with a smaller size.
ALTER TABLE history MODIFY COLUMN operation_type VARCHAR(50) NOT NULL;

-- Drop old check constraint on operation_type that only allowed the original
-- 9 operation types and blocks the new VENDOR_ADD/VENDOR_UPDATE/VENDOR_DELETE values.
-- If the constraint does not exist (fresh installation or already removed),
-- this statement will fail and be silently skipped via continue-on-error=true.
ALTER TABLE history DROP CONSTRAINT chk_operation_type;
