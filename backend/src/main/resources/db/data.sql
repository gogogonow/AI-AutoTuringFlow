-- Sample Data for Optical Modules Management System
-- Uses INSERT IGNORE to skip rows that already exist (safe to re-run)

-- Insert sample modules
INSERT IGNORE INTO modules (code, status, vendor, process_status, ld, pd, remarks) VALUES
('MOD-2024-001', 'Active', 'Vendor A', 'In Production', 'LD-001', 'PD-001', 'High-speed optical module'),
('MOD-2024-002', 'Testing', 'Vendor B', 'Quality Check', 'LD-002', 'PD-002', 'Standard module for testing'),
('MOD-2024-003', 'Active', 'Vendor A', 'Completed', 'LD-003', 'PD-003', 'Long-range communication module'),
('MOD-2024-004', 'Maintenance', 'Vendor C', 'Repair', 'LD-004', 'PD-004', 'Module under maintenance'),
('MOD-2024-005', 'Active', 'Vendor B', 'In Production', 'LD-005', 'PD-005', 'Multi-mode fiber module');
