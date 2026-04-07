-- Add version/batch field to module_vendor_info table
-- Version: 4.0
-- Description: Add version_batch field to help distinguish multiple entries from the same vendor

-- Add version_batch column to module_vendor_info table
ALTER TABLE module_vendor_info
ADD COLUMN version_batch VARCHAR(100) NULL COMMENT '版本/批次标识，用于区分同一厂家的不同供货时期或版本';
