// API 服务封装
// Utils 工具类定义在 utils.js（在本文件之前加载）
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
                const error = await response.json().catch(() => ({}));
                throw new Error(error.detail || error.message || `HTTP ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },
    
    // 光模块相关 API
    modules: {
        getAll:      ()       => API.request('/modules'),
        getById:     (id)     => API.request(`/modules/${id}`),
        create:      (data)   => API.request('/modules', { method: 'POST', body: JSON.stringify(data) }),
        update:      (id, data) => API.request(`/modules/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
        delete:      (id)     => API.request(`/modules/${id}`, { method: 'DELETE' }),
        getHistory:  (id)     => API.request(`/modules/${id}/history`)
    }
};