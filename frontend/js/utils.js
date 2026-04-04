// Utility Functions

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
 * 格式化日期时间
 * @param {string} isoString - ISO 8601 格式时间字符串
 * @returns {string} 格式化后的时间字符串 YYYY-MM-DD HH:mm:ss
 */
function formatDateTime(isoString) {
  if (!isoString) return '';
  const date = new Date(isoString);
  if (isNaN(date.getTime())) return isoString;
  
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 获取状态中文文本
 */
function getStatusText(status) {
  return CONFIG.STATUS_TEXT[status] || status;
}

/**
 * 获取操作类型中文文本
 */
function getOperationTypeText(type) {
  return CONFIG.OPERATION_TYPE_TEXT[type] || type;
}

/**
 * 显示提示消息
 */
function showMessage(message, type = 'info') {
  // 简单的 alert 实现，可以后续改为更美观的 toast
  alert(message);
}

/**
 * 显示确认对话框
 */
function showConfirm(message) {
  return confirm(message);
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