// Main App Class
class App {
  constructor() {
    this.header = null;
    this.sidebar = null;
    this.mainContent = null;
    this.currentComponent = null;
    this.currentPage = 'list';
    this._ignoreHashChange = false;
    this.init();
  }

  init() {
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => this.setup());
    } else {
      this.setup();
    }
  }

  setup() {
    // Create header
    this.header = new Header();
    document.body.appendChild(this.header.render());

    // Create layout container
    const layoutContainer = document.createElement('div');
    layoutContainer.className = 'layout-container';
    document.body.appendChild(layoutContainer);

    // Create sidebar
    this.sidebar = new Sidebar();
    layoutContainer.appendChild(this.sidebar.render());

    // Create main content area
    this.mainContent = document.createElement('main');
    this.mainContent.className = 'main-content';
    this.mainContent.id = 'mainContent';
    layoutContainer.appendChild(this.mainContent);

    // Create loading overlay
    const loading = document.createElement('div');
    loading.id = 'loading';
    loading.innerHTML = '<div class="loading-spinner"></div>';
    document.body.appendChild(loading);

    // Create toast
    const toast = document.createElement('div');
    toast.id = 'toast';
    document.body.appendChild(toast);

    // Create confirm dialog
    const confirmDialog = document.createElement('div');
    confirmDialog.id = 'confirmDialog';
    confirmDialog.innerHTML = `
      <div class="confirm-box">
        <div class="confirm-header">确认操作</div>
        <div class="confirm-message"></div>
        <div class="confirm-actions">
          <button class="btn btn-secondary" id="confirmNo">取消</button>
          <button class="btn btn-primary" id="confirmYes">确定</button>
        </div>
      </div>
    `;
    document.body.appendChild(confirmDialog);

    // Handle hash change
    window.addEventListener('hashchange', () => {
      if (this._ignoreHashChange) return;
      this.handleHashChange();
    });

    // Load initial page
    this.handleHashChange();
  }

  handleHashChange() {
    const hash = window.location.hash.slice(1); // Remove '#'
    const [path, paramsStr] = hash.split('?');
    const page = path.replace('/', '') || 'list';
    
    // Parse params
    const params = {};
    if (paramsStr) {
      paramsStr.split('&').forEach(pair => {
        const [key, value] = pair.split('=');
        params[decodeURIComponent(key)] = decodeURIComponent(value);
      });
    }

    this.showPage(page, params);
  }

  showPage(page, params = {}) {
    this.currentPage = page;
    this.sidebar.setActive(page);

    // Update hash to include params, suppressing the resulting hashchange event
    this._ignoreHashChange = true;
    const queryString = Object.keys(params)
      .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(params[k])}`)
      .join('&');
    window.location.hash = '#/' + page + (queryString ? '?' + queryString : '');
    setTimeout(() => { this._ignoreHashChange = false; }, 0);

    // Clear current content
    this.mainContent.innerHTML = '';

    // Create and mount component
    let component = null;
    switch (page) {
      case 'list':
        component = new ModuleList();
        break;
      case 'create':
        component = new ModuleForm();
        break;
      case 'edit':
        component = new ModuleForm(params);
        break;
      case 'details':
        component = new ModuleDetails(params);
        break;
      case 'history':
        component = new HistoryList();
        break;
      default:
        this.mainContent.innerHTML = `
          <div class="card">
            ${Utils.renderErrorState(
              '页面不存在',
              '<button class="btn btn-primary" onclick="window.app.showPage(\'list\')">返回首页</button>'
            )}
          </div>
        `;
        return;
    }

    this.currentComponent = component;
    this.mainContent.appendChild(component.getElement());
  }
}

// Initialize app when script loads
window.app = new App();
