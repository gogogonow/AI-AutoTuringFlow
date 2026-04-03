# AI-AutoTuringFlow

致敬计算机科学之父图灵，AI驱动的自动化工作流

## 简介

AI-AutoTuringFlow 是一个基于 [CrewAI](https://docs.crewai.com/) 的多智能体软件工厂。通过在 GitHub Issue 上打标签，可以灵活指定**任务模式**和**影响范围**，系统会动态组装最多 5 个专业 AI Agent，将 Issue 自动转化为代码并创建 Pull Request。

借鉴 Claude Code 的核心设计理念（Human-in-the-Loop、精确字符串替换编辑器、Read-Evaluate-Execute 闭环、文件级按需上下文管理），系统采用**两阶段执行**架构：规划阶段（Architect）→ **人类审批** → 执行阶段（开发 + 审查），大幅降低跑偏导致的 Token 浪费。

## 架构概览

```
GitHub Issue (run-ai 标签触发)
    │
    ▼
┌─────────────────────────────────────────────────────┐
│  🧠 阶段一：规划 (plan_crew)                          │
│  首席系统架构师                                        │
│  → 产出写入 .ai_architect_plan.md                     │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
        👨‍💻 Human-in-the-Loop
        ┌──────────────────────┐
        │ [Y/Enter] → 继续     │
        │ [N]       → 中止     │
        │ [其他]    → 追加意见  │
        └──────────┬───────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  🚀 阶段二：执行 (execution_crew)                     │
│  [UI/UX 设计师] → [前端工程师] → [后端工程师] → 审查   │
│                                                       │
│  各 Agent 通过 read_code_tool 按需读取                 │
│  .ai_architect_plan.md 获取上下文                      │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
        📦 DevOps 去 Agent 化
        直接 Python 调用创建 PR（无需 LLM）
```

## LLM 接入

本项目通过 [OAIPro](https://api.oaipro.com) OpenAI 兼容网关统一接入大模型，所有模型通过 LiteLLM + `openai/` 前缀路由，分为**两个层级**：

| 层级 | 模型 | 负责 Agent | 说明 |
|------|------|-----------|------|
| **推理层** `llm_reasoning` | `chatgpt-4o-latest` | 首席系统架构师、UI/UX 设计师、代码审查工程师 | 深度推理与结构化输出 |
| **编码层** `llm_coding` | `claude-sonnet-4-5` | 高级前端工程师、高级后端工程师 | 高质量代码生成 |

> **注意**：所有 LLM 实例必须设置 `is_litellm=True`，以绕过 CrewAI 的原生 SDK 路由，确保兼容 OAIPro 代理端点。

## 环境变量配置

在仓库 **Settings → Secrets and variables → Actions** 中配置以下密钥：

| 变量名 | 用途 |
|--------|------|
| `GH_PAT` | GitHub Personal Access Token，用于仓库操作（读取 Issue、推送代码、创建 PR） |
| `OAIPRO_API_KEY` | OAIPro API Key，用于调用大模型推理服务 |

> CI 环境中还需设置 `CREWAI_TRACING_ENABLED: "false"` 以防止交互式提示阻塞流水线。

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

## Agent 流水线（5 角色）

原来的 6 Agent 架构已精简优化：
- **QA 测试工程师** → 职责合并到前端/后端工程师（开发和测试同步完成）
- **DevOps 发布工程师** → 去 Agent 化，改为直接 Python 调用（无需 LLM 推理）
- **新增 Review Agent** → 代码审查与一致性校验工程师（替代原 QA 的审查职责 + 新增交叉验证）

```
Issue 触发
    │
    ├─ 📐 首席系统架构师（始终参与，plan_crew 阶段）
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
    ├─ 💻 高级前端工程师（含前端范围时参与）
    │     编写代码 + 编写测试 + 运行测试（execute_command_tool）+ 自动修复
    │
    ├─ ⚙️ 高级后端工程师（含后端范围时参与，ui-beautify 模式不参与）
    │     编写代码 + 编写测试 + 运行测试（execute_command_tool）+ 自动修复
    │
    └─ 🔍 代码审查与一致性校验工程师（始终参与）
          验证 Entity↔DTO、前端↔后端 API、DDL↔模型 一致性
          发现问题时用 patch_code_tool 直接修复
```

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

在架构师规划完成后、代码生成开始前，系统自动暂停并等待人类审批：
- **`Y` / Enter**：批准方案，继续执行
- **`N`**：中止整个流程
- **任意其他文字**：作为修改意见追加到所有下游任务描述中，Agent 会参考人类反馈调整执行

> 在 CI 环境中，此拦截点会被 `CREWAI_TRACING_ENABLED: "false"` 配置绕过。

## 使用方法

1. 在仓库中创建一个 Issue，详细描述你的需求
2. 给 Issue 添加以下标签：
   - **必须**：`run-ai`（触发工作流）
   - **可选**：`mode:feature` / `mode:upgrade` / `mode:bugfix` / `mode:ui-beautify`（默认 `mode:feature`）
   - **可选**：`scope:frontend` / `scope:backend` / `scope:fullstack`（默认 `scope:fullstack`）
3. GitHub Actions 自动触发，AI 研发团队按两阶段执行：
   - **阶段一**：架构师规划 → 产出写入 `.ai_architect_plan.md`
   - **人类审批**（CI 中自动跳过）
   - **阶段二**：[UI 设计] → [前端开发+测试] → [后端开发+测试] → 代码审查 → 创建 PR
4. 工作流完成后，在仓库的 Pull Requests 中查看生成的代码并进行人工 Review

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

## 项目结构

```
AI-AutoTuringFlow/
├── main.py                      # 多智能体主流程（Agent/Task/Crew 定义与执行）
├── tools/
│   ├── file_tools.py            # 文件操作工具（read/write/patch/search/list/execute）
│   ├── github_tools.py          # GitHub 交互（Issue 读取、PR 创建）
│   ├── tool_permission.py       # 角色 × 模式 工具权限矩阵
│   ├── hook_pipeline.py         # Pre/Post-task 质量检查 Hook
│   └── prompt_router.py         # 智能 Prompt 路由（关键词分析）
├── frontend/                    # 前端代码（Nginx + 静态资源）
├── backend/                     # 后端代码（Spring Boot / Java 21）
├── tests/                       # 自动化测试
├── .github/workflows/
│   ├── ai-dev-loop.yml          # AI 研发循环工作流
│   └── deploy.yml               # Railway 部署工作流
├── requirements.txt             # Python 依赖
└── README.md                    # 本文档
```
