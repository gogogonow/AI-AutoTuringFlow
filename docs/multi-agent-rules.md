# Multi-Agent 协作规则（Multi-Agent Rules）

> **强制约束**：本文件定义的所有规则对系统中每一个 Agent 均强制有效。
> Agent 在开始任何任务前，必须先读取本文件和 `docs/project-context.md`。

---

## 1. 核心原则

### 原则一：上下文优先（Context First）

**Agent 在开始任务前，必须先读取以下文档，不得跳过：**

1. `docs/project-context.md`（项目基本信息与技术栈）
2. `docs/domain-glossary.md`（领域术语，确保术语一致）
3. `docs/module-boundaries.md`（职责边界，确认自己的作业范围）
4. `docs/architecture.md`（架构约束，确保方案合规）
5. `.ai_architect_plan.md`（当前任务的架构方案，如存在）
6. **`docs/frontend-component-spec.md`（前端组件规范，含前端任务时必读）**

### 原则二：边界遵守（Boundary Compliance）

- **Frontend Agent** 只能修改 `frontend/` 目录下的文件，禁止修改 `backend/` 目录。
- **Backend Agent** 只能修改 `backend/` 目录下的文件，禁止修改 `frontend/` 目录。
- **Architect Agent** 不生成代码，只输出规划文档。
- **Review Agent** 只做检查和修复，不新增功能。

### 原则三：不臆测（No Speculation）

- Agent **不得凭空假设**业务规则、API 格式、数据库结构。
- 当信息不足时，Agent 应**停止并说明需要补充哪些上下文**，而不是自行猜测。
- 如果任务描述与已有代码或文档存在矛盾，Agent 应**指出矛盾并请求澄清**。

### 原则四：最小化修改（Minimal Change）

- Bug 修复和 Upgrade 模式下，Agent 应使用 `patch_code_tool` 进行**最小化增量修改**，避免重写无关代码。
- 不得修改与当前任务无关的文件。

---

## 2. Agent 角色与职责

### 2.1 首席系统架构师（Architect Agent）

**激活条件**：始终参与，运行于 `plan_crew` 阶段

**职责**：
- 读取 Issue 需求
- 读取项目上下文文档
- 设计完整的技术方案（API 契约、数据库变更、文件清单）
- 将方案输出到 `.ai_architect_plan.md`

**输出规范**（以下章节为必须输出）：
```markdown
## 任务概述
## 影响分析
## API 契约
## 数据库变更
## 文件清单
## 实现指导
```

**禁止行为**：
- ❌ 不生成实际代码（架构规划阶段）
- ❌ 不跳过 API 契约定义（即使是纯前端或纯后端任务）
- ❌ 不使用未在领域词典中定义的术语（应先确认或扩展词典）

---

### 2.2 UI/UX 交互设计师（UI Designer Agent）

**激活条件**：`mode:feature`（含前端范围）或 `mode:ui-beautify`

**职责**：
- 基于架构方案设计界面布局和交互流程
- 输出视觉规范、组件列表、交互说明
- 为 Frontend Agent 提供明确的实现指导
- **必须先读取 `docs/frontend-component-spec.md`，在组件清单中明确标注哪些是复用已有组件、哪些是新建**

**输出规范**：
```markdown
## 界面布局描述
## 组件清单（每项注明：复用/扩展/新建）
  - [复用] ModuleList — 列表视图无需修改
  - [扩展] ModuleForm — 在现有表单中新增一个字段
  - [新建] BatchImport — 新增批量导入模态框（需要新 CSS 类）
## 交互说明
## 视觉规范（配色、字体、间距，必须使用 variables.css 中已有的 CSS 变量）
## 新增/修改的 CSS 类（仅新建/扩展组件时填写，写入 components.css）
```

**禁止行为**：
- ❌ 不生成实际代码
- ❌ 不建议使用框架（前端无框架约束）
- ❌ 不建议使用内联 style= 属性替代 CSS 类
- ❌ 不建议重新实现 `components.css` 中已有的 CSS 类

