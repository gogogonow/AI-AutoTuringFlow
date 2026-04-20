# AI-AutoTuringFlow

致敬计算机科学之父图灵，AI驱动的自动化工作流

## 简介

AI-AutoTuringFlow 是一个基于 [CrewAI](https://docs.crewai.com/) 的多智能体软件工厂。通过在 GitHub Issue 上打标签，可以灵活指定**任务模式**和**影响范围**，系统会动态组装最多 5 个专业 AI Agent，将 Issue 自动转化为代码并创建 Pull Request。

借鉴 Claude Code 的核心设计理念（Human-in-the-Loop、精确字符串替换编辑器、Read-Evaluate-Execute 闭环、文件级按需上下文管理），系统采用**两阶段执行**架构：规划阶段（Architect）→ **人类审批** → 执行阶段（开发 + 审查），大幅降低跑偏导致的 Token 浪费。

在 GitHub Actions CI 环境中，人类审批通过 **GitHub Actions 受保护环境（protected environment）** 实现——工作流在架构规划完成后自动暂停，等待指定审批人在 GitHub UI 中点击批准后才继续进入实现阶段。

## 架构概览

### GitHub Actions 工作流（CI 模式）

```
GitHub Issue (run-ai 标签触发)
    │
    ▼
┌─────────────────────────────────────────────────────┐
│  🧠 Job 1: architecture（Stage 1）                    │
│  首席系统架构师                                        │
│  STAGE=plan → 架构设计产出                             │
│  → 上传 out/architecture-plan.md 为 artifact          │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
        ⏸️  Job 2: human-approval（Stage 1.5）
        ┌──────────────────────────────────────────┐
        │  environment: architecture-review         │
        │  工作流在此暂停，等待 Required Reviewer     │
        │  在 GitHub Actions UI 中批准               │
        │  Actions 页面 → Review deployments →      │
        │  Approve and deploy                       │
        └──────────────────┬───────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────┐
│  🚀 Job 3: implementation（Stage 2）                  │
│  STAGE=implement → 下载已审批 artifact               │
│  [UI/UX 设计师] → [前端工程师] → [后端工程师] → 审查   │
│  → 直接 Python 调用创建 PR（无需 LLM）                 │
└─────────────────────────────────────────────────────┘
```

### 本地开发模式（不设置 STAGE 环境变量）

```
python main.py
    │
    ├─ 阶段一：plan_crew（架构师规划）
    │
    │  ⏸️  input() 交互式审批拦截点
    │      [Y/Enter] 继续 | [N] 中止 | [其他] 追加修改意见
    │
    └─ 阶段二：execution_crew（开发 + 审查 → 创建 PR）
```

## LLM 接入

本项目通过 [OAIPro](https://api.oaipro.com) OpenAI 兼容网关统一接入大模型，所有模型通过 LiteLLM + `openai/` 前缀路由，分为**两个层级**：

| 层级 | 模型 | 负责 Agent | 说明 |
|------|------|-----------|------|
| **推理层** `llm_reasoning` | `claude-sonnet-4-5-20250929` | 首席系统架构师、UI/UX 设计师、代码审查工程师 | 深度推理与结构化输出 |
| **编码层** `llm_coding` | `claude-sonnet-4-5-20250929` | 高级前端工程师、高级后端工程师 | 高质量代码生成 |

> **注意**：所有 LLM 实例必须设置 `is_litellm=True`，以绕过 CrewAI 的原生 SDK 路由，确保兼容 OAIPro 代理端点。

## 环境变量配置

在仓库 **Settings → Secrets and variables → Actions** 中配置以下密钥：

| 变量名 | 用途 |
|--------|------|
| `GH_PAT` | GitHub Personal Access Token，用于仓库操作（读取 Issue、推送代码、创建 PR） |
| `OAIPRO_API_KEY` | OAIPro API Key，用于调用大模型推理服务 |

> CI 环境中还需设置 `CREWAI_TRACING_ENABLED: "false"` 以防止交互式提示阻塞流水线（已在 `ai-dev-loop.yml` 中配置好）。

## GitHub Actions 人工审批环境配置

工作流使用 `architecture-review` 受保护环境作为审批闸门。使用前需在仓库中创建并配置该环境：

1. 进入仓库页面：**Settings → Environments → New environment**
2. 名称填写：`architecture-review`
3. 勾选 **Required reviewers**，添加需要审批的用户或团队（最多 6 个）
4. 可选：勾选 **Prevent self-review**（禁止触发人自审）
5. 点击 **Save protection rules**

配置完成后，每次工作流运行到 `human-approval` 这个 Job 时，会自动暂停并向 Required Reviewer 发送通知。审批人进入 Actions 运行页面，点击 **Review deployments → Approve and deploy** 即可继续；点击 **Reject** 则终止工作流。

> **注意**：GitHub Free 计划下，required reviewers 功能仅对 **public 仓库**可用。私有仓库需要 GitHub Team 或 Enterprise 计划。

## 任务模式（mode 标签）

给 Issue 添加以下 **mode 标签**之一，指定工作流的执行策略（默认为 `mode:feature`）：

| 标签 | 模式 | 说明 |
|------|------|------|
| `mode:feature` | 新功能 | 架构师输出完整设计文档；前后端工程师从零生成代码和测试 |
| `mode:upgrade` | 依赖升级 | 架构师读取现有代码，输出版本差异分析与迁移计划；前后端工程师使用 `patch_code_tool` 增量修改；运行测试验证 |
| `mode:bugfix` | Bug 修复 | 架构师读取相关源码，输出根因分析与修复方案；前后端工程师定位并最小化修复；运行测试验证 |
| `mode:ui-beautify` | UI 美化 | 架构师读取现有前端代码，输出视觉问题诊断与优化方案；UI 设计师输出详细视觉规范；前端工程师增量美化界面 |

## 影响范围（scope 标签）

给 Issue 添加以下 **scope 标签**之一，控制哪些工程师 Agent 参与执行（默认为 `scope:fullstack`）：

| 标签 | 范围 | 参与的开发 Agent |
|------|------|-----------------|
| `scope:frontend` | 仅前端 | 高级前端工程师（feature/ui-beautify 模式下还包含 UI/UX 设计师） |
| `scope:backend` | 仅后端 | 高级后端工程师 |
| `scope:fullstack` | 全栈 | 前端工程师 + 后端工程师（feature 模式下还包含 UI/UX 设计师） |

> **注意**：首席系统架构师和代码审查工程师在所有模式和范围下均参与执行。
> **注意**：`mode:ui-beautify` 模式会自动强制影响范围为 `scope:frontend`，无需手动设置。

## Agent 流水线（6 角色）

原来的 6 Agent 架构已精简优化，并新增上下文刷新 Agent：
- **QA 测试工程师** → 职责合并到前端/后端工程师（开发和测试同步完成）
- **DevOps 发布工程师** → 去 Agent 化，改为直接 Python 调用（无需 LLM 推理）
- **新增 Review Agent** → 代码审查与一致性校验工程师（替代原 QA 的审查职责 + 新增交叉验证）
- **新增 Context Refresh Agent** → 项目上下文刷新分析师（Agent 驱动的上下文增量刷新）

```
Issue 触发
    │
    ├─ 📚 系统加载固定项目上下文 → 自动注入到所有 Agent 的 backstory
    │     context_loader.get_context_for_role() 按角色裁剪上下文
    │
    ├─ 📐 首席系统架构师（始终参与，plan_crew 阶段，backstory 含完整上下文）
    │     ├─ feature:      输出架构设计文档（API 契约、数据库设计、文件清单）
    │     ├─ upgrade:      读取现有代码 → 输出变更规约（迁移步骤、一致性约束）
    │     ├─ bugfix:       读取相关源码 → 输出诊断报告（根因分析、修复方案）
    │     └─ ui-beautify:  读取前端代码 → 输出 UI 优化方案（设计规范、变更清单）
    │     → 产出写入 .ai_architect_plan.md
    │
    │  ⏸️  Human-in-the-loop 人类审批拦截点
    │
    ├─ 🎨 UI/UX 交互设计师（mode:feature+含前端 或 mode:ui-beautify 时参与）
    │     ├─ feature:      设计界面布局与交互流程
    │     └─ ui-beautify:  设计详细视觉升级规范（配色、排版、动效等）
    │
    ├─ 💻 高级前端工程师（含前端范围时参与，backstory 含前端上下文）
    │     编写代码 + 编写测试 + 运行测试（execute_command_tool）+ 自动修复
    │
    ├─ ⚙️ 高级后端工程师（含后端范围时参与，backstory 含后端上下文）
    │     编写代码 + 编写测试 + 运行测试（execute_command_tool）+ 自动修复
    │
    ├─ 🔍 代码审查与一致性校验工程师（始终参与，backstory 含完整上下文）
    │     验证 Entity↔DTO、前端↔后端 API、DDL↔模型 一致性
    │     发现问题时用 patch_code_tool 直接修复
    │
    └─ 📝 项目上下文刷新分析师（始终参与，execution 后运行）
          分析代码变更中新增的业务实体、API、术语
          使用 patch_code_tool 增量更新 docs/ 下的上下文文档
```

> **CI 两阶段模式**：架构师运行于独立 Job（`STAGE=plan`），产出写入 `out/architecture-plan.md` 并上传为 GitHub Actions artifact；人工审批通过后，执行阶段 Job 读取 artifact（`STAGE=implement`）继续运行。

## 工具链

系统为 Agent 配备了 7 个专业工具，通过**角色 × 模式权限矩阵**（`tools/tool_permission.py`）自动分配：

| 工具 | 功能 | 可用角色 |
|------|------|---------|
| `fetch_requirement_tool` | 从 GitHub Issue 获取需求描述 | 架构师 |
| `write_code_tool` | 创建新文件或完全重写文件 | 前端、后端 |
| `patch_code_tool` | 精确字符串搜索替换（search_string→replace_string），要求匹配恰好一次 | 前端、后端、审查 |
| `read_code_tool` | 读取文件内容（支持 max_lines 截断） | 架构师*、前端*、后端*、审查 |
| `search_code_tool` | 在目录中 grep 搜索代码（正则支持） | 架构师*、前端*、后端*、审查 |
| `list_files_tool` | 递归列出目录结构（最多 3 层） | 架构师*、前端*、后端*、审查 |
| `execute_command_tool` | 沙盒执行测试命令（pytest/npm test/mvn test 等），30 秒超时 | 前端、后端 |

> *标注 `*` 表示仅在 upgrade/bugfix/ui-beautify 模式下可用，feature 模式下不可用。

### patch_code_tool 设计（借鉴 Claude Code `str_replace_editor`）

放弃传统的基于行号或 Unified Diff 的补丁方式，改为精确字符串替换：
- 参数：`file_path`、`search_string`、`replace_string`
- `search_string` 必须与文件内容**完全匹配**（包括缩进和换行）
- **匹配 0 次** → 返回错误，提示用 `read_code_tool` 确认代码片段后重试
- **匹配 >1 次** → 返回错误，提示提供更长、更精确的代码片段
- **匹配恰好 1 次** → 执行替换并写入文件

### execute_command_tool 设计（Read-Evaluate-Execute 闭环）

允许 Agent 执行测试/检查类命令，实现"写代码 → 运行测试 → 根据错误自动修复"的微循环：
- **白名单**：`pytest`、`npm test`、`npm run lint`、`mvn test`、`mvn verify`、`eslint`、`flake8`、`mypy`
- **安全措施**：`shell=False` + `shlex.split()` + shell 元字符检测（`;`, `&&`, `|` 等被拒绝）
- **超时**：30 秒
- **返回**：`exit_code` + `stdout` + `stderr`

## 高级特性

### 智能 Prompt 路由（`tools/prompt_router.py`）

系统会自动分析 Issue 标题和正文中的关键词，推断建议的影响范围和优先参与的 Agent：
- 前端关键词（css、react、组件、布局等）→ 建议 `scope:frontend`
- 后端关键词（api、数据库、spring、entity 等）→ 建议 `scope:backend`
- 路由分析结果会注入到架构师任务描述中作为补充上下文

> 路由建议不覆盖 Issue 标签，标签优先级始终最高。

### Hook 质量检查管线（`tools/hook_pipeline.py`）

在 Crew 执行前后自动进行配置合法性校验和输出质量检查：
- **Pre-flight**：验证架构师输出是否包含必要章节（如 `## API 契约`、`## 文件清单`）
- **Post-flight**：验证开发 Agent 是否实际调用了文件写入工具；Review Agent 是否输出了校验结论
- Hook 失败只打印警告，不中断流程

### 文件级上下文管理

架构师产出写入 `.ai_architect_plan.md` 文件，下游 Agent 通过 `read_code_tool` 按需读取，避免全量输出撑爆上下文窗口或分散注意力。这是借鉴 Claude Code 的 MCP（Model Context Protocol）按需加载理念。

### Human-in-the-Loop 人类审批

系统在架构规划完成后、代码生成开始前设置人类审批拦截点，防止错误方案进入实现阶段。

**CI 模式（GitHub Actions）**：通过 `architecture-review` 受保护环境实现，工作流自动暂停等待人工审批：
- 进入 Actions 运行页面 → 点击 **Review deployments** → **Approve and deploy**（继续）或 **Reject**（终止）
- 审批记录在 GitHub Deployments 中可追溯

**本地开发模式**（不设置 `STAGE` 环境变量）：通过 `input()` 交互式提示实现：
- **`Y` / Enter**：批准方案，继续执行
- **`N`**：中止整个流程
- **任意其他文字**：作为修改意见追加到所有下游任务描述中，Agent 会参考人类反馈调整执行

## 使用方法

1. 在仓库中创建一个 Issue，可直接使用对应 Issue 模板（**推荐**）：
   - [🆕 新功能需求](.github/ISSUE_TEMPLATE/feature.yml)（`mode:feature`）
   - [⬆️ 依赖升级 / 替换](.github/ISSUE_TEMPLATE/upgrade.yml)（`mode:upgrade`）
   - [🐛 Bug 修复](.github/ISSUE_TEMPLATE/bugfix.yml)（`mode:bugfix`）
   - [🎨 UI 美化需求](.github/ISSUE_TEMPLATE/ui-beautify.yml)（`mode:ui-beautify`）
2. 给 Issue 添加以下标签：
   - **必须**：`run-ai`（触发工作流）
   - **可选**：`mode:feature` / `mode:upgrade` / `mode:bugfix` / `mode:ui-beautify`（默认 `mode:feature`）
   - **可选**：`scope:frontend` / `scope:backend` / `scope:fullstack`（默认 `scope:fullstack`，`mode:ui-beautify` 自动强制为 `scope:frontend`）
3. GitHub Actions 自动触发，按以下三个 Job 执行：
   - **Job 1 `architecture`**：架构师规划 → 架构方案上传为 artifact
   - **Job 2 `human-approval`**：⏸️ 工作流暂停，等待审批人在 GitHub Actions UI 中审批（需提前配置 `architecture-review` environment）
   - **Job 3 `implementation`**：[UI 设计] → [前端开发+测试] → [后端开发+测试] → 代码审查 → 创建 PR
4. 工作流完成后，在仓库的 Pull Requests 中查看生成的代码并进行人工 Review

> **首次使用前**请确保已在 **Settings → Environments** 中创建 `architecture-review` 环境并配置 Required Reviewers，详见[环境配置章节](#github-actions-人工审批环境配置)。

## 使用示例

**场景一：新增全栈功能**
> 标签：`run-ai` + `mode:feature` + `scope:fullstack`
> → 架构师 + UI 设计师 + 前端工程师 + 后端工程师 + 审查工程师，共 5 个 Agent 全量执行

**场景二：仅修复后端 Bug**
> 标签：`run-ai` + `mode:bugfix` + `scope:backend`
> → 架构师 + 后端工程师 + 审查工程师，共 3 个 Agent 参与，精准定位修复

**场景三：升级前端依赖**
> 标签：`run-ai` + `mode:upgrade` + `scope:frontend`
> → 架构师 + 前端工程师 + 审查工程师，共 3 个 Agent 参与，增量迁移 + 测试验证

**场景四：美化 UI 界面**
> 标签：`run-ai` + `mode:ui-beautify`
> → 架构师 + UI 设计师 + 前端工程师 + 审查工程师，共 4 个 Agent 参与（自动强制 scope:frontend）

## 项目上下文治理（Multi-Agent Context Governance）

为确保 multi-agent 作业始终基于稳定、准确的项目背景，本仓库实施了一套**固定项目上下文 + Agent 驱动增量刷新**机制。

### 上下文如何关联到 Agent

上下文与 Agent 的关联通过 `tools/context_loader.py` 模块实现，**不是**依赖 Agent 主动读取文件，而是在 Agent 创建时**自动注入**：

```
main.py 启动
    │
    ├── context_loader.get_context_for_role("architect")  → 注入到架构师 backstory
    ├── context_loader.get_context_for_role("frontend_dev") → 注入到前端工程师 backstory
    ├── context_loader.get_context_for_role("backend_dev")  → 注入到后端工程师 backstory
    ├── context_loader.get_context_for_role("reviewer")    → 注入到审查工程师 backstory
    └── context_loader.get_context_for_role("ui_designer") → 注入到 UI 设计师 backstory
```

- **架构师 / 审查工程师**：获取全部上下文（项目背景 + 领域词典 + 模块边界 + 架构约束 + 协作规则）
- **前端工程师 / UI 设计师**：获取项目背景 + 领域词典 + 模块边界 + 架构约束
- **后端工程师**：获取项目背景 + 领域词典 + 模块边界 + 架构约束

每个 Task 的 description 中还包含上下文前置指令，提醒 Agent 必须在固定上下文范围内推理。

### 上下文文档结构

所有上下文文档存放在 `docs/` 目录下：

| 文档 | 路径 | 说明 |
|------|------|------|
| 项目上下文 | `docs/project-context.md` | 项目背景、目标、技术栈、入口文档 |
| 领域词典 | `docs/domain-glossary.md` | 业务术语、字段名对照、枚举定义（防止术语歧义） |
| 模块边界 | `docs/module-boundaries.md` | frontend/backend 职责划分与禁止越界说明 |
| 架构约束 | `docs/architecture.md` | 架构决策、编码约定、安全约束 |
| Multi-Agent 规则 | `docs/multi-agent-rules.md` | Agent 分工、执行流程、禁止行为清单 |
| 任务卡模板 | `docs/templates/agent-task-template.md` | 结构化任务卡格式（减少需求歧义） |
| 上下文快照模板 | `docs/templates/context-snapshot-template.md` | 上下文快照格式参考 |
| 历史快照 | `docs/snapshots/` | 每次刷新时自动归档的上下文快照 |

### Multi-Agent 使用约束

1. **上下文已自动注入**：Agent 的 backstory 中已包含固定项目上下文，无需每次手动读取 docs/ 文件。
2. **上下文优先**：所有推理必须基于注入的上下文，不得凭空假设业务规则或 API 格式。
3. **边界遵守**：Frontend Agent 只修改 `frontend/`，Backend Agent 只修改 `backend/`。
4. **不臆测**：当上下文不足时，先通过 `search_code_tool`/`read_code_tool` 补充，仍不足则明确说明需要哪些信息。
5. **自动刷新**：每次 Crew 执行后，Context Refresh Agent 会自动分析代码变更并更新上下文文档。

详细规则见 [`docs/multi-agent-rules.md`](docs/multi-agent-rules.md)。

### 上下文初始化与刷新

**首次初始化**（新项目或首次部署上下文机制时）：
```bash
chmod +x scripts/init-context.sh
./scripts/init-context.sh
```

**手动增量刷新**（需要手动触发时）：
```bash
chmod +x scripts/refresh-context.sh
./scripts/refresh-context.sh --reason "新增光模块兼容性检查功能" --pr 42
```

**自动增量刷新**（每次 Crew 执行后自动触发）：
- 由 `main.py` 中的**项目上下文刷新分析师 Agent** 自动执行
- 语义分析代码变更中的新术语、新 API、新实体
- 使用 `patch_code_tool` 增量更新 `docs/` 下的上下文文档
- 与纯 bash 脚本不同，Agent 能提取代码中的**业务语义**信息

> 也可通过 GitHub Issue 使用「🔄 上下文刷新请求」模板触发刷新流程。

---

## 项目结构

```
AI-AutoTuringFlow/
├── main.py                      # 多智能体主流程（Agent/Task/Crew 定义与执行）
├── tools/
│   ├── file_tools.py            # 文件操作工具（read/write/patch/search/list/execute）
│   ├── github_tools.py          # GitHub 交互（Issue 读取、PR 创建）
│   ├── context_loader.py        # 🆕 项目上下文加载器（读取 docs/ → 注入 Agent backstory）
│   ├── tool_permission.py       # 角色 × 模式 工具权限矩阵
│   ├── hook_pipeline.py         # Pre/Post-task 质量检查 Hook
│   └── prompt_router.py         # 智能 Prompt 路由（关键词分析）
├── docs/                        # 🆕 项目上下文治理文档
│   ├── project-context.md       #     项目背景与技术栈（Agent 必读入口）
│   ├── domain-glossary.md       #     领域词典与业务术语
│   ├── module-boundaries.md     #     前后端职责边界
│   ├── architecture.md          #     架构约束与决策记录
│   ├── multi-agent-rules.md     #     Multi-Agent 协作规则
│   ├── templates/               #     可复用模板
│   │   ├── agent-task-template.md       # 任务卡模板
│   │   └── context-snapshot-template.md # 上下文快照模板
│   └── snapshots/               #     历史上下文快照（自动归档）
├── scripts/                     # 🆕 上下文管理脚本
│   ├── init-context.sh          #     首次初始化上下文文档
│   └── refresh-context.sh       #     增量刷新上下文与快照
├── frontend/                    # 前端代码（Nginx + 静态资源）
├── backend/                     # 后端代码（Spring Boot / Java 21）
├── tests/                       # 自动化测试
├── .github/
│   ├── workflows/
│   │   ├── ai-dev-loop.yml      # AI 研发循环工作流（3 Job：architecture → human-approval → implementation）
│   │   └── deploy.yml           # Railway 部署工作流
│   └── ISSUE_TEMPLATE/
│       ├── feature.yml          # 新功能需求模板
│       ├── bugfix.yml           # Bug 修复模板
│       ├── upgrade.yml          # 依赖升级模板
│       ├── ui-beautify.yml      # 🆕 UI 美化需求模板
│       └── context-refresh.yml  # 上下文刷新请求模板
├── requirements.txt             # Python 依赖
└── README.md                    # 本文档
```
