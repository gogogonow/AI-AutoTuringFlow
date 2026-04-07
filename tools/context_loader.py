"""项目上下文加载器 — 在 Agent 启动前加载固定项目上下文。

本模块负责：
1. 读取 docs/ 下的上下文文档，构建结构化的项目背景字符串
2. 按 Agent 角色裁剪上下文（架构师看全局，前端只看前端相关，后端只看后端相关）
3. 为上下文刷新 Agent 提供代码变更摘要提取能力

使用方式：
    from tools.context_loader import load_project_context, get_context_for_role

    # 加载全部上下文
    full_ctx = load_project_context()

    # 按角色获取裁剪后的上下文
    architect_ctx = get_context_for_role("architect")
    frontend_ctx = get_context_for_role("frontend_dev")
"""

import os
import pathlib
import subprocess

# 仓库根目录（相对于本文件位置推算）
_REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent
_DOCS_DIR = _REPO_ROOT / "docs"

# 上下文文档路径映射
_CONTEXT_FILES = {
    "project_context": _DOCS_DIR / "project-context.md",
    "domain_glossary": _DOCS_DIR / "domain-glossary.md",
    "module_boundaries": _DOCS_DIR / "module-boundaries.md",
    "architecture": _DOCS_DIR / "architecture.md",
    "multi_agent_rules": _DOCS_DIR / "multi-agent-rules.md",
    "frontend_component_spec": _DOCS_DIR / "frontend-component-spec.md",
}

# 每个文件的最大读取字符数（防止过大文件撑爆 token）
_MAX_CHARS_PER_FILE = 6000

# 上下文刷新时的文档读取限制（刷新 prompt 中需要同时放入多个文档，
# 因此每个文档的限制较小，避免总 prompt 超出 token 上限）
_MAX_CHARS_REFRESH_CONTEXT = 3000
_MAX_CHARS_REFRESH_GLOSSARY = 4000


def _safe_read(filepath: pathlib.Path, max_chars: int = _MAX_CHARS_PER_FILE) -> str:
    """安全读取文件，不存在时返回空字符串。"""
    if not filepath.is_file():
        return ""
    try:
        text = filepath.read_text(encoding="utf-8")
        if len(text) > max_chars:
            text = text[:max_chars] + f"\n\n... (truncated at {max_chars} chars)"
        return text
    except Exception:
        return ""


def load_project_context() -> dict[str, str]:
    """加载所有上下文文档，返回 {文档名: 内容} 字典。

    Returns:
        dict: key 为文档标识（如 'project_context'），value 为文件内容字符串
    """
    result = {}
    for key, filepath in _CONTEXT_FILES.items():
        content = _safe_read(filepath)
        if content:
            result[key] = content
    return result


def _build_context_block(label: str, content: str) -> str:
    """将单个上下文文档格式化为可注入的文本块。"""
    return f"<{label}>\n{content}\n</{label}>"


def get_context_for_role(role: str) -> str:
    """按 Agent 角色返回裁剪后的上下文字符串。

    - architect / reviewer: 获取全部上下文（需要全局视野）
    - frontend_dev / ui_designer: 获取项目背景 + 领域词典 + 模块边界（前端部分）+ 架构约束
    - backend_dev: 获取项目背景 + 领域词典 + 模块边界（后端部分）+ 架构约束

    Args:
        role: Agent 角色名称

    Returns:
        str: 格式化的上下文字符串，可直接拼接到 backstory 或 task description 中
    """
    ctx = load_project_context()
    if not ctx:
        return "(项目上下文文档尚未初始化，请先运行 scripts/init-context.sh)"

    blocks: list[str] = []

    # 所有角色都需要项目背景和领域词典
    if "project_context" in ctx:
        blocks.append(_build_context_block("项目背景", ctx["project_context"]))
    if "domain_glossary" in ctx:
        blocks.append(_build_context_block("领域词典", ctx["domain_glossary"]))

    # 按角色裁剪
    if role in ("architect", "reviewer"):
        # 全局视野：所有文档
        if "module_boundaries" in ctx:
            blocks.append(_build_context_block("模块边界", ctx["module_boundaries"]))
        if "architecture" in ctx:
            blocks.append(_build_context_block("架构约束", ctx["architecture"]))
        if "multi_agent_rules" in ctx:
            blocks.append(_build_context_block("协作规则", ctx["multi_agent_rules"]))
        if "frontend_component_spec" in ctx:
            blocks.append(_build_context_block("前端组件规范", ctx["frontend_component_spec"]))
    elif role in ("frontend_dev", "ui_designer"):
        # 前端视角：模块边界 + 架构约束 + 前端组件规范（防止重复实现）
        if "module_boundaries" in ctx:
            blocks.append(_build_context_block("模块边界", ctx["module_boundaries"]))
        if "architecture" in ctx:
            blocks.append(_build_context_block("架构约束", ctx["architecture"]))
        if "frontend_component_spec" in ctx:
            blocks.append(_build_context_block("前端组件规范", ctx["frontend_component_spec"]))
    elif role == "backend_dev":
        # 后端视角：只需要模块边界和架构约束
        if "module_boundaries" in ctx:
            blocks.append(_build_context_block("模块边界", ctx["module_boundaries"]))
        if "architecture" in ctx:
            blocks.append(_build_context_block("架构约束", ctx["architecture"]))

    if not blocks:
        return "(上下文文档为空，请检查 docs/ 目录是否包含必要文件)"

    header = (
        "[固定项目上下文 — 以下内容为项目的固定背景，你的所有推理和输出必须在此上下文范围内进行，"
        "不得脱离项目背景自行假设或臆测。]\n\n"
    )
    return header + "\n\n".join(blocks)


