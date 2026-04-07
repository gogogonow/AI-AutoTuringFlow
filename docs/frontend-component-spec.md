# 前端组件规范（Frontend Component Spec）

> **强制约束**：所有前端 Agent（UI Designer、Frontend Dev）在任何前端任务开始前，**必须先读取本文件**。
> 本文件定义了前端唯一允许使用的组件结构、工具类及复用规则，禁止在组件内重复实现本文件已定义的模式。

---

## 1. 技术栈约束

| 维度 | 约束 |
|------|------|
| **框架** | 纯 HTML5 + CSS3 + ES6 JavaScript，**无构建工具，无前端框架** |
| **组件模式** | 原生 ES6 Class，挂载到 `window.ComponentName` |
| **CSS 命名** | BEM-like 语义命名，使用 `styles/variables.css` 中的 CSS 变量 |
| **服务端** | Nginx 静态文件服务，所有 `/api/*` 路径代理到后端 |
| **禁止** | React/Vue/Angular、Webpack/Vite、npm 依赖、内联 `style=` 属性（布局除外） |

---

## 2. 文件加载顺序（index.html）

```html
<!-- CSS：variables → main → components → module-list → history-list → responsive -->
<link rel="stylesheet" href="styles/reset.css">
<link rel="stylesheet" href="styles/variables.css">
<link rel="stylesheet" href="styles/main.css">
<link rel="stylesheet" href="styles/components.css">
<link rel="stylesheet" href="styles/module-list.css">
<link rel="stylesheet" href="styles/history-list.css">
<link rel="stylesheet" href="styles/responsive.css">

<!-- JS：config → utils → api → components → app -->
<script src="js/config.js"></script>
<script src="js/utils.js"></script>
<script src="js/api.js"></script>
<script src="js/components/Header.js"></script>
<script src="js/components/Sidebar.js"></script>
<script src="js/components/ModuleList.js"></script>
<script src="js/components/ModuleForm.js"></script>
<script src="js/components/ModuleDetails.js"></script>
<script src="js/components/HistoryList.js"></script>
<script src="js/app.js"></script>
```

> ⚠️ 新增 JS 文件时，在 `app.js` 之前加载，在 `api.js` 之后加载。

---

## 3. 目录结构

```
frontend/
├── index.html                  # 应用入口（唯一 HTML 文件）
├── styles/
│   ├── reset.css               # CSS 重置
│   ├── variables.css           # CSS 变量（颜色、间距、字体等）
│   ├── main.css                # 全局布局（header、sidebar、loading、toast）
│   ├── components.css          # 共享组件 CSS（card、btn、table、form、timeline 等）
│   ├── module-list.css         # 模块列表页专属样式
│   ├── history-list.css        # 历史记录页专属样式
│   └── responsive.css          # 响应式断点
├── js/
│   ├── config.js               # 全局常量（API base, STATUS_TEXT, OPERATION_TYPE_TEXT）
│   ├── utils.js                # Utils 工具类 + 辅助函数（唯一共享工具入口）
│   ├── api.js                  # API 客户端封装（仅 API 调用，不包含 UI 逻辑）
│   ├── app.js                  # App 主类（路由、组件挂载）
│   └── components/
│       ├── Header.js           # 顶部导航栏
│       ├── Sidebar.js          # 侧边栏菜单
│       ├── ModuleList.js       # 光模块列表
│       ├── ModuleForm.js       # 光模块创建/编辑表单
│       ├── ModuleDetails.js    # 光模块详情
│       └── HistoryList.js      # 修改历史记录
```

---

## 4. 共享工具：Utils 类（js/utils.js）

> **核心规范**：所有组件必须通过 `Utils.*` 调用共享功能。**禁止在组件内重新实现 Utils 中已有的功能。**

| 方法 | 说明 | 已替代的重复模式 |
|------|------|----------------|
| `Utils.showLoading()` | 显示全屏加载遮罩 | 各组件自行操作 `#loading` |
| `Utils.hideLoading()` | 隐藏加载遮罩 | 同上 |
| `Utils.showToast(msg, type)` | 显示 Toast 消息（success/error/warning） | 各组件自行操作 `#toast` |
| `Utils.confirm(msg, onConfirm)` | 自定义确认对话框 | 原生 `confirm()` |
| `Utils.formatDateTime(str)` | 格式化日期时间字符串 | 各组件自行用 `new Date()` 格式化 |
| `Utils.getStatusClass(status)` | 返回状态对应的 CSS class | 各组件内置 statusMap |
| `Utils.getStatusText(status)` | 返回状态中文文本 | 各组件内置 statusMap |
| `Utils.getOperationTypeText(type)` | 返回操作类型中文文本 | `HistoryList` 内置 typeMap |
| `Utils.renderErrorState(msg, retryHtml)` | 渲染统一的错误状态卡片 HTML | 各组件独立的 `⚠️ 加载失败` 模板 |
| `Utils.renderEmptyState(icon, text, actionHtml)` | 渲染统一的空数据状态 HTML | 各组件独立的 empty-state 模板 |

