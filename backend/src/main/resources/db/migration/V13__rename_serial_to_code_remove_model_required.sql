-- V13: Make model column nullable (field is being removed from UI)
-- and add photodetector_data_file column for file upload support

-- Make model column nullable
SET @dbname = DATABASE();
SET @tablename = 'module';
SET @columnname = 'model';

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
      AND IS_NULLABLE = 'NO'
  ) > 0,
  CONCAT('ALTER TABLE `', @tablename, '` MODIFY COLUMN `', @columnname, '` VARCHAR(100) NULL'),
  'SELECT 1'
));
PREPARE alterIfNeeded FROM @preparedStatement;
EXECUTE alterIfNeeded;
DEALLOCATE PREPARE alterIfNeeded;

-- Add photodetector_data_file column to module_vendor_info for file upload support
SET @tablename2 = 'module_vendor_info';
SET @columnname2 = 'photodetector_data_file';

SET @preparedStatement2 = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename2
      AND COLUMN_NAME = @columnname2
  ) = 0,
  CONCAT('ALTER TABLE `', @tablename2, '` ADD COLUMN `', @columnname2, '` VARCHAR(500) NULL'),
  'SELECT 1'
));
PREPARE alterIfNeeded2 FROM @preparedStatement2;
EXECUTE alterIfNeeded2;
DEALLOCATE PREPARE alterIfNeeded2;
