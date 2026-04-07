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
  }

  render() {
    return this.element;
  }
}

// Make Header globally available
window.Header = Header;
