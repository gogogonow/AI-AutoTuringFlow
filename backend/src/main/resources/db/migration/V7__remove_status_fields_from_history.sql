-- Remove previous_status and next_status columns from history table
-- These fields are no longer needed as status tracking has been removed

ALTER TABLE history DROP COLUMN previous_status;
ALTER TABLE history DROP COLUMN next_status;
