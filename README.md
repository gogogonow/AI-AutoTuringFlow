# AI-AutoTuringFlow
致敬计算机科学之父图灵，AI驱动的自动化工作流

## 简介
AI-AutoTuringFlow 是一个基于 CrewAI 的多智能体软件工厂，通过 6 个专业 AI Agent 协作，将 GitHub Issue 自动转化为代码并创建 Pull Request。

## LLM 接入
本项目通过 [OpenRouter](https://openrouter.ai/) 统一网关接入大模型：
- **推理模型**: `deepseek/deepseek-r1` — 用于架构设计、UI 规划等需要深度思考的任务
- **编码模型**: `openai/gpt-4o` — 用于前后端代码生成、测试编写等编码任务

## 环境变量配置
在仓库 **Settings → Secrets and variables → Actions** 中配置以下密钥：

| 变量名 | 用途 |
|--------|------|
| `GH_PAT` | GitHub Personal Access Token，用于仓库操作（读取 Issue、推送代码、创建 PR） |
| `OPENROUTER_API_KEY` | OpenRouter API Key，用于调用大模型推理服务 |

## 使用方法
1. 在仓库中创建一个 Issue，描述你的需求
2. 给 Issue 添加 `run-ai` 标签
3. GitHub Actions 自动触发 6 人 AI 研发团队，依次执行架构设计 → UI 设计 → 前端开发 → 后端开发 → 测试 → 提交 PR