def get_context_summary() -> str:
    """返回一段简短的上下文摘要，用于注入到简短的 prompt 中。

    只包含项目名称、业务名称、技术栈、核心实体等关键信息。
    """
    ctx_file = _CONTEXT_FILES.get("project_context")
    if ctx_file is None or not ctx_file.is_file():
        return "(项目上下文未初始化)"

    content = _safe_read(ctx_file, max_chars=2000)
    # 提取前两个章节（项目基本信息 + 项目背景与目标）作为摘要
    lines = content.split("\n")
    summary_lines = []
    section_count = 0
    for line in lines:
        if line.startswith("## "):
            section_count += 1
            if section_count > 2:
                break
        summary_lines.append(line)

    return "\n".join(summary_lines)


# ================================================================
# 代码变更分析（供 Agent 驱动的上下文刷新使用）
# ================================================================

def extract_code_change_summary(since_ref: str = "HEAD~1") -> str:
    """提取最近的代码变更摘要，供上下文刷新 Agent 分析。

    与 bash 脚本不同，此函数：
    - 提取变更的文件名和 diff 摘要
    - 对 Java 实体/Controller 文件，提取变更的字段和方法签名
    - 对前端文件，提取新增的页面元素和 API 调用
    - 返回结构化的变更描述，而非原始 diff

    Args:
        since_ref: git ref，默认 HEAD~1

    Returns:
        str: 结构化的变更摘要
    """
    try:
        # 检查是否在 git 仓库中
        subprocess.run(
            ["git", "rev-parse", "HEAD"],
            capture_output=True, check=True, cwd=str(_REPO_ROOT),
        )
    except (subprocess.CalledProcessError, FileNotFoundError):
        return "(无法获取 git 变更信息)"

    # 验证 since_ref 是否有效
    try:
        subprocess.run(
            ["git", "rev-parse", since_ref],
            capture_output=True, check=True, cwd=str(_REPO_ROOT),
        )
    except subprocess.CalledProcessError:
        since_ref = "HEAD"

    # 获取变更的文件列表
    try:
        diff_result = subprocess.run(
            ["git", "diff", "--name-status", since_ref, "HEAD"],
            capture_output=True, text=True, cwd=str(_REPO_ROOT),
            timeout=10,
        )
        changed_files = diff_result.stdout.strip()
    except (subprocess.TimeoutExpired, Exception):
        return "(获取变更文件列表失败)"

    if not changed_files:
        return "(没有检测到文件变更)"

    sections: list[str] = ["## 代码变更摘要\n"]
    sections.append(f"### 变更的文件（自 {since_ref}）\n")
    sections.append("```")
    sections.append(changed_files)
    sections.append("```\n")

    # 分类分析变更
    backend_entities = []
    backend_controllers = []
    frontend_changes = []
    config_changes = []

    for line in changed_files.split("\n"):
        parts = line.split("\t")
        if len(parts) < 2:
            continue
        status, filepath = parts[0], parts[-1]

        if "model/" in filepath and filepath.endswith(".java"):
            backend_entities.append((status, filepath))
        elif "controller/" in filepath and filepath.endswith(".java"):
            backend_controllers.append((status, filepath))
        elif filepath.startswith("frontend/") and filepath.endswith((".html", ".js", ".css")):
            frontend_changes.append((status, filepath))
        elif filepath in ("pom.xml", "requirements.txt", "package.json"):
            config_changes.append((status, filepath))

    if backend_entities:
        sections.append("### 后端实体变更（可能需要更新 domain-glossary.md）\n")
        for status, fp in backend_entities:
            sections.append(f"- [{status}] `{fp}`")
            # 提取实体字段变更摘要
            field_diff = _extract_java_field_changes(fp, since_ref)
            if field_diff:
                sections.append(f"  变更字段: {field_diff}")
        sections.append("")

    if backend_controllers:
        sections.append("### 后端 API 变更（可能需要更新 API 清单）\n")
        for status, fp in backend_controllers:
            sections.append(f"- [{status}] `{fp}`")
            api_diff = _extract_api_changes(fp, since_ref)
            if api_diff:
                sections.append(f"  API 变更: {api_diff}")
        sections.append("")

    if frontend_changes:
        sections.append("### 前端页面变更\n")
        for status, fp in frontend_changes:
            sections.append(f"- [{status}] `{fp}`")
        sections.append("")

    if config_changes:
        sections.append("### 配置/依赖变更\n")
        for status, fp in config_changes:
            sections.append(f"- [{status}] `{fp}`")
        sections.append("")

    return "\n".join(sections)


