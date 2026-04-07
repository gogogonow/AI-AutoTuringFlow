-- Add full field support for optical modules
-- Version: 3.0
-- Description: Add comprehensive fields for optical module specifications

SET @dbname = DATABASE();

-- Add lifecycle status and packaging fields (only if they don't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'lifecycle_status') = 0,
    'ALTER TABLE module ADD COLUMN lifecycle_status VARCHAR(20) COMMENT ''Lifecycle status: GA, EOM, EOP''',
    'SELECT ''Column lifecycle_status already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'package_form') = 0,
    'ALTER TABLE module ADD COLUMN package_form VARCHAR(50) COMMENT ''Package form (e.g., SFP, QSFP, QSFP28)''',
    'SELECT ''Column package_form already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'fiber_count') = 0,
    'ALTER TABLE module ADD COLUMN fiber_count INTEGER COMMENT ''Number of fibers''',
    'SELECT ''Column fiber_count already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'light_type') = 0,
    'ALTER TABLE module ADD COLUMN light_type VARCHAR(20) COMMENT ''Light type: GRAY, COLOR''',
    'SELECT ''Column light_type already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add speed set for multiple speed support (only if it doesn't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'speed_set') = 0,
    'ALTER TABLE module ADD COLUMN speed_set TEXT COMMENT ''Supported speed set (JSON array)''',
    'SELECT ''Column speed_set already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add fiber type (only if it doesn't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'fiber_type') = 0,
    'ALTER TABLE module ADD COLUMN fiber_type VARCHAR(20) COMMENT ''Fiber type: SMF (Single Mode), MMF (Multi Mode)''',
    'SELECT ''Column fiber_type already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add power and temperature specifications (only if they don't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'max_power_consumption') = 0,
    'ALTER TABLE module ADD COLUMN max_power_consumption DECIMAL(10, 2) COMMENT ''Maximum power consumption in Watts''',
    'SELECT ''Column max_power_consumption already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'min_case_temp') = 0,
    'ALTER TABLE module ADD COLUMN min_case_temp INTEGER COMMENT ''Minimum working case temperature in Celsius''',
    'SELECT ''Column min_case_temp already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'max_case_temp') = 0,
    'ALTER TABLE module ADD COLUMN max_case_temp INTEGER COMMENT ''Maximum working case temperature in Celsius''',
    'SELECT ''Column max_case_temp already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add shipment information (only if columns don't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'last_shipment_time') = 0,
    'ALTER TABLE module ADD COLUMN last_shipment_time TIMESTAMP NULL COMMENT ''Last shipment time''',
    'SELECT ''Column last_shipment_time already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'total_shipment_volume') = 0,
    'ALTER TABLE module ADD COLUMN total_shipment_volume BIGINT DEFAULT 0 COMMENT ''Historical total shipment volume''',
    'SELECT ''Column total_shipment_volume already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'recent_5year_shipment_volume') = 0,
    'ALTER TABLE module ADD COLUMN recent_5year_shipment_volume BIGINT DEFAULT 0 COMMENT ''Recent 5-year shipment volume''',
    'SELECT ''Column recent_5year_shipment_volume already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'shipment_regions') = 0,
    'ALTER TABLE module ADD COLUMN shipment_regions TEXT COMMENT ''Shipping regions (JSON array)''',
    'SELECT ''Column shipment_regions already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'is_mainstream_shipment') = 0,
    'ALTER TABLE module ADD COLUMN is_mainstream_shipment BOOLEAN DEFAULT FALSE COMMENT ''Is mainstream shipment''',
    'SELECT ''Column is_mainstream_shipment already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add specification and vendor information (only if columns don't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'spec_template_version') = 0,
    'ALTER TABLE module ADD COLUMN spec_template_version VARCHAR(50) COMMENT ''Specification sheet template version''',
    'SELECT ''Column spec_template_version already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND COLUMN_NAME = 'current_shipping_vendors') = 0,
    'ALTER TABLE module ADD COLUMN current_shipping_vendors TEXT COMMENT ''Currently still shipping vendors (JSON array)''',
    'SELECT ''Column current_shipping_vendors already exists in module table'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Create indexes for frequently queried fields (only if they don't exist)
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_lifecycle_status') = 0,
    'CREATE INDEX idx_lifecycle_status ON module(lifecycle_status)',
    'SELECT ''Index idx_lifecycle_status already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_package_form') = 0,
    'CREATE INDEX idx_package_form ON module(package_form)',
    'SELECT ''Index idx_package_form already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_fiber_type') = 0,
    'CREATE INDEX idx_fiber_type ON module(fiber_type)',
    'SELECT ''Index idx_fiber_type already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_light_type') = 0,
    'CREATE INDEX idx_light_type ON module(light_type)',
    'SELECT ''Index idx_light_type already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'module' AND INDEX_NAME = 'idx_is_mainstream_shipment') = 0,
    'CREATE INDEX idx_is_mainstream_shipment ON module(is_mainstream_shipment)',
    'SELECT ''Index idx_is_mainstream_shipment already exists'' AS message'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
