-- Sample Data for Optical Modules Management System
-- Uses INSERT IGNORE to skip rows that already exist (safe to re-run)
-- Column names match the Module.java JPA entity.

-- Insert sample modules
INSERT IGNORE INTO modules (serial_number, manufacturer, model_number, wavelength, transmit_power, receive_sensitivity, transmission_distance, fiber_type, connector_type, temperature_range, created_at, updated_at) VALUES
('SN-2024-001', 'Vendor A', 'SFP-10G-SR',    850.0,  -1.0, -11.0, 0.3,  'Multimode', 'LC', '0~70°C',  NOW(), NOW()),
('SN-2024-002', 'Vendor B', 'SFP-1G-LX',    1310.0,  -3.0, -20.0, 10.0, 'Singlemode','LC', '-20~85°C', NOW(), NOW()),
('SN-2024-003', 'Vendor A', 'QSFP-40G-SR4',  850.0,   2.4,  -7.5, 0.15, 'Multimode', 'MPO','-5~70°C',  NOW(), NOW()),
('SN-2024-004', 'Vendor C', 'SFP-10G-LR',   1310.0,  -1.0, -14.4, 10.0, 'Singlemode','LC', '0~70°C',  NOW(), NOW()),
('SN-2024-005', 'Vendor B', 'SFP-10G-ER',   1550.0,   0.5, -15.8, 40.0, 'Singlemode','LC', '0~70°C',  NOW(), NOW());
