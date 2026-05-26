-- Add module_name column to module table
-- This column stores the optical module name (光模块名称)
-- Nullable to maintain backward compatibility with existing data
ALTER TABLE module ADD COLUMN module_name VARCHAR(100) NULL AFTER serial_number;
