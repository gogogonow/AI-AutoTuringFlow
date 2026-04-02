// API 服务封装
const API = {
    baseURL: '/api',
    
    // 通用请求方法
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };
        
        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.detail || '请求失败');
            }
            
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },
    
    // 光模块相关 API
    modules: {
        // 获取所有光模块
        getAll: () => API.request('/modules'),
        
        // 获取单个光模块详情
        getById: (id) => API.request(`/modules/${id}`),
        
        // 创建光模块
        create: (data) => API.request('/modules', {
            method: 'POST',
            body: JSON.stringify(data)
        }),
        
        // 更新光模块
        update: (id, data) => API.request(`/modules/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        }),
        
        // 删除光模块
        delete: (id) => API.request(`/modules/${id}`, {
            method: 'DELETE'
        }),
        
        // 获取修改历史
        getHistory: (id) => API.request(`/modules/${id}/history`)
    }
};

// 工具函数
const Utils = {
    // 显示加载动画
    showLoading() {
        const loading = document.getElementById('loading');
        if (loading) loading.style.display = 'flex';
    },
    
    // 隐藏加载动画
    hideLoading() {
        const loading = document.getElementById('loading');
        if (loading) loading.style.display = 'none';
    },
    
    // 显示提示消息
    showToast(message, type = 'success') {
        const toast = document.getElementById('toast');
        if (!toast) return;
        
        toast.textContent = message;
        toast.className = `toast ${type}`;
        toast.style.display = 'block';
        
        setTimeout(() => {
            toast.style.display = 'none';
        }, 3000);
    },
    
    // 格式化日期时间
    formatDateTime(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    },
    
    // 获取状态显示类名
    getStatusClass(status) {
        const statusMap = {
            'active': 'status-active',
            'inactive': 'status-inactive',
            'maintenance': 'status-maintenance'
        };
        return statusMap[status] || 'status-active';
    },
    
    // 获取状态显示文本
    getStatusText(status) {
        const statusMap = {
            'active': '活跃',
            'inactive': '停用',
            'maintenance': '维护中'
        };
        return statusMap[status] || status;
    },
    
    // 创建确认对话框
    confirm(message, onConfirm) {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        overlay.innerHTML = `
            <div class="modal">
                <div class="modal-header">确认操作</div>
                <div class="modal-body">${message}</div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" id="cancel-btn">取消</button>
                    <button class="btn btn-danger" id="confirm-btn">确认</button>
                </div>
            </div>
        `;
        
        document.body.appendChild(overlay);
        
        document.getElementById('cancel-btn').addEventListener('click', () => {
            document.body.removeChild(overlay);
        });
        
        document.getElementById('confirm-btn').addEventListener('click', () => {
            document.body.removeChild(overlay);
            onConfirm();
        });
    }
};