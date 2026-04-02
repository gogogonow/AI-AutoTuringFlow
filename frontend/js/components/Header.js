// 顶部导航栏组件
class Header {
    constructor() {
        this.init();
    }
    
    init() {
        // 监听退出按钮
        const logoutBtn = document.querySelector('.logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                this.handleLogout();
            });
        }
    }
    
    handleLogout() {
        Utils.confirm('确定要退出系统吗？', () => {
            Utils.showToast('已退出系统', 'success');
            // 这里可以添加实际的登出逻辑
            // 例如：清除 token，跳转到登录页等
        });
    }
}