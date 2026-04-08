/**
 * Frontend Utils Test Suite
 * Tests the utility functions in js/utils.js
 */

// Load CONFIG first
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
    DELETE_MODULE: '删除光模块'
  }
};

// Load the actual Utils class from the source
require('../../frontend/js/utils.js');

describe('Utils', () => {
  beforeEach(() => {
    // Setup DOM elements for testing
    document.body.innerHTML = `
      <div id="loading"></div>
      <div id="toast"></div>
      <div id="confirmDialog">
        <div class="confirm-message"></div>
        <button id="confirmYes"></button>
        <button id="confirmNo"></button>
      </div>
    `;
  });

  describe('showLoading / hideLoading', () => {
    test('showLoading should add show class to loading element', () => {
      Utils.showLoading();
      const loading = document.getElementById('loading');
      expect(loading.classList.contains('show')).toBe(true);
    });

    test('hideLoading should remove show class from loading element', () => {
      const loading = document.getElementById('loading');
      loading.classList.add('show');
      Utils.hideLoading();
      expect(loading.classList.contains('show')).toBe(false);
    });
  });

  describe('showToast', () => {
    test('should display toast with correct message and type', () => {
      Utils.showToast('Test message', 'success');
      const toast = document.getElementById('toast');
      expect(toast.textContent).toBe('Test message');
      expect(toast.classList.contains('success')).toBe(true);
      expect(toast.classList.contains('show')).toBe(true);
    });

    test('should auto-hide toast after 3 seconds', (done) => {
      jest.useFakeTimers();
      Utils.showToast('Test', 'error');
      const toast = document.getElementById('toast');
      
      expect(toast.classList.contains('show')).toBe(true);
      
      jest.advanceTimersByTime(3000);
      expect(toast.classList.contains('show')).toBe(false);
      
      jest.useRealTimers();
      done();
    });
  });

  describe('formatDateTime', () => {
    test('should format ISO date string correctly', () => {
      const result = Utils.formatDateTime('2024-01-15T10:30:00Z');
      expect(result).toMatch(/2024-01-15 \d{2}:\d{2}:\d{2}/);
    });

    test('should return "-" for null/undefined', () => {
      expect(Utils.formatDateTime(null)).toBe('-');
      expect(Utils.formatDateTime(undefined)).toBe('-');
      expect(Utils.formatDateTime('')).toBe('-');
    });
  });

  describe('getStatusClass', () => {
    test('should return correct CSS class for status', () => {
      expect(Utils.getStatusClass('IN_STOCK')).toBe('status-in_stock');
      expect(Utils.getStatusClass('DEPLOYED')).toBe('status-deployed');
      expect(Utils.getStatusClass(null)).toBe('');
    });
  });

  describe('getStatusText', () => {
    test('should return Chinese text for status', () => {
      expect(Utils.getStatusText('IN_STOCK')).toBe('在库');
      expect(Utils.getStatusText('DEPLOYED')).toBe('已部署');
      expect(Utils.getStatusText('FAULTY')).toBe('故障');
      expect(Utils.getStatusText(null)).toBe('-');
    });
  });

  describe('getOperationTypeText', () => {
    test('should return Chinese text for operation type', () => {
      expect(Utils.getOperationTypeText('INBOUND')).toBe('入库');
      expect(Utils.getOperationTypeText('OUTBOUND')).toBe('出库');
      expect(Utils.getOperationTypeText('DEPLOY')).toBe('部署');
      expect(Utils.getOperationTypeText(null)).toBe('-');
    });
  });

  describe('escapeHtml', () => {
    test('should escape HTML special characters', () => {
      const input = '<script>alert("XSS")</script>';
      const result = Utils.escapeHtml(input);
      expect(result).not.toContain('<script>');
      expect(result).toContain('&lt;script&gt;');
    });
  });

  describe('renderErrorState', () => {
    test('should return error state HTML', () => {
      const result = Utils.renderErrorState('Error message', '<button>Retry</button>');
      expect(result).toContain('error-state');
      expect(result).toContain('Error message');
      expect(result).toContain('<button>Retry</button>');
    });
  });

  describe('renderEmptyState', () => {
    test('should return empty state HTML', () => {
      const result = Utils.renderEmptyState('📦', 'No data', '<button>Add</button>');
      expect(result).toContain('empty-state');
      expect(result).toContain('📦');
      expect(result).toContain('No data');
      expect(result).toContain('<button>Add</button>');
    });
  });

  describe('confirm', () => {
    test('should show dialog with correct message when called with callback', () => {
      const callback = jest.fn();
      Utils.confirm('Test message', callback);

      const dialog = document.getElementById('confirmDialog');
      const messageEl = dialog.querySelector('.confirm-message');

      expect(messageEl.textContent).toBe('Test message');
      expect(dialog.classList.contains('show')).toBe(true);
    });

    test('should call callback when confirm button clicked (callback pattern)', () => {
      const callback = jest.fn();
      Utils.confirm('Test message', callback);

      const confirmBtn = document.getElementById('confirmYes');
      confirmBtn.click();

      expect(callback).toHaveBeenCalled();

      const dialog = document.getElementById('confirmDialog');
      expect(dialog.classList.contains('show')).toBe(false);
    });

    test('should not call callback when cancel button clicked (callback pattern)', () => {
      const callback = jest.fn();
      Utils.confirm('Test message', callback);

      const cancelBtn = document.getElementById('confirmNo');
      cancelBtn.click();

      expect(callback).not.toHaveBeenCalled();

      const dialog = document.getElementById('confirmDialog');
      expect(dialog.classList.contains('show')).toBe(false);
    });

    test('should return Promise that resolves to true when confirm clicked', async () => {
      const promise = Utils.confirm('Test message');

      const confirmBtn = document.getElementById('confirmYes');
      confirmBtn.click();

      const result = await promise;
      expect(result).toBe(true);

      const dialog = document.getElementById('confirmDialog');
      expect(dialog.classList.contains('show')).toBe(false);
    });

    test('should return Promise that resolves to false when cancel clicked', async () => {
      const promise = Utils.confirm('Test message');

      const cancelBtn = document.getElementById('confirmNo');
      cancelBtn.click();

      const result = await promise;
      expect(result).toBe(false);

      const dialog = document.getElementById('confirmDialog');
      expect(dialog.classList.contains('show')).toBe(false);
    });

    test('should return rejected Promise when dialog element does not exist', async () => {
      document.body.innerHTML = ''; // Remove dialog

      await expect(Utils.confirm('Test')).rejects.toThrow('Confirm dialog not found');
    });
  });
});
