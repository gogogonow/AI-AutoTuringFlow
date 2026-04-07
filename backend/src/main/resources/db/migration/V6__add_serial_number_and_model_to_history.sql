-- Add serial_number and model columns to history table
-- This allows history records to preserve module information even after the module is deleted

SET @dbname = DATABASE();

-- Add serial_number column (only if it doesn't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'history' AND COLUMN_NAME = 'serial_number') = 0,
    'ALTER TABLE history ADD COLUMN serial_number VARCHAR(50)',
    'SELECT ''Column serial_number already exists in history table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add model column (only if it doesn't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'history' AND COLUMN_NAME = 'model') = 0,
    'ALTER TABLE history ADD COLUMN model VARCHAR(100)',
    'SELECT ''Column model already exists in history table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index on serial_number for faster lookups (only if it doesn't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'history' AND INDEX_NAME = 'idx_history_serial_number') = 0,
    'CREATE INDEX idx_history_serial_number ON history(serial_number)',
    'SELECT ''Index idx_history_serial_number already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
