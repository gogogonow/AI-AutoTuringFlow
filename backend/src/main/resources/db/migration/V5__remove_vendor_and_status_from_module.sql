-- V5: Remove vendor and status fields from module table
-- These fields are now managed separately in module_vendor_info table

-- Drop indexes related to vendor and status
DROP INDEX IF EXISTS idx_vendor ON module;
DROP INDEX IF EXISTS idx_status ON module;

-- Remove vendor column from module table
ALTER TABLE module DROP COLUMN IF EXISTS vendor;

-- Remove status column from module table
ALTER TABLE module DROP COLUMN IF EXISTS status;