---

### 2.3 高级前端工程师（Frontend Dev Agent）

**激活条件**：含前端范围时参与

**职责**：
- 读取架构方案和 UI 设计方案
- 生成/修改 `frontend/` 下的 HTML/CSS/JS 文件
- 编写前端测试并通过 `execute_command_tool` 运行

**输入约定**：
- 必须先读取 `docs/project-context.md`、`docs/module-boundaries.md`
- **必须先读取 `docs/frontend-component-spec.md`（组件规范、共享 Utils、CSS 类清单）**
- 必须先读取 `.ai_architect_plan.md` 中的 API 契约部分
- feature 模式：直接生成新文件
- upgrade/bugfix 模式：先用 `read_code_tool` 读取现有文件，再用 `patch_code_tool` 增量修改

**组件复用检查（必须在生成代码前完成）**：
```
□ 检查 js/components/ 下是否有可复用/扩展的组件
□ 确认使用 Utils.renderErrorState() 而非自定义 errorState 模板
□ 确认使用 Utils.renderEmptyState() 而非自定义 emptyState 模板
□ 确认状态文本通过 Utils.getStatusText() 获取（不要内置 statusMap）
□ 确认操作类型文本通过 Utils.getOperationTypeText() 获取（不要内置 typeMap）
□ 确认新增 CSS 类写入 components.css 而非内联样式
□ 确认 API 字段使用 camelCase（对齐后端 Jackson 序列化）
```

**输出约定（JSON 格式）**：
```json
{
  "files_modified": [],
  "components_reused": [],
  "components_new": [],
  "api_dependencies": [],
  "test_results": "passed/failed",
  "deliverables_summary": ""
}
```

---

### 2.4 高级后端工程师（Backend Dev Agent）

**激活条件**：含后端范围时参与，`mode:ui-beautify` 不参与

**职责**：
- 读取架构方案
- 生成/修改 `backend/` 下的 Java 代码
- 编写后端测试并通过 `execute_command_tool` 运行

**输入约定**：
- 必须先读取 `docs/project-context.md`、`docs/module-boundaries.md`
- 必须先读取 `.ai_architect_plan.md` 中的 API 契约和数据库变更部分
- feature 模式：直接生成新文件
- upgrade/bugfix 模式：先用 `read_code_tool` 读取现有文件，再用 `patch_code_tool` 增量修改

**输出约定（JSON 格式）**：
```json
{
  "files_modified": [],
  "apis_implemented": [],
  "migration_scripts": [],
  "test_results": "passed/failed",
  "deliverables_summary": ""
}
```

---

### 2.5 代码审查工程师（Review Agent）

**激活条件**：始终参与

**职责**：
- 验证 Entity↔DTO 一致性
- 验证 frontend API 调用↔backend API 实现一致性
- 验证 DDL↔JPA 实体一致性
- 发现问题时用 `patch_code_tool` 直接修复（不仅仅是报告）

**检查清单**：
```
□ API 路径与 HTTP 方法是否与架构方案一致
□ 请求/响应字段名是否前后端一致（camelCase）
□ 枚举值字符串是否前后端一致
□ 错误响应格式是否统一
□ JPA 实体使用 jakarta.persistence.* 而非 javax.persistence.*
□ DTO 与实体字段是否对齐
□ 操作历史是否在所有写操作中写入
□ 前端是否有重复实现 Utils 已有的 renderErrorState/renderEmptyState 模板？
□ 前端组件是否有各自内置 statusMap/typeMap（应统一使用 Utils/CONFIG）？
```

---

## 3. 前端组件复用规则（Frontend Component Reuse）

> 本节专门针对 multi-agent 在前端任务中容易产生重复组件的问题，给出强制规范。

### 3.1 重复组件的根因

multi-agent 在处理前端任务时常见的重复模式和根因：

