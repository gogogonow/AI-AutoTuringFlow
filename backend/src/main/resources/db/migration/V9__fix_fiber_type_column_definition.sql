-- Fix enum columns from MySQL ENUM to VARCHAR to match Hibernate expectations
-- This resolves Hibernate schema validation errors:
-- "found [enum (Types#CHAR)], but expecting [varchar(20) (Types#ENUM)]"
--
-- When using @Enumerated(EnumType.STRING) with columnDefinition="VARCHAR(N)",
-- Hibernate expects actual VARCHAR columns, not MySQL ENUM type.

SET @dbname = DATABASE();

-- Alter lifecycle_status column to VARCHAR(20) if it exists and is not already VARCHAR
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname
       AND TABLE_NAME = 'module'
       AND COLUMN_NAME = 'lifecycle_status'
       AND DATA_TYPE != 'varchar') > 0,
    'ALTER TABLE module MODIFY COLUMN lifecycle_status VARCHAR(20) COMMENT ''Lifecycle status: GA, EOM, EOP''',
    'SELECT ''Column lifecycle_status is already VARCHAR or does not exist'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Alter light_type column to VARCHAR(20) if it exists and is not already VARCHAR
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname
       AND TABLE_NAME = 'module'
       AND COLUMN_NAME = 'light_type'
       AND DATA_TYPE != 'varchar') > 0,
    'ALTER TABLE module MODIFY COLUMN light_type VARCHAR(20) COMMENT ''Light type: GRAY, COLOR''',
    'SELECT ''Column light_type is already VARCHAR or does not exist'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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