def _extract_java_field_changes(filepath: str, since_ref: str) -> str:
    """从 git diff 中提取 Java 实体字段的增减。"""
    try:
        result = subprocess.run(
            ["git", "diff", since_ref, "HEAD", "--", filepath],
            capture_output=True, text=True, cwd=str(_REPO_ROOT),
            timeout=10,
        )
        diff_text = result.stdout
    except Exception:
        return ""

    added_fields = []
    removed_fields = []
    for line in diff_text.split("\n"):
        stripped = line.strip()
        # Match Java field declarations like: private String fieldName;
        if stripped.startswith("+") and "private " in stripped and ";" in stripped:
            field = stripped.lstrip("+").strip()
            if not field.startswith("//"):
                added_fields.append(field)
        elif stripped.startswith("-") and "private " in stripped and ";" in stripped:
            field = stripped.lstrip("-").strip()
            if not field.startswith("//"):
                removed_fields.append(field)

    parts = []
    if added_fields:
        parts.append(f"新增: {', '.join(f[:60] for f in added_fields[:5])}")
    if removed_fields:
        parts.append(f"删除: {', '.join(f[:60] for f in removed_fields[:5])}")
    return "; ".join(parts)


def _extract_api_changes(filepath: str, since_ref: str) -> str:
    """从 git diff 中提取 Controller API 路由的增减。"""
    try:
        result = subprocess.run(
            ["git", "diff", since_ref, "HEAD", "--", filepath],
            capture_output=True, text=True, cwd=str(_REPO_ROOT),
            timeout=10,
        )
        diff_text = result.stdout
    except Exception:
        return ""

    import re
    added_apis = []
    removed_apis = []
    # Match Spring Web annotations: @GetMapping("/path"), @PostMapping('/path'), etc.
    mapping_pattern = re.compile(r'@(Get|Post|Put|Delete|Patch)Mapping\(["\']([^"\']+)["\']\)')
    for line in diff_text.split("\n"):
        match = mapping_pattern.search(line)
        if match:
            method, path = match.group(1).upper(), match.group(2)
            if line.strip().startswith("+"):
                added_apis.append(f"{method} {path}")
            elif line.strip().startswith("-"):
                removed_apis.append(f"{method} {path}")

    parts = []
    if added_apis:
        parts.append(f"新增: {', '.join(added_apis[:5])}")
    if removed_apis:
        parts.append(f"删除: {', '.join(removed_apis[:5])}")
    return "; ".join(parts)


def build_refresh_prompt(change_summary: str) -> str:
    """为上下文刷新 Agent 构建分析 prompt。

    Args:
        change_summary: extract_code_change_summary() 的输出

    Returns:
        str: 完整的刷新分析 prompt
    """
    current_glossary = _safe_read(_CONTEXT_FILES["domain_glossary"], max_chars=_MAX_CHARS_REFRESH_GLOSSARY)
    current_context = _safe_read(_CONTEXT_FILES["project_context"], max_chars=_MAX_CHARS_REFRESH_CONTEXT)

    return (
        "你是项目上下文刷新分析师。以下是最近的代码变更摘要和当前的项目上下文文档。\n"
        "请分析变更对项目上下文的影响，输出需要更新的内容。\n\n"
        f"## 当前代码变更\n\n{change_summary}\n\n"
        f"## 当前项目上下文（摘要）\n\n{current_context}\n\n"
        f"## 当前领域词典\n\n{current_glossary}\n\n"
        "请按以下格式输出分析结果：\n\n"
        "### 1. 需要新增到领域词典的术语\n"
        "（列出新发现的业务术语、字段名、枚举值，包含中文名、英文/代码名、说明）\n\n"
        "### 2. 需要更新的项目上下文\n"
        "（列出项目背景、技术栈、已知限制等需要更新的条目）\n\n"
        "### 3. 需要更新的 API 清单\n"
        "（列出新增、修改或删除的 API 路径）\n\n"
        "### 4. 需要更新的模块边界\n"
        "（如有新的目录结构或职责变化）\n\n"
        "### 5. README 更新建议\n"
        "（面向用户的功能说明是否需要调整）\n"
    )
