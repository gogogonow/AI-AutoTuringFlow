"""工具权限上下文模块 — 基于角色 × 模式的工具权限过滤。

提供 get_tools_for_role(role, mode) 函数，替代 main.py 中的手动工具列表硬编码。
"""

from tools.file_tools import (
    write_code_tool,
    patch_code_tool,
    read_code_tool,
    search_code_tool,
    list_files_tool,
    execute_command_tool,
)
from tools.github_tools import fetch_requirement_tool

# ---------------------------------------------------------------------------
# 工具名 → 工具对象 映射表
# ---------------------------------------------------------------------------
_TOOL_MAP = {
    "fetch_requirement": fetch_requirement_tool,
    "write_code": write_code_tool,
    "patch_code": patch_code_tool,
    "read_code": read_code_tool,
    "search_code": search_code_tool,
    "list_files": list_files_tool,
    "execute_command": execute_command_tool,
}

# ---------------------------------------------------------------------------
# 权限矩阵：role → mode → 允许使用的工具名列表
# ---------------------------------------------------------------------------
# 设计原则（对应 main.py 原有逻辑）：
#   architect : feature 模式只需 fetch；其他模式额外需要 read/list/search
#   ui_designer : 不使用任何工具（目前纯推理输出）
#   frontend_dev: feature 模式只用 write；其他模式用 read/list/search/patch/write
#   backend_dev : feature 模式只用 write；upgrade/bugfix 同 frontend；
#                 ui-beautify 模式后端不参与（返回空列表）
#   reviewer    : 所有模式统一用 read/list/search/patch
# ---------------------------------------------------------------------------
_PERMISSION_MATRIX: dict[str, dict[str, list[str]]] = {
    "architect": {
        "feature":     ["fetch_requirement"],
        "upgrade":     ["fetch_requirement", "read_code", "list_files", "search_code"],
        "bugfix":      ["fetch_requirement", "read_code", "list_files", "search_code"],
        "ui-beautify": ["fetch_requirement", "read_code", "list_files", "search_code"],
    },
    "ui_designer": {
        "feature":     [],
        "upgrade":     [],
        "bugfix":      [],
        "ui-beautify": [],
    },
    "frontend_dev": {
        "feature":     ["write_code", "execute_command"],
        "upgrade":     ["read_code", "list_files", "search_code", "patch_code", "write_code", "execute_command"],
        "bugfix":      ["read_code", "list_files", "search_code", "patch_code", "write_code", "execute_command"],
        "ui-beautify": ["read_code", "list_files", "search_code", "patch_code", "write_code", "execute_command"],
    },
    "backend_dev": {
        "feature":     ["write_code", "execute_command"],
        "upgrade":     ["read_code", "list_files", "search_code", "patch_code", "write_code", "execute_command"],
        "bugfix":      ["read_code", "list_files", "search_code", "patch_code", "write_code", "execute_command"],
        "ui-beautify": [],  # 后端不参与 ui-beautify
    },
    "reviewer": {
        "feature":     ["read_code", "list_files", "search_code", "patch_code"],
        "upgrade":     ["read_code", "list_files", "search_code", "patch_code"],
        "bugfix":      ["read_code", "list_files", "search_code", "patch_code"],
        "ui-beautify": ["read_code", "list_files", "search_code", "patch_code"],
    },
}

# 支持的合法值
_VALID_ROLES = frozenset(_PERMISSION_MATRIX)
_VALID_MODES = frozenset(next(iter(_PERMISSION_MATRIX.values())))


def get_tools_for_role(role: str, mode: str) -> list:
    """返回指定角色在指定模式下允许使用的 CrewAI tool 对象列表。

    Args:
        role: Agent 角色，支持 architect / ui_designer / frontend_dev /
              backend_dev / reviewer
        mode: 任务模式，支持 feature / upgrade / bugfix / ui-beautify

    Returns:
        list: 包含零个或多个 CrewAI tool 对象的列表。未知角色或模式返回空列表。
    """
    if role not in _VALID_ROLES:
        print(f"⚠️  [工具权限] 未知角色 '{role}'，返回空工具列表")
        return []
    if mode not in _VALID_MODES:
        print(f"⚠️  [工具权限] 未知模式 '{mode}'，返回空工具列表")
        return []

    tool_names = _PERMISSION_MATRIX[role][mode]
    tool_objects = [_TOOL_MAP[name] for name in tool_names if name in _TOOL_MAP]
    print(f"🔧 [工具权限] {role} × {mode} → {[t.name for t in tool_objects]}")
    return tool_objects
