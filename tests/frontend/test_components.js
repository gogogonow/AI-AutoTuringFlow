/**
 * 前端组件测试
 * 测试 ModuleList、ModuleForm、ModuleDetails 等核心组件
 */

// 模拟依赖
jest.mock('../../frontend/js/api');

const {
    getAllModules,
    getModuleById,
    createModule,
    updateModule,
    deleteModule,
    showToast
} = require('../../frontend/js/api');

const ModuleList = require('../../frontend/js/components/ModuleList');
const ModuleForm = require('../../frontend/js/components/ModuleForm');
const ModuleDetails = require('../../frontend/js/components/ModuleDetails');

describe('ModuleList Component Tests', () => {
    
    let container;
    
    beforeEach(() => {
        container = document.createElement('div');
        container.id = 'app';
        document.body.appendChild(container);
        
        // 清除 mock
        getAllModules.mockClear();
        deleteModule.mockClear();
    });
    
    afterEach(() => {
        document.body.removeChild(container);
    });
    
    test('应该成功渲染模块列表', async () => {
        const mockModules = [
            {
                id: 1,
                serialNumber: 'SN-001',
                manufacturer: '华为',
                modelNumber: 'eSFP-GE-SX-MM850',
                wavelength: 850.0,
                transmitPower: -5.0,
                receiveSensitivity: -18.0,
                transmissionDistance: 550.0
            },
            {
                id: 2,
                serialNumber: 'SN-002',
                manufacturer: '中兴',
                modelNumber: 'Model-002',
                wavelength: 1310.0,
                transmitPower: -3.0,
                receiveSensitivity: -20.0,
                transmissionDistance: 10000.0
            }
        ];
        
        getAllModules.mockResolvedValueOnce(mockModules);
        
        await ModuleList.render(container);
        
        // 验证表格是否渲染
        const table = container.querySelector('table');
        expect(table).toBeTruthy();
        
        // 验证行数（包括表头）
        const rows = table.querySelectorAll('tr');
        expect(rows.length).toBe(3); // 1 表头 + 2 数据行
        
        // 验证数据内容
        expect(container.textContent).toContain('SN-001');
        expect(container.textContent).toContain('华为');
        expect(container.textContent).toContain('SN-002');
        expect(container.textContent).toContain('中兴');
    });
    
    test('应该显示空状态提示', async () => {
        getAllModules.mockResolvedValueOnce([]);
        
        await ModuleList.render(container);
        
        const emptyState = container.querySelector('.empty-state');
        expect(emptyState).toBeTruthy();
        expect(emptyState.textContent).toContain('暂无数据');
    });
    
    test('应该处理删除操作', async () => {
        const mockModules = [
            { id: 1, serialNumber: 'SN-001', manufacturer: '华为' }
        ];
        
        getAllModules.mockResolvedValueOnce(mockModules);
        deleteModule.mockResolvedValueOnce();
        
        // 模拟用户确认删除
        window.confirm = jest.fn(() => true);
        
        await ModuleList.render(container);
        
        const deleteButton = container.querySelector('.delete-btn');
        expect(deleteButton).toBeTruthy();
        
        deleteButton.click();
        
        // 等待异步操作完成
        await new Promise(resolve => setTimeout(resolve, 100));
        
        expect(deleteModule).toHaveBeenCalledWith(1);
        expect(window.confirm).toHaveBeenCalled();
    });
    
    test('应该处理删除取消', async () => {
        const mockModules = [
            { id: 1, serialNumber: 'SN-001', manufacturer: '华为' }
        ];
        
        getAllModules.mockResolvedValueOnce(mockModules);
        
        // 模拟用户取消删除
        window.confirm = jest.fn(() => false);
        
        await ModuleList.render(container);
        
        const deleteButton = container.querySelector('.delete-btn');
        deleteButton.click();
        
        expect(deleteModule).not.toHaveBeenCalled();
    });
    
    test('应该处理查看详情操作', async () => {
        const mockModules = [
            { id: 1, serialNumber: 'SN-001', manufacturer: '华为' }
        ];
        
        getAllModules.mockResolvedValueOnce(mockModules);
        
        const navigateMock = jest.fn();
        global.navigateTo = navigateMock;
        
        await ModuleList.render(container);
        
        const viewButton = container.querySelector('.view-btn');
        expect(viewButton).toBeTruthy();
        
        viewButton.click();
        
        expect(navigateMock).toHaveBeenCalledWith('details', { id: 1 });
    });
    
    test('应该处理错误情况', async () => {
        getAllModules.mockRejectedValueOnce(new Error('Network error'));
        
        await ModuleList.render(container);
        
        expect(showToast).toHaveBeenCalledWith(
            expect.stringContaining('错误'),
            'error'
        );
    });
});

