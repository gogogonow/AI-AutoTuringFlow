-- Add version/batch field to module_vendor_info table
-- Version: 4.0
-- Description: Add version_batch field to help distinguish multiple entries from the same vendor

-- Add version_batch column to module_vendor_info table (only if it doesn't exist)
SET @dbname = DATABASE();

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module_vendor_info' AND COLUMN_NAME = 'version_batch') = 0,
    'ALTER TABLE module_vendor_info ADD COLUMN version_batch VARCHAR(100) NULL COMMENT ''版本/批次标识，用于区分同一厂家的不同供货时期或版本''',
    'SELECT ''Column version_batch already exists in module_vendor_info table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
