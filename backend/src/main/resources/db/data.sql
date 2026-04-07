-- Sample Data for Optical Modules Management System
-- Uses INSERT IGNORE to skip rows that already exist (safe to re-run)
-- Column names match the Module.java JPA entity (@Table name = "module").

-- Insert sample modules
INSERT IGNORE INTO module (serial_number, model, vendor, speed, wavelength, transmission_distance, connector_type, status, inbound_time, created_at, updated_at) VALUES
('SN-2024-001', 'SFP-10G-SR',   'Vendor A', '10G',  '850nm',  300,   'LC',  'IN_STOCK', NOW(), NOW(), NOW()),
('SN-2024-002', 'SFP-1G-LX',    'Vendor B', '1G',   '1310nm', 10000, 'LC',  'IN_STOCK', NOW(), NOW(), NOW()),
('SN-2024-003', 'QSFP-40G-SR4', 'Vendor A', '40G',  '850nm',  150,   'MPO', 'DEPLOYED', NOW(), NOW(), NOW()),
('SN-2024-004', 'SFP-10G-LR',   'Vendor C', '10G',  '1310nm', 10000, 'LC',  'IN_STOCK', NOW(), NOW(), NOW()),
('SN-2024-005', 'SFP-10G-ER',   'Vendor B', '10G',  '1550nm', 40000, 'LC',  'FAULTY',   NOW(), NOW(), NOW());
