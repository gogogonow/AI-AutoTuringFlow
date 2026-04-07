-- Optical Transceiver Management System - Initial Schema
-- Version: 1.0
-- Description: Create module and history tables

-- Drop tables if exist (for development)
DROP TABLE IF EXISTS history CASCADE;
DROP TABLE IF EXISTS module CASCADE;

-- Module table: stores optical transceiver information
CREATE TABLE module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    vendor VARCHAR(100) NOT NULL,
    speed VARCHAR(20),
    wavelength VARCHAR(20),
    transmission_distance INTEGER,
    connector_type VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK',
    inbound_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN ('IN_STOCK', 'DEPLOYED', 'FAULTY', 'UNDER_REPAIR', 'SCRAPPED')),
    INDEX idx_serial_number (serial_number),
    INDEX idx_status (status),
    INDEX idx_model (model),
    INDEX idx_vendor (vendor)
);

-- History table: stores operation logs
CREATE TABLE history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operator VARCHAR(100),
    previous_status VARCHAR(20),
    next_status VARCHAR(20),
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_module FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE,
    INDEX idx_module_id (module_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operation_type (operation_type)
);

-- Insert sample data for testing
INSERT INTO module (serial_number, model, vendor, speed, wavelength, transmission_distance, connector_type, status, remark) VALUES
('SN20240001', 'SFP-10G-SR', 'Cisco', '10G', '850', 300, 'LC', 'IN_STOCK', '测试数据1'),
('SN20240002', 'QSFP-100G-SR4', 'Arista', '100G', '850', 100, 'MPO', 'DEPLOYED', '已部署到核心交换机'),
('SN20240003', 'SFP-1G-LX', 'Huawei', '1G', '1310', 10000, 'LC', 'IN_STOCK', NULL),
('SN20240004', 'SFP-10G-LR', 'Juniper', '10G', '1310', 10000, 'LC', 'FAULTY', '光衰过大'),
('SN20240005', 'QSFP28-100G-LR4', 'Mellanox', '100G', '1310', 10000, 'LC', 'UNDER_REPAIR', '送修中');

INSERT INTO history (module_id, operation_type, operator, previous_status, next_status, remark) VALUES
(1, 'INBOUND', 'admin', NULL, 'IN_STOCK', '首次入库'),
(2, 'INBOUND', 'admin', NULL, 'IN_STOCK', '首次入库'),
(2, 'DEPLOY', 'admin', 'IN_STOCK', 'DEPLOYED', '部署到SW-CORE-01'),
(3, 'INBOUND', 'admin', NULL, 'IN_STOCK', '首次入库'),
(4, 'INBOUND', 'admin', NULL, 'IN_STOCK', '首次入库'),
(4, 'MARK_FAULTY', 'admin', 'IN_STOCK', 'FAULTY', '光功率异常'),
(5, 'INBOUND', 'admin', NULL, 'IN_STOCK', '首次入库'),
(5, 'SEND_REPAIR', 'admin', 'IN_STOCK', 'UNDER_REPAIR', '送厂维修');
