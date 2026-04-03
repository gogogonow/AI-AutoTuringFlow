# AI-AutoTuringFlow
致敬计算机科学之父图灵，AI驱动的自动化工作流

## 简介
AI-AutoTuringFlow 是一个基于 CrewAI 的多智能体软件工厂。通过在 GitHub Issue 上打标签，可以灵活指定**任务模式**和**影响范围**，系统会动态组装最多 6 个专业 AI Agent，将 Issue 自动转化为代码并创建 Pull Request。

## LLM 接入
本项目通过 [OAIPro](https://api.oaipro.com) OpenAI 兼容网关统一接入大模型，分为三个层级：

| 层级 | 模型 | 负责 Agent |
|------|------|-----------|
| **推理层** | `gpt-3.5-turbo` | 首席系统架构师、UI/UX 交互设计师 |
| **编码层** | `claude-sonnet-4-5` | 高级前端工程师、高级后端工程师、自动化测试工程师 |
| **轻量层** | `gpt-4o-mini` | DevOps 与发布工程师 |

## 环境变量配置
在仓库 **Settings → Secrets and variables → Actions** 中配置以下密钥：

| 变量名 | 用途 |
|--------|------|
| `GH_PAT` | GitHub Personal Access Token，用于仓库操作（读取 Issue、推送代码、创建 PR） |
| `OAIPRO_API_KEY` | OAIPro API Key，用于调用大模型推理服务 |

## 任务模式（mode 标签）

给 Issue 添加以下 **mode 标签**之一，指定工作流的执行策略（默认为 `mode:feature`）：

| 标签 | 模式 | 说明 |
|------|------|------|
| `mode:feature` | 新功能 | 架构师输出完整设计文档；前后端工程师从零生成代码；QA 编写全量测试 |
| `mode:upgrade` | 依赖升级 | 架构师读取现有代码，输出版本差异分析与迁移计划；前后端工程师读取并修改现有文件；QA 补充兼容性回归测试 |
| `mode:bugfix` | Bug 修复 | 架构师读取相关源码，输出根因分析与修复方案；前后端工程师定位并最小化修复；QA 编写 Bug 复现用例 |
| `mode:ui-beautify` | UI 美化 | 架构师读取现有前端代码，输出视觉问题诊断与优化方案；UI 设计师输出详细视觉规范；前端工程师增量美化界面；QA 编写视觉回归测试 |

## 影响范围（scope 标签）

给 Issue 添加以下 **scope 标签**之一，控制哪些工程师 Agent 参与执行（默认为 `scope:fullstack`）：

| 标签 | 范围 | 参与的开发 Agent |
|------|------|-----------------|
| `scope:frontend` | 仅前端 | 高级前端工程师（feature/ui-beautify 模式下还包含 UI/UX 设计师） |
| `scope:backend` | 仅后端 | 高级后端工程师 |
| `scope:fullstack` | 全栈 | 前端工程师 + 后端工程师（feature 模式下还包含 UI/UX 设计师） |

> **注意**：首席系统架构师、自动化测试工程师和 DevOps 工程师在所有模式和范围下均参与执行。
> **注意**：`mode:ui-beautify` 模式会自动强制影响范围为 `scope:frontend`，无需手动设置。

## Agent 动态组装逻辑

```
Issue 触发
    │
    ├─ 首席系统架构师（始终参与）
    │     ├─ feature:      输出架构设计文档
    │     ├─ upgrade:      读取现有代码 → 输出迁移方案
    │     ├─ bugfix:       读取相关源码 → 输出诊断报告
    │     └─ ui-beautify:  读取前端代码 → 输出 UI 优化方案
    │
    ├─ UI/UX 交互设计师（mode:feature + 含前端时参与，或 mode:ui-beautify 时始终参与）
    │     ├─ feature:      设计界面布局与交互流程
    │     └─ ui-beautify:  设计详细视觉升级规范（配色、排版、动效等）
    │
    ├─ 高级前端工程师（含前端范围时参与）
    │
    ├─ 高级后端工程师（含后端范围时参与，ui-beautify 模式不参与）
    │
    ├─ 自动化测试工程师 SDET（始终参与）
    │
    └─ DevOps 与发布工程师（始终参与，创建 PR）
```

## 使用方法

1. 在仓库中创建一个 Issue，详细描述你的需求
2. 给 Issue 添加以下标签：
   - **必须**：`run-ai`（触发工作流）
   - **可选**：`mode:feature` / `mode:upgrade` / `mode:bugfix` / `mode:ui-beautify`（默认 `mode:feature`）
   - **可选**：`scope:frontend` / `scope:backend` / `scope:fullstack`（默认 `scope:fullstack`）
3. GitHub Actions 自动触发，AI 研发团队按顺序执行：**架构分析 → [UI 设计] → [前端开发] → [后端开发] → 测试 → 提交 PR**
4. 工作流完成后，在仓库的 Pull Requests 中查看生成的代码并进行人工 Review

## 使用示例

**场景一：新增全栈功能**
> 标签：`run-ai` + `mode:feature` + `scope:fullstack`
> → 触发 6 个 Agent 全量执行，从零构建前后端代码

**场景二：仅修复后端 Bug**
> 标签：`run-ai` + `mode:bugfix` + `scope:backend`
> → 架构师 + 后端工程师 + QA + DevOps 共 4 个 Agent 参与，精准定位修复

**场景三：升级前端依赖**
> 标签：`run-ai` + `mode:upgrade` + `scope:frontend`
> → 架构师 + 前端工程师 + QA + DevOps 共 4 个 Agent 参与，读取并迁移现有代码

**场景四：美化 UI 界面**
> 标签：`run-ai` + `mode:ui-beautify`
> → 架构师 + UI 设计师 + 前端工程师 + QA + DevOps 共 5 个 Agent 参与，增量优化界面视觉和交互体验（自动强制 scope:frontend）