describe('ModuleForm Component Tests', () => {
    
    let container;
    
    beforeEach(() => {
        container = document.createElement('div');
        container.id = 'app';
        document.body.appendChild(container);
        
        createModule.mockClear();
        updateModule.mockClear();
    });
    
    afterEach(() => {
        document.body.removeChild(container);
    });
    
    test('应该渲染创建表单', () => {
        ModuleForm.render(container, 'create');
        
        const form = container.querySelector('form');
        expect(form).toBeTruthy();
        
        // 验证所有必填字段
        expect(container.querySelector('[name="serialNumber"]')).toBeTruthy();
        expect(container.querySelector('[name="manufacturer"]')).toBeTruthy();
        expect(container.querySelector('[name="modelNumber"]')).toBeTruthy();
        expect(container.querySelector('[name="wavelength"]')).toBeTruthy();
        
        // 验证提交按钮
        const submitBtn = container.querySelector('[type="submit"]');
        expect(submitBtn).toBeTruthy();
        expect(submitBtn.textContent).toContain('创建');
    });
    
    test('应该渲染编辑表单并填充数据', async () => {
        const existingModule = {
            id: 1,
            serialNumber: 'SN-001',
            manufacturer: '华为',
            modelNumber: 'Model-001',
            wavelength: 850.0
        };
        
        getModuleById.mockResolvedValueOnce(existingModule);
        
        await ModuleForm.render(container, 'edit', { id: 1 });
        
        // 验证表单字段已填充
        const serialNumberInput = container.querySelector('[name="serialNumber"]');
        expect(serialNumberInput.value).toBe('SN-001');
        
        const manufacturerInput = container.querySelector('[name="manufacturer"]');
        expect(manufacturerInput.value).toBe('华为');
    });
    
    test('应该验证必填字段', async () => {
        ModuleForm.render(container, 'create');
        
        const form = container.querySelector('form');
        const submitBtn = container.querySelector('[type="submit"]');
        
        // 不填写任何字段直接提交
        submitBtn.click();
        
        // 应该显示验证错误
        const errors = container.querySelectorAll('.error-message');
        expect(errors.length).toBeGreaterThan(0);
        
        // 不应该调用 API
        expect(createModule).not.toHaveBeenCalled();
    });
    
    test('应该成功提交创建表单', async () => {
        const newModule = {
            serialNumber: 'SN-NEW',
            manufacturer: '诺基亚',
            modelNumber: 'Model-NEW',
            wavelength: 1550.0,
            transmitPower: -4.0,
            receiveSensitivity: -19.0,
            transmissionDistance: 80000.0
        };
        
        createModule.mockResolvedValueOnce({ id: 3, ...newModule });
        
        ModuleForm.render(container, 'create');
        
        // 填写表单字段
        container.querySelector('[name="serialNumber"]').value = newModule.serialNumber;
        container.querySelector('[name="manufacturer"]').value = newModule.manufacturer;
        container.querySelector('[name="modelNumber"]').value = newModule.modelNumber;
        container.querySelector('[name="wavelength"]').value = newModule.wavelength;
        
        const form = container.querySelector('form');
        form.dispatchEvent(new Event('submit'));
        
        // 等待异步操作
        await new Promise(resolve => setTimeout(resolve, 100));
        
        expect(createModule).toHaveBeenCalledWith(
            expect.objectContaining({
                serialNumber: 'SN-NEW',
                manufacturer: '诺基亚'
            })
        );
    });
    
    test('应该验证数值字段范围', () => {
        ModuleForm.render(container, 'create');
        
        const wavelengthInput = container.querySelector('[name="wavelength"]');
        
        // 测试无效值
        wavelengthInput.value = '-100';
        wavelengthInput.dispatchEvent(new Event('blur'));
        
        const errorMsg = container.querySelector('.error-message');
        expect(errorMsg).toBeTruthy();
        expect(errorMsg.textContent).toContain('有效');
    });
    
    test('应该处理表单提交错误', async () => {
        createModule.mockRejectedValueOnce(new Error('Server error'));
        
        ModuleForm.render(container, 'create');
        
        // 填写并提交表单
        container.querySelector('[name="serialNumber"]').value = 'SN-001';
        
        const form = container.querySelector('form');
        form.dispatchEvent(new Event('submit'));
        
        await new Promise(resolve => setTimeout(resolve, 100));
        
        expect(showToast).toHaveBeenCalledWith(
            expect.stringContaining('失败'),
            'error'
        );
    });
});

