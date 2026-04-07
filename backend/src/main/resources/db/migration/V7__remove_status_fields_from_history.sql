-- Remove previous_status and next_status columns from history table
-- These fields are no longer needed as status tracking has been removed

SET @dbname = DATABASE();

-- Remove previous_status column (only if it exists)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'history' AND COLUMN_NAME = 'previous_status') > 0,
    'ALTER TABLE history DROP COLUMN previous_status',
    'SELECT ''Column previous_status does not exist in history table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Remove next_status column (only if it exists)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'history' AND COLUMN_NAME = 'next_status') > 0,
    'ALTER TABLE history DROP COLUMN next_status',
    'SELECT ''Column next_status does not exist in history table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
