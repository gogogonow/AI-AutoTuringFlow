# 架构与约束（Architecture & Constraints）

> 本文件记录系统架构决策、编码约定与安全约束。
> Multi-agent 在生成代码或提出方案时，必须遵守本文件定义的所有约束。

---

## 1. 系统架构概览

### 1.1 部署架构

```
Internet
    │
    ▼
Railway 前端服务（Nginx 容器）
├── 静态资源服务（HTML/CSS/JS）
└── /api/* 反向代理 → Railway 后端服务（Spring Boot 容器）
                            └── 数据库（H2/PostgreSQL）
```

### 1.2 AI 研发流水线架构

```
GitHub Issue（run-ai 标签触发）
    │
    ▼
GitHub Actions Workflow（ai-dev-loop.yml）
    │
    ├── Job 1: architecture（STAGE=plan）
    │   └── 首席系统架构师 Agent → 产出 architecture-plan.md
    │
    ├── Job 2: human-approval（受保护环境 architecture-review）
    │   └── ⏸️ 人工审批闸门
    │
    └── Job 3: implementation（STAGE=implement）
        ├── [UI/UX 设计师]（仅 feature/ui-beautify）
        ├── [高级前端工程师]（含前端范围）
        ├── [高级后端工程师]（含后端范围，非 ui-beautify）
        └── 代码审查工程师 → 创建 PR
```

---

## 2. 前端架构约束

### 2.1 技术选型约束

| 约束 | 说明 |
|------|------|
| **无框架** | 不使用 React/Vue/Angular，纯原生 HTML+CSS+JS |
| **无构建工具** | 不使用 npm/webpack/vite，文件直接引用 |
| **单页或多页** | 以 `index.html` 为主入口，子页面通过 hash 路由或独立 HTML 文件 |
| **CSS 命名** | 使用 kebab-case，避免行内样式 |
| **JS 模块化** | 使用原生 ES Modules 或 IIFE 封装，避免全局变量污染 |

### 2.2 nginx 配置约束

- 前端 nginx 配置使用 **envsubst 模板**，`nginx.conf` 中只替换 `${BACKEND_URL}`
- 环境变量 `NGINX_ENVSUBST_FILTER=BACKEND_URL` 确保只替换该变量，不影响 nginx 内置变量
- 生产端口通过环境变量 `PORT`（默认 80）动态注入
- 所有 `/api/` 前缀请求代理到 `${BACKEND_URL}`

### 2.3 前端安全约束

- 不在 JS 中存储敏感信息（Token、密码等）
- 不在前端实现权限逻辑（仅做 UI 显隐控制）
- API 调用统一通过封装函数，统一处理错误和认证头

---

## 3. 后端架构约束

### 3.1 分层约束

| 层次 | 类型 | 约束 |
|------|------|------|
| Controller | `@RestController` | 只处理 HTTP 协议层（请求/响应），不写业务逻辑 |
| Service | `@Service` | 业务逻辑层，必须使用接口+实现分离（如有复杂实现） |
| Repository | `@Repository` / JPA | 数据访问，使用 Spring Data JPA，复杂查询用 JPQL |
| Model | `@Entity` | JPA 实体，使用 `jakarta.persistence.*`（Spring Boot 3.x 要求） |
| DTO | POJO | 用于 API 请求/响应，与实体隔离，使用 Bean Validation 注解 |
| Config | `@Configuration` | 配置类，包含 CORS、Security、全局异常处理 |

### 3.2 JPA/数据库约束

