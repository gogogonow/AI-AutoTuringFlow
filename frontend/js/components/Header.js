// Header Component
class Header {
  constructor() {
    this.element = null;
    this.init();
  }

  init() {
    this.element = document.createElement('header');
    this.element.className = 'header';
    this.element.innerHTML = `
      <div class="header-left">
        <button class="sidebar-toggle" id="sidebarToggle">☰</button>
        <h1 class="header-title">光模块管理系统</h1>
      </div>
      <div class="header-right">
        <button class="btn btn-secondary btn-sm" id="refreshBtn">
          <span class="icon">🔄</span>
          <span>刷新</span>
        </button>
        <button class="btn btn-primary btn-sm" id="importBtn">
          <span class="icon">📥</span>
          <span>导入</span>
        </button>
        <button class="btn btn-primary btn-sm" id="exportBtn">
          <span class="icon">📤</span>
          <span>导出</span>
        </button>
      </div>
    `;

    this.bindEvents();
  }

  bindEvents() {
    // Sidebar toggle
    const toggleBtn = this.element.querySelector('#sidebarToggle');
    toggleBtn.addEventListener('click', () => {
      const sidebar = document.querySelector('.sidebar');
      if (sidebar) {
        sidebar.classList.toggle('collapsed');
      }
    });

    // Refresh button
    const refreshBtn = this.element.querySelector('#refreshBtn');
    refreshBtn.addEventListener('click', () => {
      if (window.app && window.app.currentComponent) {
        if (typeof window.app.currentComponent.refresh === 'function') {
          window.app.currentComponent.refresh();
        }
      }
    });

    // Import button
    const importBtn = this.element.querySelector('#importBtn');
    importBtn.addEventListener('click', () => {
      this.handleImport();
    });

    // Export button
    const exportBtn = this.element.querySelector('#exportBtn');
    exportBtn.addEventListener('click', () => {
      this.handleExport();
    });
  }

  async handleImport() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.xlsx,.xls,.csv';
    input.onchange = async (e) => {
      const file = e.target.files[0];
      if (!file) return;

      try {
        Utils.showLoading();
        await API.importModules(file);
        Utils.hideLoading();
        Utils.showToast('导入成功', 'success');
        
        // Refresh current page if it's the module list
        if (window.app && window.app.currentComponent && window.app.currentComponent.refresh) {
          window.app.currentComponent.refresh();
        }
      } catch (error) {
        Utils.hideLoading();
        Utils.showToast('导入失败: ' + error.message, 'error');
      }
    };
    input.click();
  }

  async handleExport() {
    try {
      Utils.showLoading();
      
      // Get current filters from ModuleList component if available
      let filters = {};
      if (window.app && window.app.currentComponent && window.app.currentComponent.getFilters) {
        filters = window.app.currentComponent.getFilters();
      }
      
      await API.exportModules(filters);
      Utils.hideLoading();
      Utils.showToast('导出成功', 'success');
    } catch (error) {
      Utils.hideLoading();
      Utils.showToast('导出失败: ' + error.message, 'error');
    }
  }

  render() {
    return this.element;
  }
}

// Make Header globally available
window.Header = Header;