describe('ModuleDetails Component Tests', () => {
    
    let container;
    
    beforeEach(() => {
        container = document.createElement('div');
        container.id = 'app';
        document.body.appendChild(container);
        
        getModuleById.mockClear();
    });
    
    afterEach(() => {
        document.body.removeChild(container);
    });
    
    test('应该渲染模块详情', async () => {
        const mockModule = {
            id: 1,
            serialNumber: 'SN-001',
            manufacturer: '华为',
            modelNumber: 'eSFP-GE-SX-MM850',
            wavelength: 850.0,
            transmitPower: -5.0,
            receiveSensitivity: -18.0,
            transmissionDistance: 550.0,
            fiberType: 'MMF',
            connectorType: 'LC',
            temperatureRange: '-40~85°C',
            voltage: 3.3,
            powerConsumption: 1.5,
            createdAt: '2024-01-15T10:00:00',
            updatedAt: '2024-01-15T12:00:00'
        };
        
        getModuleById.mockResolvedValueOnce(mockModule);
        
        await ModuleDetails.render(container, { id: 1 });
        
        // 验证所有字段是否显示
        expect(container.textContent).toContain('SN-001');
        expect(container.textContent).toContain('华为');
        expect(container.textContent).toContain('eSFP-GE-SX-MM850');
        expect(container.textContent).toContain('850');
        expect(container.textContent).toContain('-5');
        expect(container.textContent).toContain('MMF');
        expect(container.textContent).toContain('LC');
    });
    
    test('应该显示编辑和查看历史按钮', async () => {
        const mockModule = {
            id: 1,
            serialNumber: 'SN-001',
            manufacturer: '华为'
        };
        
        getModuleById.mockResolvedValueOnce(mockModule);
        
        await ModuleDetails.render(container, { id: 1 });
        
        const editBtn = container.querySelector('.edit-btn');
        const historyBtn = container.querySelector('.history-btn');
        
        expect(editBtn).toBeTruthy();
        expect(historyBtn).toBeTruthy();
    });
    
    test('应该处理模块不存在的情况', async () => {
        getModuleById.mockRejectedValueOnce(new Error('Not found'));
        
        await ModuleDetails.render(container, { id: 999 });
        
        expect(showToast).toHaveBeenCalledWith(
            expect.stringContaining('未找到'),
            'error'
        );
    });
    
    test('应该格式化显示数值', async () => {
        const mockModule = {
            id: 1,
            wavelength: 850.123456,  // 应该格式化为 2 位小数
            transmitPower: -5.6789,
            voltage: 3.3,
            powerConsumption: 1.5
        };
        
        getModuleById.mockResolvedValueOnce(mockModule);
        
        await ModuleDetails.render(container, { id: 1 });
        
        // 验证数值格式化
        const content = container.textContent;
        expect(content).toContain('850.12');
        expect(content).toContain('-5.68');
    });
});

describe('Integration Tests', () => {
    
    test('完整流程：列表 -> 详情 -> 编辑 -> 列表', async () => {
        const container = document.createElement('div');
        document.body.appendChild(container);
        
        // 1. 渲染列表
        getAllModules.mockResolvedValueOnce([
            { id: 1, serialNumber: 'SN-001', manufacturer: '华为' }
        ]);
        
        await ModuleList.render(container);
        expect(container.querySelector('table')).toBeTruthy();
        
        // 2. 点击查看详情
        getModuleById.mockResolvedValueOnce({
            id: 1,
            serialNumber: 'SN-001',
            manufacturer: '华为'
        });
        
        const viewBtn = container.querySelector('.view-btn');
        viewBtn.click();
        
        // 3. 点击编辑
        await ModuleDetails.render(container, { id: 1 });
        const editBtn = container.querySelector('.edit-btn');
        expect(editBtn).toBeTruthy();
        
        document.body.removeChild(container);
    });
});

// Jest 配置
module.exports = {
    testEnvironment: 'jsdom',
    setupFiles: ['<rootDir>/tests/setup.js'],
    moduleNameMapper: {
        '\\.(css|less|scss)$': 'identity-obj-proxy'
    },
    collectCoverageFrom: [
        'frontend/js/**/*.js',
        '!frontend/js/vendor/**'
    ],
    coverageThreshold: {
        global: {
            branches: 75,
            functions: 75,
            lines: 75,
            statements: 75
        }
    }
};
