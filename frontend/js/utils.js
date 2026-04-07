// Utility Functions
class Utils {
  // Loading overlay
  static showLoading() {
    const loading = document.getElementById('loading');
    if (loading) {
      loading.classList.add('show');
    }
  }

  static hideLoading() {
    const loading = document.getElementById('loading');
    if (loading) {
      loading.classList.remove('show');
    }
  }

  // Toast notification
  static showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    if (!toast) return;

    toast.textContent = message;
    toast.className = 'show ' + type;

    setTimeout(() => {
      toast.classList.remove('show');
    }, 3000);
  }

  // Confirm dialog
  static confirm(message, onConfirm) {
    const dialog = document.getElementById('confirmDialog');
    if (!dialog) return;

    const messageEl = dialog.querySelector('.confirm-message');
    messageEl.textContent = message;

    const confirmBtn = dialog.querySelector('#confirmYes');
    const cancelBtn = dialog.querySelector('#confirmNo');

    const close = () => {
      dialog.classList.remove('show');
      confirmBtn.onclick = null;
      cancelBtn.onclick = null;
    };

    confirmBtn.onclick = () => {
      close();
      if (onConfirm) onConfirm();
    };

    cancelBtn.onclick = close;

    dialog.classList.add('show');
  }

  // Format date time
  static formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }

  // Get status CSS class
  static getStatusClass(status) {
    if (!status) return '';
    return 'status-' + status.toLowerCase();
  }

  // Get status text
  static getStatusText(status) {
    if (!status) return '-';
    return CONFIG.STATUS_TEXT[status] || status;
  }

  // Get operation type text
  static getOperationTypeText(type) {
    if (!type) return '-';
    return CONFIG.OPERATION_TYPE_TEXT[type] || type;
  }

  // Render error state
  static renderErrorState(message, retryHtml = '') {
    return `
      <div class="error-state">
        <div class="error-state-icon">⚠️</div>
        <div class="error-state-text">${message}</div>
        ${retryHtml}
      </div>
    `;
  }

  // Render empty state
  static renderEmptyState(icon, text, actionHtml = '') {
    return `
      <div class="empty-state">
        <div class="empty-state-icon">${icon}</div>
        <div class="empty-state-text">${text}</div>
        ${actionHtml}
      </div>
    `;
  }

  // Escape HTML
  static escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
}

// Make Utils globally available
window.Utils = Utils;
