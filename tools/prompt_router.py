"""Prompt 路由模块 — 基于 Issue 内容的智能分析，增强现有标签系统。

提供 analyze_issue() 和 enhance_task_description()，
通过关键词匹配为 Agent 补充上下文，但不覆盖 Issue 标签的优先级。
"""

import re

# ---------------------------------------------------------------------------
# 关键词字典：关键词 → (suggested_scope, category_label)
# ---------------------------------------------------------------------------
# 前端相关关键词
_FRONTEND_KEYWORDS: list[str] = [
    "css", "样式", "按钮", "ui", "界面", "前端", "html", "react", "vue",
    "angular", "javascript", "typescript", "组件", "布局", "动效", "动画",
    "响应式", "页面", "视觉", "颜色", "配色", "字体", "图标", "svg",
    "webpack", "vite", "npm", "yarn", "scss", "less",
]

# 后端相关关键词
_BACKEND_KEYWORDS: list[str] = [
    "api", "数据库", "sql", "后端", "接口", "服务", "java", "spring",
    "controller", "service", "repository", "entity", "dto", "rest",
    "restful", "http", "endpoint", "mysql", "postgresql", "redis",
    "缓存", "队列", "消息", "kafka", "rabbitmq", "docker", "k8s",
    "kubernetes", "maven", "gradle", "pom", "dependency", "依赖",
    "迁移", "migration", "ddl", "orm", "jpa", "hibernate",
]

# 全栈相关标志词（同时包含前端和后端关键词时升级为 fullstack）
_FULLSTACK_TRIGGER_KEYWORDS: list[str] = [
    "全栈", "fullstack", "full-stack", "端到端", "前后端",
]

# 各角色对应的优先关键词
_ROLE_PRIORITY_KEYWORDS: dict[str, list[str]] = {
    "architect": ["架构", "设计", "系统", "方案", "规划", "接口设计", "api设计"],
    "ui_designer": ["ui", "ux", "界面", "视觉", "设计稿", "原型", "配色", "动效"],
    "frontend_dev": ["前端", "css", "样式", "组件", "页面", "react", "vue"],
    "backend_dev": ["后端", "api", "数据库", "sql", "java", "spring", "接口"],
    "reviewer": ["review", "审查", "校验", "一致性", "检查"],
}


def _normalize(text: str) -> str:
    """将文本转为小写并合并连续空白，便于关键词匹配。"""
    return re.sub(r"\s+", " ", text.lower().strip())


def analyze_issue(title: str, body: str) -> dict:
    """分析 Issue 标题和正文，返回路由分析结果。

    Args:
        title: Issue 标题
        body: Issue 正文（可以为空字符串）

    Returns:
        dict，包含以下字段：
          - suggested_scope (str): "frontend" / "backend" / "fullstack"
          - detected_keywords (list[str]): 检测到的关键词列表
          - priority_agents (list[str]): 建议优先参与的 Agent 角色列表
          - analysis_summary (str): 人类可读的分析摘要
    """
    combined = _normalize(f"{title} {body or ''}")

    detected_frontend: list[str] = [kw for kw in _FRONTEND_KEYWORDS if kw in combined]
    detected_backend: list[str] = [kw for kw in _BACKEND_KEYWORDS if kw in combined]
    detected_fullstack: list[str] = [kw for kw in _FULLSTACK_TRIGGER_KEYWORDS if kw in combined]

    all_detected = list(dict.fromkeys(detected_frontend + detected_backend + detected_fullstack))

    # 推断 scope
    has_frontend = bool(detected_frontend)
    has_backend = bool(detected_backend)
    has_fullstack_trigger = bool(detected_fullstack)

    if has_fullstack_trigger or (has_frontend and has_backend):
        suggested_scope = "fullstack"
    elif has_frontend:
        suggested_scope = "frontend"
    elif has_backend:
        suggested_scope = "backend"
    else:
        suggested_scope = "fullstack"  # 无明显倾向时，默认全栈（与现有逻辑保持一致）

    # 推断优先 Agent
    priority_agents: list[str] = []
    for role, kws in _ROLE_PRIORITY_KEYWORDS.items():
        if any(kw in combined for kw in kws):
            priority_agents.append(role)
    # 保证架构师始终在列表首位
    if "architect" not in priority_agents:
        priority_agents.insert(0, "architect")
    elif priority_agents[0] != "architect":
        priority_agents.remove("architect")
        priority_agents.insert(0, "architect")

    # 生成摘要
    parts: list[str] = []
    if detected_frontend:
        parts.append(f"前端关键词: {', '.join(detected_frontend[:5])}")
    if detected_backend:
        parts.append(f"后端关键词: {', '.join(detected_backend[:5])}")
    if detected_fullstack:
        parts.append(f"全栈触发词: {', '.join(detected_fullstack)}")
    if not all_detected:
        parts.append("未检测到明显领域关键词，默认全栈模式")

    summary = (
        f"建议范围: {suggested_scope} | "
        + " | ".join(parts)
        + f" | 优先 Agent: {', '.join(priority_agents)}"
    )

    return {
        "suggested_scope": suggested_scope,
        "detected_keywords": all_detected,
        "priority_agents": priority_agents,
        "analysis_summary": summary,
    }


def enhance_task_description(original_desc: str, analysis: dict) -> str:
    """将路由分析结果注入到 Task 描述中，为 Agent 提供更多上下文。

    在原始描述前插入 `[智能路由分析]` 段落。

    Args:
        original_desc: 原始 Task 描述文字
        analysis: analyze_issue() 返回的分析结果 dict

    Returns:
        str: 增强后的 Task 描述
    """
    keywords_str = (
        "、".join(analysis.get("detected_keywords", [])[:10]) or "（未检测到特定关键词）"
    )
    agents_str = "、".join(analysis.get("priority_agents", []))
    scope_str = analysis.get("suggested_scope", "fullstack")

    routing_block = (
        "[智能路由分析]\n"
        f"- 建议影响范围: {scope_str}\n"
        f"- 检测到的关键词: {keywords_str}\n"
        f"- 建议优先参与的 Agent: {agents_str}\n"
        f"- 分析摘要: {analysis.get('analysis_summary', '')}\n"
        "\n"
    )

    return routing_block + original_desc
