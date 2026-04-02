/**
 * API 服务层测试
 * 测试所有 API 调用、错误处理、工具函数
 */

// 模拟 fetch API
global.fetch = jest.fn();

// 导入待测试模块
const {
    API_BASE_URL,
    getAllModules,
    getModuleById,
    createModule,
    updateModule,
    deleteModule,
    getModuleHistory,
    showLoading,
    hideLoading,
    showToast,
    formatDate,
    confirmDelete
} = require('../../frontend/js/api');

describe('API Service Tests', () => {
    
    beforeEach(() => {
        // 清除所有 mock
        fetch.mockClear();
        // 清除 DOM
        document.body.innerHTML = '';
    });

    describe('getAllModules()', () => {
        
        test('应该成功获取所有模块列表', async () => {
            const mockModules = [
                { id: 1, serialNumber: 'SN-001', manufacturer: '华为' },
                { id: 2, serialNumber: 'SN-002', manufacturer: '中兴' }
            ];
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockModules
            });
            
            const modules = await getAllModules();
            
            expect(fetch).toHaveBeenCalledTimes(1);
            expect(fetch).toHaveBeenCalledWith(`${API_BASE_URL}/modules`);
            expect(modules).toEqual(mockModules);
            expect(modules.length).toBe(2);
        });
        
        test('应该处理空列表响应', async () => {
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => []
            });
            
            const modules = await getAllModules();
            
            expect(modules).toEqual([]);
            expect(modules.length).toBe(0);
        });
        
        test('应该处理网络错误', async () => {
            fetch.mockRejectedValueOnce(new Error('Network error'));
            
            await expect(getAllModules()).rejects.toThrow('Network error');
        });
        
        test('应该处理服务器 500 错误', async () => {
            fetch.mockResolvedValueOnce({
                ok: false,
                status: 500,
                statusText: 'Internal Server Error'
            });
            
            await expect(getAllModules()).rejects.toThrow();
        });
    });

    describe('getModuleById()', () => {
        
        test('应该成功获取单个模块', async () => {
            const mockModule = {
                id: 1,
                serialNumber: 'SN-001',
                manufacturer: '华为',
                wavelength: 850.0
            };
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockModule
            });
            
            const module = await getModuleById(1);
            
            expect(fetch).toHaveBeenCalledWith(`${API_BASE_URL}/modules/1`);
            expect(module).toEqual(mockModule);
            expect(module.id).toBe(1);
        });
        
        test('应该处理模块不存在的情况(404)', async () => {
            fetch.mockResolvedValueOnce({
                ok: false,
                status: 404,
                statusText: 'Not Found'
            });
            
            await expect(getModuleById(999)).rejects.toThrow();
        });
        
        test('应该验证 ID 参数类型', async () => {
            // 测试无效 ID
            await expect(getModuleById(null)).rejects.toThrow();
            await expect(getModuleById(undefined)).rejects.toThrow();
        });
    });

    describe('createModule()', () => {
        
        test('应该成功创建新模块', async () => {
            const newModule = {
                serialNumber: 'SN-NEW',
                manufacturer: '诺基亚',
                wavelength: 1310.0
            };
            
            const createdModule = { ...newModule, id: 3 };
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => createdModule
            });
            
            const result = await createModule(newModule);
            
            expect(fetch).toHaveBeenCalledWith(
                `${API_BASE_URL}/modules`,
                expect.objectContaining({
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(newModule)
                })
            );
            expect(result.id).toBe(3);
        });
        
        test('应该处理验证错误(400)', async () => {
            const invalidModule = { serialNumber: '' };
            
            fetch.mockResolvedValueOnce({
                ok: false,
                status: 400,
                statusText: 'Bad Request'
            });
            
            await expect(createModule(invalidModule)).rejects.toThrow();
        });
        
        test('应该验证必填字段', async () => {
            await expect(createModule(null)).rejects.toThrow();
            await expect(createModule({})).rejects.toThrow();
        });
    });

    describe('updateModule()', () => {
        
        test('应该成功更新模块', async () => {
            const updatedData = {
                serialNumber: 'SN-UPDATED',
                manufacturer: '爱立信'
            };
            
            const updatedModule = { id: 1, ...updatedData };
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => updatedModule
            });
            
            const result = await updateModule(1, updatedData);
            
            expect(fetch).toHaveBeenCalledWith(
                `${API_BASE_URL}/modules/1`,
                expect.objectContaining({
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(updatedData)
                })
            );
            expect(result.manufacturer).toBe('爱立信');
        });
        
        test('应该处理更新不存在的模块(404)', async () => {
            fetch.mockResolvedValueOnce({
                ok: false,
                status: 404,
                statusText: 'Not Found'
            });
            
            await expect(updateModule(999, {})).rejects.toThrow();
        });
    });

    describe('deleteModule()', () => {
        
        test('应该成功删除模块', async () => {
            fetch.mockResolvedValueOnce({
                ok: true,
                status: 204
            });
            
            await deleteModule(1);
            
            expect(fetch).toHaveBeenCalledWith(
                `${API_BASE_URL}/modules/1`,
                expect.objectContaining({ method: 'DELETE' })
            );
        });
        
        test('应该处理删除不存在的模块(404)', async () => {
            fetch.mockResolvedValueOnce({
                ok: false,
                status: 404
            });
            
            await expect(deleteModule(999)).rejects.toThrow();
        });
    });

    describe('getModuleHistory()', () => {
        
        test('应该成功获取模块历史记录', async () => {
            const mockHistory = [
                { id: 1, operation: 'CREATE', fieldName: 'serialNumber' },
                { id: 2, operation: 'UPDATE', fieldName: 'manufacturer' }
            ];
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockHistory
            });
            
            const history = await getModuleHistory(1);
            
            expect(fetch).toHaveBeenCalledWith(`${API_BASE_URL}/modules/1/history`);
            expect(history.length).toBe(2);
            expect(history[0].operation).toBe('CREATE');
        });
    });

    describe('Utility Functions', () => {
        
        test('formatDate() 应该正确格式化日期', () => {
            const testDate = '2024-01-15T10:30:00';
            const formatted = formatDate(testDate);
            
            expect(formatted).toMatch(/2024-01-15/);
            expect(formatted).toMatch(/10:30/);
        });
        
        test('formatDate() 应该处理无效日期', () => {
            expect(formatDate(null)).toBe('');
            expect(formatDate(undefined)).toBe('');
            expect(formatDate('invalid')).toBe('');
        });
        
        test('showLoading() 应该显示加载动画', () => {
            const loadingDiv = document.createElement('div');
            loadingDiv.id = 'loading';
            loadingDiv.style.display = 'none';
            document.body.appendChild(loadingDiv);
            
            showLoading();
            
            expect(loadingDiv.style.display).toBe('flex');
        });
        
        test('hideLoading() 应该隐藏加载动画', () => {
            const loadingDiv = document.createElement('div');
            loadingDiv.id = 'loading';
            loadingDiv.style.display = 'flex';
            document.body.appendChild(loadingDiv);
            
            hideLoading();
            
            expect(loadingDiv.style.display).toBe('none');
        });
        
        test('showToast() 应该显示提示消息', () => {
            showToast('测试消息', 'success');
            
            const toast = document.querySelector('.toast');
            expect(toast).toBeTruthy();
            expect(toast.textContent).toContain('测试消息');
            expect(toast.classList.contains('success')).toBe(true);
        });
        
        test('confirmDelete() 应该返回用户确认结果', () => {
            // 模拟用户点击确认
            window.confirm = jest.fn(() => true);
            expect(confirmDelete('SN-001')).toBe(true);
            
            // 模拟用户点击取消
            window.confirm = jest.fn(() => false);
            expect(confirmDelete('SN-002')).toBe(false);
        });
    });

    describe('Error Handling', () => {
        
        test('应该处理 JSON 解析错误', async () => {
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => { throw new Error('Invalid JSON'); }
            });
            
            await expect(getAllModules()).rejects.toThrow();
        });
        
        test('应该处理超时错误', async () => {
            fetch.mockImplementationOnce(
                () => new Promise((resolve, reject) => {
                    setTimeout(() => reject(new Error('Timeout')), 100);
                })
            );
            
            await expect(getAllModules()).rejects.toThrow('Timeout');
        });
    });

    describe('Performance Tests', () => {
        
        test('批量请求应该在合理时间内完成', async () => {
            fetch.mockResolvedValue({
                ok: true,
                json: async () => []
            });
            
            const startTime = Date.now();
            
            const promises = [];
            for (let i = 0; i < 10; i++) {
                promises.push(getAllModules());
            }
            
            await Promise.all(promises);
            
            const duration = Date.now() - startTime;
            expect(duration).toBeLessThan(5000); // 应在5秒内完成
        });
    });
});

// 运行测试配置
module.exports = {
    testEnvironment: 'jsdom',
    setupFilesAfterEnv: ['<rootDir>/tests/setup.js'],
    coverageThreshold: {
        global: {
            branches: 80,
            functions: 80,
            lines: 80,
            statements: 80
        }
    }
};
