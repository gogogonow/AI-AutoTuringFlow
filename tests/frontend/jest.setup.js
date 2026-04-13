/**
 * Jest Setup File
 * Global test configuration and mocks
 */

// Import testing library extensions
require('@testing-library/jest-dom');

// Mock window.location
delete window.location;
window.location = {
  hash: '',
  href: '',
  assign: jest.fn(),
  reload: jest.fn()
};

// Mock localStorage
const localStorageMock = {
  store: {},
  getItem: jest.fn((key) => localStorageMock.store[key] || null),
  setItem: jest.fn((key, value) => { localStorageMock.store[key] = value; }),
  removeItem: jest.fn((key) => { delete localStorageMock.store[key]; }),
  clear: jest.fn(() => { localStorageMock.store = {}; })
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock fetch
global.fetch = jest.fn();

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
    UPDATE_INFO: '更新信息',
    VENDOR_ADD: '新增厂家',
    VENDOR_UPDATE: '更新厂家',
    VENDOR_DELETE: '删除厂家',
    DELETE_MODULE: '删除光模块'
  },
  SPEED_OPTIONS: ['1G', '10G', '25G', '40G', '100G'],
  CONNECTOR_TYPE_OPTIONS: ['LC', 'SC', 'MPO', 'RJ45']
};
window.CONFIG = global.CONFIG;

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
    if (text === null || text === undefined) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  })
};
window.Utils = global.Utils;

// Mock global app
global.app = {
  showPage: jest.fn(),
  currentComponent: null,
  getCurrentUser: jest.fn(() => ({ role: 'OWNER', username: 'admin' }))
};
window.app = global.app;

// Load frontend source files to make classes globally available
const fs = require('fs');
const path = require('path');

function loadScript(filePath) {
  const code = fs.readFileSync(filePath, 'utf8');
  // Wrap in a function to avoid duplicate declaration errors
  // Extract class name and assign to global
  const fn = new Function(code + '\n; return typeof arguments !== "undefined" ? undefined : undefined;');
  fn();
}

const frontendDir = path.resolve(__dirname, '../../frontend/js');

// Load source files by evaluating them in global scope
// We use eval to ensure classes are declared in global scope
const filesToLoad = [
  'api.js',
  'components/ModuleList.js',
  'components/ModuleForm.js',
  'components/ModuleDetails.js',
  'components/HistoryList.js',
  'components/Header.js',
  'components/Sidebar.js',
  'components/Login.js',
  'app.js'
];

for (const file of filesToLoad) {
  const filePath = path.join(frontendDir, file);
  if (fs.existsSync(filePath)) {
    let code = fs.readFileSync(filePath, 'utf8');
    // For app.js, remove the auto-instantiation that requires all dependencies
    if (file === 'app.js') {
      // Add global class assignment and remove auto-instantiation that requires full environment
      code = code.replace(
        /\/\/\s*Initialize app when script loads\s*\nwindow\.app\s*=\s*new\s+App\(\)\s*;?/,
        '// Make App globally available\nwindow.App = App;'
      );
    }
    try {
      // Use indirect eval to execute in global scope
      (0, eval)(code);
    } catch (e) {
      // Ignore errors from files that reference browser APIs not available in test env
    }
  }
}

// Suppress console errors in tests
global.console = {
  ...console,
  error: jest.fn(),
  warn: jest.fn()
};
