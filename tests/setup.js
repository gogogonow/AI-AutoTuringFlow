/**
 * Jest 测试环境设置文件
 * 配置全局测试环境和模拟对象
 */

// 导入测试工具库
import '@testing-library/jest-dom';

// 设置全局超时时间（毫秒）
jest.setTimeout(10000);

// 模拟浏览器 API
global.localStorage = {
    store: {},
    getItem(key) {
        return this.store[key] || null;
    },
    setItem(key, value) {
        this.store[key] = value.toString();
    },
    removeItem(key) {
        delete this.store[key];
    },
    clear() {
        this.store = {};
    }
};

// 模拟 sessionStorage
global.sessionStorage = {
    store: {},
    getItem(key) {
        return this.store[key] || null;
    },
    setItem(key, value) {
        this.store[key] = value.toString();
    },
    removeItem(key) {
        delete this.store[key];
    },
    clear() {
        this.store = {};
    }
};

// 模拟 window.location
delete window.location;
window.location = {
    href: 'http://localhost:3000',
    pathname: '/',
    search: '',
    hash: '',
    reload: jest.fn()
};

// 模拟 window.alert
global.alert = jest.fn();

// 模拟 window.confirm
global.confirm = jest.fn(() => true);

// 模拟 window.prompt
global.prompt = jest.fn();

// 模拟 console 方法（避免测试输出污染）
global.console = {
    ...console,
    log: jest.fn(),
    debug: jest.fn(),
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn()
};

// 模拟 fetch API
global.fetch = jest.fn();

// 每个测试前重置所有 mock
beforeEach(() => {
    // 清除所有 mock 调用记录
    jest.clearAllMocks();
    
    // 重置 localStorage 和 sessionStorage
    global.localStorage.clear();
    global.sessionStorage.clear();
    
    // 重置 DOM
    document.body.innerHTML = '';
    document.head.innerHTML = '';
    
    // 重置 fetch mock
    global.fetch.mockClear();
});

// 每个测试后清理
afterEach(() => {
    // 清理定时器
    jest.clearAllTimers();
    
    // 恢复所有 mock
    jest.restoreAllMocks();
});

// 测试套件完成后的清理
afterAll(() => {
    // 清理可能残留的资源
    jest.clearAllTimers();
});

// 自定义匹配器
expect.extend({
    toBeValidModule(received) {
        const requiredFields = [
            'serialNumber',
            'manufacturer',
            'modelNumber',
            'wavelength'
        ];
        
        const pass = requiredFields.every(field => 
            received.hasOwnProperty(field) && received[field] !== null
        );
        
        if (pass) {
            return {
                message: () => `expected ${received} not to be a valid module`,
                pass: true
            };
        } else {
            return {
                message: () => `expected ${received} to be a valid module with all required fields`,
                pass: false
            };
        }
    },
    
    toHaveValidWavelength(received) {
        const wavelength = received.wavelength;
        const validWavelengths = [850, 1310, 1490, 1550];
        const pass = validWavelengths.includes(wavelength);
        
        if (pass) {
            return {
                message: () => `expected ${wavelength} not to be a valid wavelength`,
                pass: true
            };
        } else {
            return {
                message: () => `expected ${wavelength} to be one of ${validWavelengths.join(', ')}`,
                pass: false
            };
        }
    }
});

// 错误处理：捕获未处理的 Promise 拒绝
process.on('unhandledRejection', (reason, promise) => {
    console.error('Unhandled Rejection at:', promise, 'reason:', reason);
});

// 导出配置供测试使用
module.exports = {
    testTimeout: 10000,
    setupFiles: ['<rootDir>/tests/setup.js']
};