### 使用示例

```javascript
// ✅ 正确：使用共享工具
catch (error) {
    Utils.hideLoading();
    Utils.showToast('加载失败: ' + error.message, 'error');
    this.container.innerHTML = Utils.renderErrorState(
        '加载失败',
        `<button class="btn btn-secondary" onclick="window.app.showPage('list')">返回列表</button>`
    );
}

// ❌ 错误：不要在组件内自行实现 errorState 模板
this.container.innerHTML = `
    <div class="card">
        <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            ...
        </div>
    </div>
`;
```

---

## 5. 共享常量：CONFIG（js/config.js）

```javascript
CONFIG.API_BASE_URL          // '/api'
CONFIG.DEFAULT_PAGE_SIZE     // 20
CONFIG.STATUS_TEXT           // { IN_STOCK: '在库', DEPLOYED: '已部署', ... }
CONFIG.OPERATION_TYPE_TEXT   // { INBOUND: '入库', OUTBOUND: '出库', ... }
```

> 禁止在组件内硬编码状态文本或操作类型文本。新增枚举值时，先更新 `config.js`，再更新后端枚举。

---

## 6. 共享 CSS 类（css/components.css）

以下 CSS 类在所有组件中通用，**禁止在组件内用内联样式重新实现**：

### 卡片容器
```html
<div class="card">
  <div class="card-header">
    <h2 class="card-title">标题</h2>
    <div class="card-actions"><!-- 操作按钮 --></div>
  </div>
  <!-- 内容 -->
</div>
```

### 按钮
```html
<button class="btn btn-primary">主操作</button>
<button class="btn btn-secondary">次操作</button>
<button class="btn btn-warning">警告操作</button>
<button class="btn btn-danger">危险操作</button>
<button class="btn btn-success">成功操作</button>
<button class="btn btn-primary btn-sm">小按钮</button>
```

### 状态徽章
```html
<span class="status-badge status-active">活跃</span>
<span class="status-badge status-inactive">停用</span>
<span class="status-badge status-maintenance">维护中</span>
<!-- 对应 config.js 中的大写枚举 -->
<span class="status-badge status-in_stock">在库</span>
<span class="status-badge status-deployed">已部署</span>
<span class="status-badge status-faulty">故障</span>
<span class="status-badge status-under_repair">维修中</span>
<span class="status-badge status-scrapped">已报废</span>
```

### 数据表格
```html
<div class="table-container">
  <table class="table">
    <thead><tr><th>列名</th></tr></thead>
    <tbody><tr><td>数据</td></tr></tbody>
  </table>
</div>
```

### 表单
```html
<div class="form-row">
  <div class="form-col">
    <div class="form-group">
      <label class="form-label">字段名 *</label>
      <input class="form-control" type="text">
      <div class="form-error" id="error-fieldname"></div>
    </div>
  </div>
</div>
<div class="form-actions">
  <button class="btn btn-secondary">取消</button>
  <button class="btn btn-success" type="submit">保存</button>
</div>
```

### 详情网格
```html
<div class="details-grid">
  <div class="detail-item">
    <div class="detail-label">字段标签</div>
    <div class="detail-value">字段值</div>
  </div>
</div>
```

### 时间线（历史记录）
```html
<div class="timeline">
  <div class="timeline-item">
    <div class="timeline-content">
      <div class="timeline-header">
        <span class="timeline-type">操作类型</span>
        <span class="timeline-time">时间</span>
      </div>
      <div class="timeline-changes">
        <div class="change-item">
          <div class="change-label">字段名</div>
          <div class="change-values">
            <span class="old-value">旧值</span>
            <span>→</span>
            <span class="new-value">新值</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
```

---

## 7. 组件开发规范

### 7.1 标准组件结构

```javascript
// js/components/MyComponent.js
class MyComponent {
    constructor(container, /* params */) {
        this.container = container;
        // ... 初始化属性
    }

    async render() {
        try {
            Utils.showLoading();
            const data = await API.modules.getXxx();
            Utils.hideLoading();

            this.container.innerHTML = `
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">标题</h2>
                        <div class="card-actions"><!-- 操作按钮 --></div>
                    </div>
                    ${data.length === 0
                        ? Utils.renderEmptyState('📋', '暂无数据')
                        : this.renderContent(data)
                    }
                </div>
            `;
            this.attachEventListeners();
        } catch (error) {
            Utils.hideLoading();
            Utils.showToast('加载失败: ' + error.message, 'error');
            this.container.innerHTML = Utils.renderErrorState(
                '加载失败',
                `<button class="btn btn-secondary" onclick="window.app.showPage('list')">返回列表</button>`
            );
        }
    }

    renderContent(data) {
        // 渲染主体内容
    }

    attachEventListeners() {
        // 绑定事件
    }
}
```

### 7.2 复用检查清单

在开始实现 UI 之前，必须检查：

