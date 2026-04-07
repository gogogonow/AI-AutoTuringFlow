-- Add soft delete columns to module and module_vendor_info tables
-- Version: 2.0
-- Description: Add deleted flag and deletedAt timestamp for soft delete functionality

-- Add soft delete columns to module table (only if they don't exist)
-- Using a stored procedure to check column existence before adding
SET @dbname = DATABASE();

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'deleted') = 0,
    'ALTER TABLE module ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT ''Column deleted already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'deleted_at') = 0,
    'ALTER TABLE module ADD COLUMN deleted_at TIMESTAMP NULL',
    'SELECT ''Column deleted_at already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for deleted column to improve query performance (only if it doesn't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_module_deleted') = 0,
    'CREATE INDEX idx_module_deleted ON module(deleted)',
    'SELECT ''Index idx_module_deleted already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add soft delete columns to module_vendor_info table (only if they don't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module_vendor_info' AND COLUMN_NAME = 'deleted') = 0,
    'ALTER TABLE module_vendor_info ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT ''Column deleted already exists in module_vendor_info table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module_vendor_info' AND COLUMN_NAME = 'deleted_at') = 0,
    'ALTER TABLE module_vendor_info ADD COLUMN deleted_at TIMESTAMP NULL',
    'SELECT ''Column deleted_at already exists in module_vendor_info table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index for deleted column to improve query performance (only if it doesn't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module_vendor_info' AND INDEX_NAME = 'idx_vendor_info_deleted') = 0,
    'CREATE INDEX idx_vendor_info_deleted ON module_vendor_info(deleted)',
    'SELECT ''Index idx_vendor_info_deleted already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add change_details column to history table (only if it doesn't exist)
-- This column stores detailed information about what changed in each operation
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'history' AND COLUMN_NAME = 'change_details') = 0,
    'ALTER TABLE history ADD COLUMN change_details TEXT NULL',
    'SELECT ''Column change_details already exists in history table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
