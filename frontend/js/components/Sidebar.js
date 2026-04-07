// Sidebar Component
class Sidebar {
  constructor() {
    this.element = null;
    this.currentPage = 'list';
    this.init();
  }

  init() {
    this.element = document.createElement('aside');
    this.element.className = 'sidebar';
    this.element.id = 'sidebar';
    this.element.innerHTML = `
      <nav class="sidebar-nav">
        <a href="#/list" class="nav-item active" data-page="list">
          <span class="nav-icon">📦</span>
          <span class="nav-text">光模块列表</span>
        </a>
        <a href="#/create" class="nav-item" data-page="create">
          <span class="nav-icon">➕</span>
          <span class="nav-text">入库登记</span>
        </a>
        <a href="#/history" class="nav-item" data-page="history">
          <span class="nav-icon">📜</span>
          <span class="nav-text">修改历史</span>
        </a>
      </nav>
    `;

    this.bindEvents();
  }

  bindEvents() {
    const navItems = this.element.querySelectorAll('.nav-item');
    navItems.forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const page = item.dataset.page;
        this.setActive(page);
        
        if (window.app) {
          window.app.showPage(page);
        }
      });
    });
  }

  setActive(page) {
    this.currentPage = page;
    const navItems = this.element.querySelectorAll('.nav-item');
    navItems.forEach(item => {
      if (item.dataset.page === page) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });
    
    // Update hash
    window.location.hash = '#/' + page;
  }

  render() {
    return this.element;
  }
}

// Make Sidebar globally available
window.Sidebar = Sidebar;
