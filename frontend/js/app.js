// 主应用程序
class App {
    constructor() {
        this.container = document.getElementById('page-container');
        this.currentPage = null;
        this.currentComponent = null;
        
        // 初始化组件
        this.header = new Header();
        this.sidebar = new Sidebar();
        
        // 初始化路由
        this.initRouter();
        
        // 显示默认页面
        this.showPage('list');
    }
    
    initRouter() {
        // 监听浏览器前进后退
        window.addEventListener('popstate', (e) => {
            if (e.state && e.state.page) {
                this.showPage(e.state.page, e.state.id, false);
            }
        });
    }
    
    showPage(page, id = null, pushState = true) {
        // 更新浏览器历史
        if (pushState) {
            const state = { page, id };
            const url = id ? `#${page}/${id}` : `#${page}`;
            window.history.pushState(state, '', url);
        }
        
        // 更新侧边栏激活状态
        if (page === 'list' || page === 'create') {
            this.sidebar.setActiveByPage(page);
        } else {
            // 对于详情、编辑、历史页面，保持列表页高亮
            this.sidebar.setActiveByPage('list');
        }
        
        // 清空当前容器
        this.container.innerHTML = '';
        
        // 根据页面类型创建对应组件
        switch (page) {
            case 'list':
                this.currentComponent = new ModuleList(this.container);
                break;
                
            case 'details':
                if (id) {
                    this.currentComponent = new ModuleDetails(this.container, id);
                } else {
                    this.showPage('list');
                    return;
                }
                break;
                
            case 'create':
                this.currentComponent = new ModuleForm(this.container);
                break;
                
            case 'edit':
                if (id) {
                    this.currentComponent = new ModuleForm(this.container, id);
                } else {
                    this.showPage('list');
                    return;
                }
                break;
                
            case 'history':
                if (id) {
                    this.currentComponent = new HistoryList(this.container, id);
                } else {
                    this.showPage('list');
                    return;
                }
                break;
                
            default:
                this.showPage('list');
                return;
        }
        
        // 渲染组件
        this.currentPage = page;
        if (this.currentComponent && typeof this.currentComponent.render === 'function') {
            this.currentComponent.render();
        }
    }
}

// 页面加载完成后初始化应用
document.addEventListener('DOMContentLoaded', () => {
    window.app = new App();
});