| 重复模式 | 根因 | 解决方案 |
|---------|------|---------|
| 每个组件都有自己的 `renderErrorState()` 模板 | Agent 没有检查共享 Utils 就直接生成 | 强制要求读取 `frontend-component-spec.md` |
| 每个组件都有自己的 `renderEmptyState()` 模板 | 同上 | 使用 `Utils.renderEmptyState()` |
| 每个组件都有自己的 `statusMap`/`typeMap` | 状态枚举未集中管理 | 统一使用 `config.js` + `Utils.getStatusText()` |
| `api.js` 中混入 UI 工具类（Utils） | 职责边界不清，agent 在错误的文件中添加代码 | Utils 归属 `utils.js`，api.js 只负责 HTTP 调用 |
| 同一 HTML 结构出现在多个组件的不同位置 | 没有提取公共片段 | 使用 `Utils.renderXxx()` 共享方法 |
| 新 agent 创建了完全不同的 HTML 结构（如 `index.html` 重写） | UI Agent 未检查现有实现就从头设计 | UI Agent 必须先读取现有组件文件 |

### 3.2 UI Agent 与 Frontend Agent 的协作规则

```
UI Designer Agent
  │ 读取 frontend-component-spec.md
  │ 读取 js/components/ 现有文件（了解已有实现）
  │ 输出组件清单，明确标注：复用/扩展/新建
  ↓
Frontend Dev Agent
  │ 读取 UI Designer 输出的组件清单
  │ 对于"复用"项：直接使用，不修改
  │ 对于"扩展"项：在现有文件中增量修改（patch_code_tool）
  │ 对于"新建"项：
  │   □ 先检查 Utils/CONFIG 有无可复用逻辑
  │   □ 复用 components.css 中已有 CSS 类
  │   □ 在 index.html 正确位置添加 <script> 标签
  ↓
Review Agent
  │ 验证新增组件是否有重复实现 Utils 已有功能
  │ 发现重复则用 patch_code_tool 替换为 Utils.* 调用
```

### 3.3 ui-beautify 模式的特殊约束

`mode:ui-beautify` 是最容易产生组件重复的场景（UI 美化任务往往重写整个 HTML/CSS）。

**强制约束**：
- UI Designer 必须先读取 `index.html` 和所有 `js/components/*.js` 文件
- 禁止重写 `index.html` 整体结构，只允许在现有结构上修改
- CSS 美化只能修改 `styles/*.css`，不能在组件 JS 中引入内联样式
- 如果需要大幅调整 HTML 结构，必须在架构师规划阶段讨论，不能在 ui-beautify 阶段直接替换

---

## 4. 任务执行流程

### 4.1 标准执行流程

```
Issue 创建（用户打 run-ai 标签）
    │
    ▼
0. 系统加载固定项目上下文 → 注入到所有 Agent 的 backstory
    │ context_loader.get_context_for_role() 按角色裁剪上下文
    ▼
1. Architect Agent（backstory 包含完整项目上下文 + 领域词典）
    │ 读取 Issue + 在项目上下文范围内设计方案
    │ 输出：.ai_architect_plan.md
    ▼
2. [Human-in-the-Loop 审批]
    │
    ▼
3. [UI Designer Agent]（条件参与，backstory 包含前端相关上下文）
    │ 读取：架构方案
    │ 输出：UI 规范
    ▼
4. Frontend Dev Agent（条件参与，backstory 包含前端上下文 + 模块边界）
    │ 在项目上下文约束下编写前端代码
    │ 产出：前端代码变更
    ▼
5. Backend Dev Agent（条件参与，backstory 包含后端上下文 + 架构约束）
    │ 在项目上下文约束下编写后端代码
    │ 产出：后端代码变更
    ▼
6. Review Agent（backstory 包含完整上下文 + 领域词典 + 协作规则）
    │ 基于上下文校验一致性
    │ 产出：修复后的一致性代码
    ▼
7. Context Refresh Agent（Agent 驱动的上下文增量刷新）
    │ 分析代码变更 → 语义提取新术语/新API/新实体
    │ 使用 patch_code_tool 更新 docs/ 下的上下文文档
    ▼
8. 创建 Pull Request
```

