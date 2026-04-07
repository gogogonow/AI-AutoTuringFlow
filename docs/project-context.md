# 项目上下文（Project Context）

> **版本**：自动维护，请勿手动编辑关键字段；通过 `scripts/refresh-context.sh` 刷新。
> **用途**：Multi-agent 作业时必须先读取本文件，所有推理须在本上下文范围内进行。

---

## 1. 项目基本信息

| 字段 | 内容 |
|------|------|
| **项目名称** | AI-AutoTuringFlow |
| **业务名称** | 光模块管理系统（Optical Transceiver Management System） |
| **仓库** | gogogonow/AI-AutoTuringFlow |
| **主要语言** | Python（AI 流水线）、Java 21（后端）、HTML/JS/CSS（前端） |
| **部署平台** | Railway（前端 + 后端独立服务） |
| **上下文最后刷新时间** | 2026-04-03T09:30:00Z |

---

## 2. 项目背景与目标

### 业务背景

本项目是一套**光模块（Optical Transceiver）全生命周期管理平台**，面向网络运维团队和采购部门，用于管理光模块的入库、在库状态跟踪、出库、故障记录、兼容性校验等核心业务流程。

### 核心业务目标

1. 支持光模块从**采购入库**到**报废出库**的完整生命周期管理。
2. 提供**库存实时查询**与**多维筛选**（型号、波长、速率、供应商、状态等）。
3. 提供**历史操作记录**与**审计追踪**功能。
4. 支持**批量导入/导出**（Excel/CSV 格式）。
5. 提供**兼容性校验**能力，确保光模块与目标设备端口的匹配性。

### 技术目标

- 通过 **AI 多智能体流水线（CrewAI）** 驱动 GitHub Issue → 自动生成代码 → 创建 PR 的研发循环。
- 通过**固定项目上下文 + 检索增强**机制，确保所有 Agent 在稳定的业务背景下工作，避免跑偏。
- 通过 **Human-in-the-Loop** 审批机制，保障代码质量与方案可控性。

---

## 3. 技术栈概览

### 前端（`frontend/`）

| 项目 | 详情 |
|------|------|
| 类型 | 原生 HTML + CSS + JavaScript（无框架） |
| 服务器 | Nginx（Docker 容器，支持 Railway 动态端口） |
| 入口 | `frontend/index.html` |
| 样式 | `frontend/styles/` |
| 脚本 | `frontend/js/` |
| 构建 | Docker 构建，nginx envsubst 模板注入 `BACKEND_URL` |
| API 调用 | 通过 nginx 反向代理到 `BACKEND_URL`（运行时注入） |

### 后端（`backend/`）

| 项目 | 详情 |
|------|------|
| 框架 | Spring Boot 3.2.0 |
| JDK | Java 21 |
| 持久层 | JPA + H2（开发）/ PostgreSQL（生产） |
| 包名前缀 | `com.example.backend` |
| 入口类 | `com.example.backend.BackendApplication` |
| 主要实体 | `Module`（光模块）、`History`（操作历史） |
| API 风格 | RESTful JSON，`/api/` 前缀 |
| 构建工具 | Maven |

### AI 流水线（根目录）

| 项目 | 详情 |
|------|------|
| 框架 | CrewAI |
| 入口 | `main.py` |
| LLM 路由 | OAIPro OpenAI 兼容网关，所有模型用 `openai/` 前缀 |
| 工具 | `tools/` 目录（file、github、permission、hook、router） |
| 工作流 | `.github/workflows/ai-dev-loop.yml` |

---

## 4. 子模块职责边界

详见 [`docs/module-boundaries.md`](module-boundaries.md)。

---

## 5. 核心业务实体（简要）

详见 [`docs/domain-glossary.md`](domain-glossary.md)。

---

## 6. 架构约束

详见 [`docs/architecture.md`](architecture.md)。

---

## 7. Multi-Agent 协作规则

详见 [`docs/multi-agent-rules.md`](multi-agent-rules.md)。

---

## 8. 当前已知限制与注意事项

- 前端为纯静态页面，**不使用 NPM / 构建工具**，所有 JS 文件直接引入。
- 后端测试套件 `RegressionTestSuite.java` 存在已知编译问题（非公开类访问），暂未修复，不影响主流程。
- 所有 Agent 使用的 LLM 必须通过 `openai/` 前缀 + `base_url=https://api.oaipro.com/v1` 路由，使用 `anthropic/` 前缀会导致工具调用失败。
- Railway 部署时，前端 `BACKEND_URL` 由环境变量注入，nginx 模板在容器启动时展开。

---

## 9. 相关文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| 领域词典 | `docs/domain-glossary.md` | 业务术语、字段名对照、状态枚举 |
| 模块边界 | `docs/module-boundaries.md` | 前后端职责划分、禁止越界说明 |
| 架构约束 | `docs/architecture.md` | 整体架构、编码约定、安全约束 |
| Multi-Agent 规则 | `docs/multi-agent-rules.md` | Agent 分工、输入输出契约、禁止行为 |
| 任务卡模板 | `docs/templates/agent-task-template.md` | 结构化任务卡格式 |
| 上下文快照模板 | `docs/templates/context-snapshot-template.md` | 上下文快照格式 |
| 部署说明 | `DEPLOYMENT.md` | Railway 部署操作手册 |
| 主 README | `README.md` | 系统总览与使用说明 |
