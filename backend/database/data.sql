-- Sample Data for Optical Modules Management System

-- Insert sample modules
INSERT INTO modules (code, status, vendor, process_status, ld, pd, remarks) VALUES
('MOD-2024-001', 'Active', 'Vendor A', 'In Production', 'LD-001', 'PD-001', 'High-speed optical module'),
('MOD-2024-002', 'Testing', 'Vendor B', 'Quality Check', 'LD-002', 'PD-002', 'Standard module for testing'),
('MOD-2024-003', 'Active', 'Vendor A', 'Completed', 'LD-003', 'PD-003', 'Long-range communication module'),
('MOD-2024-004', 'Maintenance', 'Vendor C', 'Repair', 'LD-004', 'PD-004', 'Module under maintenance'),
('MOD-2024-005', 'Active', 'Vendor B', 'In Production', 'LD-005', 'PD-005', 'Multi-mode fiber module');

-- Insert sample history records
INSERT INTO history (module_id, operation, old_value, new_value) VALUES
(1, 'CREATE', NULL, '{"code":"MOD-2024-001","status":"Active"}'),
(1, 'UPDATE', '{"status":"Testing"}', '{"status":"Active"}'),
(2, 'CREATE', NULL, '{"code":"MOD-2024-002","status":"Testing"}'),
(3, 'CREATE', NULL, '{"code":"MOD-2024-003","status":"Active"}'),
(4, 'CREATE', NULL, '{"code":"MOD-2024-004","status":"Maintenance"}'),
(5, 'CREATE', NULL, '{"code":"MOD-2024-005","status":"Active"}');
