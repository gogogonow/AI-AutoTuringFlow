-- Add soft delete columns to module and module_vendor_info tables
-- Version: 2.0
-- Description: Add deleted flag and deletedAt timestamp for soft delete functionality

-- Add soft delete columns to module table
ALTER TABLE module
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP NULL;

-- Add index for deleted column to improve query performance
CREATE INDEX idx_module_deleted ON module(deleted);

-- Add soft delete columns to module_vendor_info table
ALTER TABLE module_vendor_info
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP NULL;

-- Add index for deleted column to improve query performance
CREATE INDEX idx_vendor_info_deleted ON module_vendor_info(deleted);

-- Add change_details column to history table if not exists
-- This column stores detailed information about what changed in each operation
ALTER TABLE history
ADD COLUMN change_details TEXT NULL;
