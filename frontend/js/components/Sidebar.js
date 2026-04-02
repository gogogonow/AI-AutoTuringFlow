// 侧边栏菜单组件
class Sidebar {
    constructor() {
        this.init();
    }
    
    init() {
        // 监听导航项点击
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const page = item.getAttribute('data-page');
                if (page) {
                    this.setActiveItem(item);
                    window.app.showPage(page);
                }
            });
        });
    }
    
    setActiveItem(activeItem) {
        // 移除所有活动状态
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        
        // 设置当前活动项
        activeItem.classList.add('active');
    }
    
    setActiveByPage(page) {
        const navItem = document.querySelector(`.nav-item[data-page="${page}"]`);
        if (navItem) {
            this.setActiveItem(navItem);
        }
    }
}