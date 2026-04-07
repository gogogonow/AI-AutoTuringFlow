-- Add serial_number and model columns to history table
-- This allows history records to preserve module information even after the module is deleted

ALTER TABLE history ADD COLUMN serial_number VARCHAR(50);
ALTER TABLE history ADD COLUMN model VARCHAR(100);

-- Add index on serial_number for faster lookups
CREATE INDEX idx_history_serial_number ON history(serial_number);
