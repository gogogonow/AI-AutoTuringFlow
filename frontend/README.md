# 光模块管理系统 - 前端文档

## 项目简介

这是一个用于管理光模块信息的前端应用程序，提供了完整的 CRUD（创建、读取、更新、删除）功能以及操作历史记录查看功能。

## 技术栈

- **HTML5**: 页面结构
- **CSS3**: 样式设计，响应式布局
- **Vanilla JavaScript**: 组件化开发，无框架依赖
- **RESTful API**: 与后端通信

## 项目结构

```
frontend/
├── index.html              # 主 HTML 文件
├── styles/
│   ├── main.css           # 主样式文件
│   └── components.css     # 组件样式文件
├── js/
│   ├── api.js             # API 服务封装
│   ├── app.js             # 主应用程序
│   ├── config.js          # 全局配置（状态枚举、操作类型映射、速率选项等）
│   ├── utils.js           # 工具函数（加载动画、Toast、确认对话框、日期格式化等）
│   └── components/
│       ├── Header.js          # 顶部导航栏组件
│       ├── Login.js           # 登录页面组件
│       ├── Sidebar.js         # 侧边栏菜单组件
│       ├── ModuleList.js      # 光模块列表组件
│       ├── ModuleDetails.js   # 光模块详情组件
│       ├── ModuleForm.js      # 光模块表单组件（创建/编辑）
│       └── HistoryList.js     # 修改历史记录组件
└── README.md              # 项目文档
```

## 功能特性

### 1. 登录页面
- 用户名/密码登录，获取 JWT Token
- 登录状态持久化（本地存储）
- 未登录自动跳转到登录页

### 2. 光模块列表页面
- 显示所有光模块的基本信息（支持分页）
- 多条件搜索与筛选（序列号、速率、波长、状态等）
- 支持查看详情、编辑、删除操作
- 空状态提示
- 响应式表格设计

### 3. 光模块详情页面
- 展示光模块的完整信息
- 网格布局显示各项属性
- 提供编辑和查看历史的快捷入口

### 4. 创建/编辑光模块页面
- 完整的表单验证
- 必填字段标识
- 实时错误提示
- 支持所有光模块属性的编辑

### 5. 修改历史记录页面
- 时间线展示操作历史
- 显示字段变更的旧值和新值
- 操作类型分类（入库、出库、部署、收回、故障、维修等）

### 6. 通用功能
- JWT 认证请求头自动注入
- 加载动画（`Utils.showLoading` / `Utils.hideLoading`）
- Toast 提示消息（`Utils.showToast`）
- 确认对话框（`Utils.confirm`，支持 Promise 和回调两种用法）
- 日期格式化（`Utils.formatDateTime`）
- 响应式设计
- 浏览器历史管理

## 使用说明

### 前置要求

- 现代浏览器（Chrome、Firefox、Safari、Edge）
- 后端 API 服务运行在 `http://localhost:8000`

### 启动应用

1. 确保后端 API 服务已启动
2. 直接在浏览器中打开 `index.html` 文件，或使用本地 HTTP 服务器：

```bash
# 使用 Python 3
python -m http.server 8080

# 使用 Node.js (http-server)
npx http-server -p 8080
```

3. 在浏览器中访问 `http://localhost:8080`

### API 配置

如果后端 API 地址不是 `http://localhost:8000`，请修改 `js/api.js` 文件中的 `baseURL`：

```javascript
const API = {
    baseURL: 'http://your-api-server:port/api',
    // ...
};
```

## 组件说明

### Login 组件
- 渲染登录表单
- 调用认证 API 获取 JWT Token
- 登录成功后跳转到主页

### Header 组件
- 显示系统标题
- 显示当前登录用户信息
- 提供退出功能（清除 Token）

### Sidebar 组件
- 显示主要功能导航
- 高亮当前活动页面
- 响应式菜单

### ModuleList 组件
- 渲染光模块列表表格（支持分页与多条件筛选）
- 处理删除操作
- 提供创建入口

### ModuleDetails 组件
- 展示光模块详细信息
- 提供编辑和查看历史的入口

### ModuleForm 组件
- 处理创建和编辑逻辑
- 表单验证
- 数据提交

### HistoryList 组件
- 渲染操作历史时间线
- 显示字段变更详情

### 全局工具

#### `config.js` — 全局配置对象（`window.CONFIG`）
- `API_BASE_URL`：API 基础路径（默认 `/api`）
- `STATUS_TEXT`：光模块状态枚举的中文映射（IN_STOCK / DEPLOYED / FAULTY / UNDER_REPAIR / SCRAPPED）
- `OPERATION_TYPE_TEXT`：操作类型枚举的中文映射
- `SPEED_OPTIONS`：端口速率选项列表
- `CONNECTOR_TYPE_OPTIONS`：接口类型选项列表

#### `utils.js` — 工具函数类（`window.Utils`）
- `showLoading()` / `hideLoading()`：全局加载遮罩
- `showToast(message, type)`：浮动提示消息（success / error / warning）
- `confirm(message, onConfirm?)`：确认对话框（支持回调和 Promise 两种用法）
- `formatDateTime(dateString)`：将日期字符串格式化为 `YYYY-MM-DD HH:mm:ss`
- `getStatusText(status)` / `getOperationTypeText(type)`：枚举值转中文文本
- `renderErrorState(message)` / `renderEmptyState(icon, text)`：渲染错误/空状态占位符
- `escapeHtml(text)`：HTML 转义防 XSS

## 样式主题

### 主要颜色
- 主色调: #3498db (蓝色)
- 成功: #27ae60 (绿色)
- 警告: #f39c12 (橙色)
- 危险: #e74c3c (红色)
- 深色: #2c3e50 (深蓝灰)
- 浅色: #ecf0f1 (浅灰)

### 响应式断点
- 桌面: > 768px
- 平板: 576px - 768px
- 移动: < 576px

## 浏览器支持

- Chrome (最新版本)
- Firefox (最新版本)
- Safari (最新版本)
- Edge (最新版本)

## 开发指南

### 添加新组件

1. 在 `js/components/` 目录下创建新组件文件
2. 使用 ES6 类定义组件
3. 实现 `render()` 方法
4. 在 `index.html` 中引入组件脚本
5. 在 `app.js` 中添加路由逻辑

### 组件模板

```javascript
class MyComponent {
    constructor(container, ...args) {
        this.container = container;
        // 初始化
    }
    
    async render() {
        // 渲染逻辑
        this.container.innerHTML = `...`;
        this.attachEventListeners();
    }
    
    attachEventListeners() {
        // 事件监听
    }
}
```

## 常见问题

### API 连接失败
- 检查后端服务是否运行
- 检查 API baseURL 配置是否正确
- 检查浏览器控制台的错误信息

### 样式显示异常
- 清除浏览器缓存
- 检查 CSS 文件是否正确加载

### 功能不响应
- 检查浏览器控制台的 JavaScript 错误
- 确认所有 JS 文件都已正确加载

## 许可证

MIT License
