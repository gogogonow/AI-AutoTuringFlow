-- V5: Remove vendor and status fields from module table
-- These fields are now managed separately in module_vendor_info table

SET @dbname = DATABASE();

-- Drop index idx_vendor on module (only if it exists)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_vendor') > 0,
    'DROP INDEX idx_vendor ON module',
    'SELECT ''Index idx_vendor does not exist on module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop index idx_status on module (only if it exists)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_status') > 0,
    'DROP INDEX idx_status ON module',
    'SELECT ''Index idx_status does not exist on module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Remove vendor column from module table (only if it exists)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'vendor') > 0,
    'ALTER TABLE module DROP COLUMN vendor',
    'SELECT ''Column vendor does not exist in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Remove status column from module table (only if it exists)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'status') > 0,
    'ALTER TABLE module DROP COLUMN status',
    'SELECT ''Column status does not exist in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
