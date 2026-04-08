-- Fix fiber_type column type from MySQL ENUM to VARCHAR(20)
-- This resolves Hibernate schema validation error:
-- "found [enum (Types#CHAR)], but expecting [varchar(20) (Types#ENUM)]"

SET @dbname = DATABASE();

-- Alter fiber_type column to VARCHAR(20) if it exists and is not already VARCHAR
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname
       AND TABLE_NAME = 'module'
       AND COLUMN_NAME = 'fiber_type'
       AND DATA_TYPE != 'varchar') > 0,
    'ALTER TABLE module MODIFY COLUMN fiber_type VARCHAR(20) COMMENT ''Fiber type: SMF (Single Mode), MMF (Multi Mode)''',
    'SELECT ''Column fiber_type is already VARCHAR or does not exist'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
