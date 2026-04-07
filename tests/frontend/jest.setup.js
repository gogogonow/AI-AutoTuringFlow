/**
 * Jest Setup File
 * Global test configuration and mocks
 */

// Import testing library extensions
import '@testing-library/jest-dom';

// Mock window.location
delete window.location;
window.location = {
  hash: '',
  href: '',
  assign: jest.fn(),
  reload: jest.fn()
};

// Mock CONFIG
global.CONFIG = {
  API_BASE_URL: '/api',
  DEFAULT_PAGE_SIZE: 20,
  STATUS_TEXT: {
    IN_STOCK: '在库',
    DEPLOYED: '已部署',
    FAULTY: '故障',
    UNDER_REPAIR: '维修中',
    SCRAPPED: '已报废'
  },
  OPERATION_TYPE_TEXT: {
    INBOUND: '入库',
    OUTBOUND: '出库',
    DEPLOY: '部署',
    RETRIEVE: '收回',
    MARK_FAULTY: '标记故障',
    SEND_REPAIR: '送修',
    RETURN_REPAIR: '维修归还',
    SCRAP: '报废',
    UPDATE_INFO: '更新信息'
  },
  SPEED_OPTIONS: ['1G', '10G', '25G', '40G', '100G'],
  CONNECTOR_TYPE_OPTIONS: ['LC', 'SC', 'MPO', 'RJ45']
};

// Mock Utils class
global.Utils = {
  showLoading: jest.fn(),
  hideLoading: jest.fn(),
  showToast: jest.fn(),
  confirm: jest.fn((msg, callback) => callback()),
  formatDateTime: jest.fn((str) => {
    if (!str) return '-';
    return str.replace('T', ' ').replace('Z', '');
  }),
  getStatusClass: jest.fn((status) => {
    if (!status) return '';
    return 'status-' + status.toLowerCase();
  }),
  getStatusText: jest.fn((status) => {
    if (!status) return '-';
    return CONFIG.STATUS_TEXT[status] || status;
  }),
  getOperationTypeText: jest.fn((type) => {
    if (!type) return '-';
    return CONFIG.OPERATION_TYPE_TEXT[type] || type;
  }),
  renderErrorState: jest.fn((msg, retry) => {
    return `<div class="error-state">${msg}${retry}</div>`;
  }),
  renderEmptyState: jest.fn((icon, text, action) => {
    return `<div class="empty-state">${icon}${text}${action}</div>`;
  }),
  escapeHtml: jest.fn((text) => {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  })
};

// Mock global app
global.app = {
  showPage: jest.fn(),
  currentComponent: null
};

// Suppress console errors in tests
global.console = {
  ...console,
  error: jest.fn(),
  warn: jest.fn()
};