- **必须使用 `jakarta.persistence.*`**（不能用 `javax.persistence.*`），Spring Boot 3.x 强制要求
- 实体类不使用 `@Data`（Lombok），手写 getter/setter 避免 JPA 懒加载问题
- 实体主键统一用 `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- 审计字段（创建时间、更新时间）使用 `@CreationTimestamp`/`@UpdateTimestamp`
- 不直接暴露实体类作为 API 响应，必须转换为 DTO

### 3.3 API 设计约束

| 约束 | 说明 |
|------|------|
| 路径前缀 | 所有 API 以 `/api/` 开头 |
| HTTP 方法 | 遵循 REST 语义：GET 查询，POST 创建，PUT 全量更新，PATCH 部分更新，DELETE 删除 |
| 响应格式 | 统一 JSON，Content-Type: application/json |
| 错误格式 | `{"error": "描述", "code": "ERROR_CODE", "details": [...]}` |
| 分页 | 使用 Spring Data Pageable，参数 `page`（从0开始）、`size` |
| CORS | 允许前端域（由配置类统一设置） |
| 版本 | 当前无版本控制，重大变更时在路径加 `/v2/` 前缀 |

### 3.4 安全约束

- 所有写操作（POST/PUT/PATCH/DELETE）必须校验入参（`@Valid`）
- 操作历史（`History`）必须在每次状态变更时自动写入，不能跳过
- 不在代码中硬编码数据库密码或 API Key
- 数据库凭证通过环境变量注入（`SPRING_DATASOURCE_URL` 等）

---

## 4. AI 流水线约束

### 4.1 LLM 接入约束

| 约束 | 说明 |
|------|------|
| **必须用 `openai/` 前缀** | 所有模型通过 OAIPro 兼容网关，用 `openai/xxx` 格式 |
| **设置 `is_litellm=True`** | 绕过 CrewAI 原生 SDK 路由 |
| **base_url** | 必须为 `https://api.oaipro.com/v1` |
| **禁用 Anthropic 直连** | `anthropic/` 前缀会导致工具调用丢失 |
| **token 上限** | 每个 LLM 实例 max_tokens=8192 |

### 4.2 工具使用约束

| 工具 | 约束 |
|------|------|
| `patch_code_tool` | search_string 必须精确匹配（0次=报错，>1次=报错） |
| `execute_command_tool` | 仅白名单命令（pytest/npm test/mvn test 等），禁止 shell 元字符 |
| `write_code_tool` | 只用于创建新文件或完全重写，不用于增量修改 |

### 4.3 代码生成约束

- 架构师产出必须写入 `.ai_architect_plan.md`，下游 Agent 通过 `read_code_tool` 读取
- 架构师必须输出 `## API 契约` 和 `## 文件清单` 章节（hook 会检查）
- Review Agent 发现不一致时必须用 `patch_code_tool` 直接修复，而非仅报告
- 所有 Agent 必须先读取项目上下文（`docs/project-context.md`）再开始任务

---

## 5. 架构决策记录（ADR）

### ADR-001：使用纯原生前端（无框架）

**决定**：前端不引入 React/Vue 等框架。  
**原因**：项目规模适中，引入框架会带来构建工具链复杂性，且不利于 AI Agent 直接生成可运行的静态文件。  
**影响**：前端 Agent 应生成原生 HTML/CSS/JS，不生成 JSX/SFC/TS 等需要编译的格式。

### ADR-002：Railway 动态端口 + nginx envsubst

**决定**：使用 nginx 配置模板 + `NGINX_ENVSUBST_FILTER` 实现后端 URL 动态注入。  
**原因**：Railway 每次部署可能分配不同端口，前端需要运行时获取后端地址。  
**影响**：修改 nginx 配置时必须保持 envsubst 模板机制，不能硬编码后端 URL。

### ADR-003：LLM 全部通过 OAIPro 代理路由

**决定**：不直接调用各 LLM 提供商 API，统一通过 OAIPro OpenAI 兼容网关。  
**原因**：统一密钥管理，降低多 API Key 维护成本。  
**影响**：所有 LLM 实例必须用 `openai/` 前缀，`anthropic/` 前缀会导致工具调用失败。

### ADR-004：Human-in-the-Loop 作为质量闸门

**决定**：架构规划完成后必须经过人类审批才能进入代码生成阶段。  
**原因**：防止错误架构方案导致大量 Token 浪费和错误代码。  
**影响**：CI 中通过 `architecture-review` 受保护环境实现，本地通过 `input()` 交互实现。