- [ ] **Utils 方法是否已覆盖**：`showLoading/showToast/confirm/formatDateTime/getStatusText/renderErrorState/renderEmptyState`？
- [ ] **CSS 类是否已存在**：在 `components.css` / `module-list.css` / `history-list.css` 中查找？
- [ ] **CONFIG 枚举是否已定义**：状态文本、操作类型文本是否在 `config.js` 中？
- [ ] **同类组件是否已存在**：`js/components/` 下是否有类似组件可以扩展？
- [ ] **API 方法是否已封装**：`api.js` 中是否已有对应接口调用？

> 如果以上任一项已存在，**必须复用，不得重复实现**。

---

## 8. API 调用规范

所有 API 调用必须通过 `api.js` 的 `API` 对象，禁止在组件内直接使用 `fetch()`。

```javascript
// ✅ 正确
const modules = await API.modules.getAll();
const module = await API.modules.getById(id);
await API.modules.create(data);
await API.modules.update(id, data);
await API.modules.delete(id);
const history = await API.modules.getHistory(id);

// ❌ 错误：直接使用 fetch
const res = await fetch('/api/modules');
```

### 字段名约定（对齐后端 Java camelCase 序列化）

| 后端 Java 字段 | JSON/前端字段 | 中文含义 |
|---------------|--------------|---------|
| `id` | `id` | 主键 |
| `serialNumber` | `serialNumber` | 序列号 |
| `manufacturer` | `manufacturer` | 制造商 |
| `modelNumber` | `modelNumber` | 型号 |
| `wavelength` | `wavelength` | 波长 (nm) |
| `transmitPower` | `transmitPower` | 发射功率 |
| `receiveSensitivity` | `receiveSensitivity` | 接收灵敏度 |
| `transmissionDistance` | `transmissionDistance` | 传输距离 |
| `fiberType` | `fiberType` | 光纤类型 |
| `connectorType` | `connectorType` | 连接器类型 |
| `temperatureRange` | `temperatureRange` | 温度范围 |
| `voltage` | `voltage` | 电压 |
| `powerConsumption` | `powerConsumption` | 功耗 |
| `createdAt` | `createdAt` | 创建时间 |
| `updatedAt` | `updatedAt` | 更新时间 |

> ⚠️ 后端使用 Jackson 默认 camelCase 序列化，前端直接使用 `module.serialNumber`，不使用 `module.serial_number`。

---

## 9. 新增组件/功能的操作流程

```
1. 读取本文件（frontend-component-spec.md）
2. 读取 docs/module-boundaries.md（确认职责边界）
3. 读取 docs/domain-glossary.md（确认术语）
4. 检查 js/components/ 是否有可复用/扩展的组件
5. 检查 Utils 是否有可复用方法
6. 检查 components.css 是否有可复用 CSS 类
7. 如需新增 CSS 类，写入 components.css（不是内联样式）
8. 实现组件，继承标准结构（参考 §7.1）
9. 在 index.html 的正确位置添加 <script> 加载新组件
10. 在 app.js 的 showPage() switch 中注册新路由
```

---

## 10. 禁止行为

| 禁止行为 | 正确做法 |
|---------|---------|
| ❌ 在组件内自定义 errorState HTML 模板 | ✅ 使用 `Utils.renderErrorState()` |
| ❌ 在组件内自定义 emptyState HTML 模板 | ✅ 使用 `Utils.renderEmptyState()` |
| ❌ 在组件内自行实现 Loading 显示/隐藏 | ✅ 使用 `Utils.showLoading()` / `Utils.hideLoading()` |
| ❌ 在组件内自行定义 statusMap/typeMap/fieldMap | ✅ 使用 `Utils.getStatusText()` / `Utils.getOperationTypeText()` |
| ❌ 在 `api.js` 中添加 UI 逻辑 | ✅ UI 逻辑只在 components 中 |
| ❌ 使用内联 `style=` 代替 CSS 类 | ✅ 将样式提取到对应 CSS 文件 |
| ❌ 在组件内直接使用 `fetch()` | ✅ 使用 `API.modules.*` |
| ❌ 引入 npm 包或构建工具 | ✅ 纯原生 ES6 + 现有 CSS 变量体系 |
| ❌ 使用 `snake_case` 字段名访问 API 数据 | ✅ 使用 `camelCase`（对齐后端序列化） |
| ❌ 创建 `main.css` 以外的全局布局 CSS | ✅ 只在 `components.css` 或页面专属 CSS 中添加 |

---

## 11. 已知的历史遗留问题（待修复）

| 问题 | 位置 | 优先级 |
|------|------|--------|
| 组件中使用 `snake_case` 字段名（如 `module.module_number`）而后端返回 camelCase | `ModuleForm.js`, `ModuleDetails.js`, `ModuleList.js` | 🟠 高 |
| 状态枚举不统一（组件用 `active/inactive`，CONFIG 用 `IN_STOCK/DEPLOYED`，后端无 status 字段） | `ModuleList.js`, `api.js`, `config.js` | 🟠 高 |
| `js/components/` 与后端 Module 实体字段名对齐 | 所有 components | 🟡 中 |
