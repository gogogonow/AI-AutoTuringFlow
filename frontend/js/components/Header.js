// Header Component
class Header {
  constructor() {
    this.element = null;
    this.init();
  }

  init() {
    this.element = document.createElement('header');
    this.element.className = 'header';
    this.updateUserInfo();
    this.bindEvents();
  }

  updateUserInfo() {
    const currentUser = this.getCurrentUser();

    this.element.innerHTML = `
      <div class="header-left">
        <button class="sidebar-toggle" id="sidebarToggle">☰</button>
        <h1 class="header-title">光模块管理系统</h1>
      </div>
      ${currentUser ? `
      <div class="header-right">
        <div class="user-info">
          <span class="user-name">${currentUser.username}</span>
          <span class="user-role ${currentUser.role === 'OWNER' ? 'role-owner' : 'role-reader'}">${currentUser.role === 'OWNER' ? '管理员' : '只读'}</span>
          <button class="btn btn-secondary btn-sm" id="logoutBtn">退出</button>
        </div>
      </div>
      ` : ''}
    `;

    this.bindEvents();
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('currentUser');
    return userStr ? JSON.parse(userStr) : null;
  }

  bindEvents() {
    // Sidebar toggle
    const toggleBtn = this.element.querySelector('#sidebarToggle');
    if (toggleBtn) {
      toggleBtn.addEventListener('click', () => {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar) {
          sidebar.classList.toggle('collapsed');
        }
      });
    }

    // Logout button
    const logoutBtn = this.element.querySelector('#logoutBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => this.handleLogout());
    }
  }

  async handleLogout() {
    const confirmed = await Utils.confirm('确定要退出登录吗？');
    if (confirmed) {
      API.logout();
      localStorage.removeItem('currentUser');
      Utils.showToast('已退出登录', 'success');
      this.updateUserInfo();
      window.app.showPage('login');
    }
  }

  render() {
    return this.element;
  }
}

// Make Header globally available
window.Header = Header;
