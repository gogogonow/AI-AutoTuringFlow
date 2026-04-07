// Utility Functions — shared across all components
// NOTE: The Utils object (with showLoading, showToast, etc.) is defined
//       further below. Standalone functions here are kept for legacy use.

/**
 * HTML 转义防止 XSS
 */
function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

/**
 * 防抖函数
 */
function debounce(func, wait) {
  let timeout;
  return function(...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}

/**
 * 节流函数
 */
function throttle(func, limit) {
  let inThrottle;
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

/**
 * Utils — 所有组件共享的 UI 工具类
 * 职责：加载态、消息提示、格式化、状态映射、公共 HTML 片段
 * 规范：所有组件必须通过 Utils.* 调用，不得在组件内自行重新实现。
 */
const Utils = {
  // ── 加载状态 ──────────────────────────────────────────────────
  showLoading() {
    const el = document.getElementById('loading');
    if (el) el.style.display = 'flex';
  },

  hideLoading() {
    const el = document.getElementById('loading');
    if (el) el.style.display = 'none';
  },

  // ── 消息提示 ──────────────────────────────────────────────────
  showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    if (!toast) return;
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 3000);
  },

  // ── 确认对话框 ────────────────────────────────────────────────
  confirm(message, onConfirm) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.style.cssText = 'position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);z-index:9999;display:flex;align-items:center;justify-content:center;';
    overlay.innerHTML = `
      <div style="background:#fff;border-radius:8px;padding:24px;min-width:300px;box-shadow:0 8px 24px rgba(0,0,0,.16);">
        <div style="font-size:14px;color:#333;margin-bottom:20px;">${escapeHtml(message)}</div>
        <div style="display:flex;justify-content:flex-end;gap:8px;">
          <button id="_confirm-cancel" style="padding:6px 16px;border:1px solid #e8e8e8;background:#fff;border-radius:4px;cursor:pointer;font-size:14px;">取消</button>
          <button id="_confirm-ok" style="padding:6px 16px;background:#ff4d4f;color:#fff;border:none;border-radius:4px;cursor:pointer;font-size:14px;">确认</button>
        </div>
      </div>`;
    document.body.appendChild(overlay);
    overlay.querySelector('#_confirm-cancel').onclick = () => document.body.removeChild(overlay);
    overlay.querySelector('#_confirm-ok').onclick = () => { document.body.removeChild(overlay); onConfirm(); };
  },

  // ── 格式化 ────────────────────────────────────────────────────
  formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return date.toLocaleString('zh-CN', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
  },

  // ── 状态映射 ──────────────────────────────────────────────────
  getStatusClass(status) {
    const map = {
      'active': 'status-active',
      'inactive': 'status-inactive',
      'maintenance': 'status-maintenance'
    };
    return map[status] || 'status-inactive';
  },

  getStatusText(status) {
    // 支持 CONFIG 中定义的枚举（大写），以及组件内部的简化枚举
    if (CONFIG && CONFIG.STATUS_TEXT && CONFIG.STATUS_TEXT[status]) {
      return CONFIG.STATUS_TEXT[status];
    }
    const fallback = { 'active': '活跃', 'inactive': '停用', 'maintenance': '维护中' };
    return fallback[status] || status;
  },

  getOperationTypeText(type) {
    if (CONFIG && CONFIG.OPERATION_TYPE_TEXT && CONFIG.OPERATION_TYPE_TEXT[type]) {
      return CONFIG.OPERATION_TYPE_TEXT[type];
    }
    const fallback = { 'create': '✨ 创建', 'update': '📝 更新', 'delete': '🗑️ 删除' };
    return fallback[type] || type;
  },

  // ── 公共 HTML 片段 ────────────────────────────────────────────
  /**
   * 渲染错误状态卡片（所有组件统一调用，禁止各自重新实现）
   * @param {string} message     - 错误说明文字
   * @param {string} retryHtml   - 重试按钮 HTML（可选）
   */
  renderErrorState(message, retryHtml = '') {
    return `
      <div class="card">
        <div class="empty-state">
          <span class="empty-state-icon">⚠️</span>
          <div class="empty-state-text">${escapeHtml(message)}</div>
          ${retryHtml}
        </div>
      </div>`;
  },

  /**
   * 渲染空数据状态（所有组件统一调用，禁止各自重新实现）
   * @param {string} icon       - emoji 图标
   * @param {string} text       - 提示文字
   * @param {string} actionHtml - 操作按钮 HTML（可选）
   */
  renderEmptyState(icon, text, actionHtml = '') {
    return `
      <div class="empty-state">
        <span class="empty-state-icon">${icon}</span>
        <div class="empty-state-text">${escapeHtml(text)}</div>
        ${actionHtml}
      </div>`;
  }
};