### 4.2 上下文不足时的处理流程

```
Agent 发现上下文不足
    │
    ▼
1. 先尝试通过 search_code_tool 和 read_code_tool 从已有代码中补充信息
    │
    ▼
2. 若仍不足：在 Issue 评论或任务输出中明确说明缺失的信息
    │
    ▼
3. 不得自行臆测，不得基于假设生成代码
    │
    ▼
4. 若有上下文文档需要更新，触发 refresh-context.sh 流程
```

---

## 5. 禁止行为清单

以下行为对所有 Agent 均明确禁止：

| 禁止行为 | 严重程度 | 说明 |
|---------|---------|------|
| 未读取项目上下文就开始任务 | 🔴 严重 | 会导致与项目业务完全脱节 |
| 凭空假设 API 路径或字段名 | 🔴 严重 | 会导致前后端不一致 |
| 修改超出自己职责范围的文件 | 🔴 严重 | 会破坏模块边界 |
| 在前端实现业务逻辑或权限判断 | 🟠 高 | 违反架构约束 |
| 在后端返回 JPA 实体（而非 DTO） | 🟠 高 | 暴露内部数据结构 |
| 使用 `javax.persistence.*` | 🟠 高 | Spring Boot 3.x 不兼容 |
| 跳过写操作的 History 记录 | 🟠 高 | 违反审计要求 |
| 在代码中硬编码密码/Token | 🔴 严重 | 安全漏洞 |
| 生成需要构建工具才能运行的前端代码 | 🟡 中 | 前端无构建工具约束 |
| Review Agent 发现问题不修复只报告 | 🟡 中 | 审查责任要求主动修复 |
| **在组件中重新实现 Utils 已有的 renderErrorState/renderEmptyState 模板** | 🟠 高 | 导致错误状态 UI 不一致，增加维护成本 |
| **在组件中内置 statusMap/typeMap 替代 Utils.getStatusText()** | 🟠 高 | 枚举值散落多处，修改时容易漏改 |
| **重写 index.html 整体结构（ui-beautify 模式）** | 🔴 严重 | 会破坏现有组件挂载逻辑，产生平行实现 |
| **在 api.js 中添加 UI 工具函数** | 🟠 高 | 职责混淆，应在 utils.js 中统一管理 |
| **使用 snake_case 字段名访问后端 API 数据** | 🟠 高 | 后端 Jackson 默认 camelCase 序列化 |

---

## 6. 上下文刷新触发条件

以下情况发生时，项目上下文会被自动或手动刷新：

### 5.1 自动刷新（Agent 驱动）

在每次 Crew 执行（代码生成 + 审查）完成后，系统会自动启动**项目上下文刷新分析师 Agent**：
- 该 Agent 使用 `extract_code_change_summary()` 获取本次代码变更的结构化摘要
- 语义分析变更中新增的业务实体、API、前端页面、领域术语
- 使用 `patch_code_tool` 增量更新 `docs/domain-glossary.md`、`docs/project-context.md` 等文档
- 与纯 bash 脚本不同，Agent 能理解业务语义，从代码变更中提取有意义的信息

### 5.2 手动刷新（脚本辅助）

运行 `scripts/refresh-context.sh` 可以：
- 更新时间戳
- 生成结构化快照
- 检测上下文敏感的文件变更
- 输出需要手动更新的文档清单

### 5.3 触发条件

1. 新增或修改了核心业务实体（`Module`、`History` 等）
2. 新增或修改了 API 接口（路径、参数、响应格式）
3. 修改了前端目录结构或新增了主要页面
4. 修改了技术栈（升级框架版本、引入新依赖）
5. 发现领域词典中缺少的新术语
6. 架构约束发生变更
7. 完成一个较大的 feature 后，更新整体状态

详见 [`scripts/refresh-context.sh`](../scripts/refresh-context.sh) 和 [`tools/context_loader.py`](../tools/context_loader.py)。
