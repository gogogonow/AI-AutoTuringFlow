-- V14: Change test_report_link column from VARCHAR(500) to TEXT
-- to support multiple test report URLs (one per covered board)
-- Uses idempotent approach: checks current type before altering

SET @dbname = DATABASE();
SET @tablename = 'module_vendor_info';
SET @columnname = 'test_report_link';

-- Check if the column exists and is not already TEXT type, then alter it
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname
       AND TABLE_NAME = @tablename
       AND COLUMN_NAME = @columnname
       AND DATA_TYPE != 'text') > 0,
    CONCAT('ALTER TABLE `', @tablename, '` MODIFY COLUMN `', @columnname, '` TEXT NULL'),
    'SELECT 1'
));

PREPARE alterIfNeeded FROM @preparedStatement;
EXECUTE alterIfNeeded;
DEALLOCATE PREPARE alterIfNeeded;
