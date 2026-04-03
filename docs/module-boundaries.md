# 子模块职责边界（Module Boundaries）

> 本文件明确 frontend 与 backend 子模块的职责边界，以及禁止越界的行为。
> Multi-agent 在执行任务前必须确认自己的作业范围，不得超越职责边界生成代码。

---

## 1. 整体分层结构

```
┌────────────────────────────────────────────────────┐
│               用户浏览器（User Browser）              │
├────────────────────────────────────────────────────┤
│          frontend/（Nginx + 静态资源）               │
│  HTML + CSS + JavaScript（无框架，原生实现）          │
│  职责：界面渲染、交互逻辑、表单校验、API 调用封装      │
├────────────────────────────────────────────────────┤
│           Nginx 反向代理（/api/* → backend）         │
├────────────────────────────────────────────────────┤
│       backend/（Spring Boot 3.2 / Java 21）         │
│  职责：业务逻辑、数据校验、持久化、权限、审计日志       │
├────────────────────────────────────────────────────┤
│              数据库（H2 / PostgreSQL）               │
└────────────────────────────────────────────────────┘
```

---

## 2. Frontend 子模块

### 2.1 目录结构

```
frontend/
├── index.html              # 应用入口，单页或多页
├── styles/                 # CSS 样式文件
│   └── *.css
├── js/                     # JavaScript 业务逻辑
│   └── *.js
├── nginx.conf              # Nginx 配置（envsubst 模板）
├── Dockerfile              # 容器构建
└── README.md               # 前端独立说明
```

### 2.2 Frontend 的职责（CAN DO）

| 职责 | 说明 |
|------|------|
| 界面渲染 | 根据后端返回数据动态渲染 HTML 内容 |
| 交互逻辑 | 按钮事件、表单提交、模态框、分页切换 |
| 前端表单校验 | 必填校验、格式校验（非业务规则校验） |
| API 封装 | 封装 fetch/XMLHttpRequest 调用，统一处理请求/响应 |
| 状态管理 | 在页面级别管理 UI 状态（无需全局状态管理框架） |
| 错误展示 | 将后端错误信息友好展示给用户 |
| 多语言/国际化 | 前端文案的国际化（如有需要） |

### 2.3 Frontend 禁止的行为（CANNOT DO）

| 禁止行为 | 原因 |
|---------|------|
| ❌ 直接连接数据库 | 数据层由后端负责，前端只通过 API 通信 |
| ❌ 在 JS 中硬编码业务规则 | 业务规则（权限、库存状态机）由后端统一管理 |
| ❌ 在 JS 中处理权限判断逻辑 | 权限由后端校验，前端只做 UI 隐藏/显示 |
| ❌ 构建完整的数据库操作逻辑 | 前端不持久化数据 |
| ❌ 绕过后端直接修改数据 | 所有写操作必须通过 API |

### 2.4 Frontend 与 Backend 的接口约定

- 所有 API 请求以 `/api/` 开头（由 nginx 代理到后端）
- 请求/响应均为 `application/json`
- 错误响应格式：`{ "error": "message", "code": "ERROR_CODE" }`
- 分页参数：`?page=0&size=20`（Spring Data 分页约定）
- 列表响应：包含 `content`、`totalElements`、`totalPages` 等 Spring Page 标准字段

---

## 3. Backend 子模块

### 3.1 目录结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/backend/
│   │   │   ├── BackendApplication.java    # Spring Boot 入口
│   │   │   ├── controller/               # REST Controller（HTTP 层）
│   │   │   ├── service/                  # 业务逻辑层
│   │   │   ├── repository/               # 数据访问层（JPA Repository）
│   │   │   ├── model/                    # JPA 实体（Module, History）
│   │   │   ├── dto/                      # 数据传输对象
│   │   │   └── config/                   # 配置类（CORS, Security 等）
│   │   └── resources/
│   │       ├── application.properties    # 应用配置
│   │       └── db/migration/            # 数据库迁移脚本（如有）
│   └── test/                            # 单元/集成测试
├── pom.xml                              # Maven 构建配置
└── Dockerfile                           # 容器构建
```

### 3.2 Backend 的职责（CAN DO）

| 职责 | 说明 |
|------|------|
| 业务逻辑 | 光模块状态机、库存计算、兼容性校验等核心业务规则 |
| 数据校验 | 使用 Bean Validation（`@Valid`）校验入参，返回标准错误 |
| 数据持久化 | 通过 JPA Repository 与数据库交互 |
| API 设计 | 设计并实现 RESTful API |
| 权限校验 | 鉴权与授权（Spring Security 或自定义拦截器） |
| 审计日志 | 所有状态变更操作写入 `History` 表 |
| 事务管理 | 使用 `@Transactional` 保证数据一致性 |
| 异常处理 | 全局异常处理（`@ControllerAdvice`），统一错误格式 |

### 3.3 Backend 禁止的行为（CANNOT DO）

| 禁止行为 | 原因 |
|---------|------|
| ❌ 在 Controller 中直接写业务逻辑 | 业务逻辑应在 Service 层，Controller 只做路由 |
| ❌ 在 Service 中直接写 SQL | 数据访问应通过 Repository 层抽象 |
| ❌ 跳过校验直接写入数据库 | 所有写入必须经过 Bean Validation + 业务规则校验 |
| ❌ 返回 JPA 实体对象作为 API 响应 | 使用 DTO 隔离数据库模型和 API 模型 |
| ❌ 硬编码环境配置 | 环境相关配置通过 `application.properties` 或环境变量注入 |

---

## 4. AI 流水线子模块

### 4.1 目录结构（根目录）

```
AI-AutoTuringFlow/
├── main.py                     # Agent/Task/Crew 定义与执行主入口
├── tools/
│   ├── file_tools.py           # 文件操作工具
│   ├── github_tools.py         # GitHub API 交互
│   ├── tool_permission.py      # 角色×模式工具权限矩阵
│   ├── hook_pipeline.py        # Pre/Post 质量检查 Hook
│   └── prompt_router.py        # 智能 Prompt 路由
├── docs/                       # 项目上下文文档（本目录）
├── scripts/                    # 上下文管理脚本
└── .github/workflows/          # CI/CD 工作流
```

### 4.2 AI 流水线的职责

| 职责 | 说明 |
|------|------|
| 需求解析 | 从 GitHub Issue 提取结构化任务信息 |
| 代码生成 | 调用 LLM 生成/修改 frontend 和 backend 代码 |
| 代码审查 | 验证生成代码的一致性和正确性 |
| PR 创建 | 自动创建 Pull Request |
| 上下文管理 | 维护项目上下文文档（`docs/`）的时效性 |

---

## 5. 跨模块协作边界

### 5.1 API 契约（Interface Contract）

- API 契约是 frontend 和 backend 的**唯一边界**
- 新增/修改 API 时，必须先在架构师规划阶段定义 API 契约，再由前后端分别实现
- API 契约变更必须同时更新 frontend 的调用代码和 backend 的接口实现
- 禁止 frontend 直接引用 backend 的 Java 类或数据库表结构

### 5.2 数据格式约定

- 日期时间：ISO 8601 格式（`2024-01-15T10:30:00Z`）
- 枚举值：后端定义标准字符串枚举，前端使用相同字符串
- 分页：遵循 Spring Data Page 格式
- 错误码：后端统一定义，前端按错误码处理
