-- Add full field support for optical modules
-- Version: 3.0
-- Description: Add comprehensive fields for optical module specifications

-- Add lifecycle status and packaging fields
ALTER TABLE module
ADD COLUMN lifecycle_status VARCHAR(20) COMMENT 'Lifecycle status: GA, EOM, EOP',
ADD COLUMN package_form VARCHAR(50) COMMENT 'Package form (e.g., SFP, QSFP, QSFP28)',
ADD COLUMN fiber_count INTEGER COMMENT 'Number of fibers',
ADD COLUMN light_type VARCHAR(20) COMMENT 'Light type: GRAY, COLOR';

-- Add speed set for multiple speed support
ALTER TABLE module
ADD COLUMN speed_set TEXT COMMENT 'Supported speed set (JSON array)';

-- Add fiber type
ALTER TABLE module
ADD COLUMN fiber_type VARCHAR(20) COMMENT 'Fiber type: SMF (Single Mode), MMF (Multi Mode)';

-- Add power and temperature specifications
ALTER TABLE module
ADD COLUMN max_power_consumption DECIMAL(10, 2) COMMENT 'Maximum power consumption in Watts',
ADD COLUMN min_case_temp INTEGER COMMENT 'Minimum working case temperature in Celsius',
ADD COLUMN max_case_temp INTEGER COMMENT 'Maximum working case temperature in Celsius';

-- Add shipment information
ALTER TABLE module
ADD COLUMN last_shipment_time TIMESTAMP NULL COMMENT 'Last shipment time',
ADD COLUMN total_shipment_volume BIGINT DEFAULT 0 COMMENT 'Historical total shipment volume',
ADD COLUMN recent_5year_shipment_volume BIGINT DEFAULT 0 COMMENT 'Recent 5-year shipment volume',
ADD COLUMN shipment_regions TEXT COMMENT 'Shipping regions (JSON array)',
ADD COLUMN is_mainstream_shipment BOOLEAN DEFAULT FALSE COMMENT 'Is mainstream shipment';

-- Add specification and vendor information
ALTER TABLE module
ADD COLUMN spec_template_version VARCHAR(50) COMMENT 'Specification sheet template version',
ADD COLUMN current_shipping_vendors TEXT COMMENT 'Currently still shipping vendors (JSON array)';

-- Create indexes for frequently queried fields
CREATE INDEX idx_lifecycle_status ON module(lifecycle_status);
CREATE INDEX idx_package_form ON module(package_form);
CREATE INDEX idx_fiber_type ON module(fiber_type);
CREATE INDEX idx_light_type ON module(light_type);
CREATE INDEX idx_is_mainstream_shipment ON module(is_mainstream_shipment);
