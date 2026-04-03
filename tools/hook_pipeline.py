"""钩子管线模块 — Agent 执行前后的质量检查钩子。

提供 pre_task_hook / post_task_hook / run_hooks_report，
在 Crew 执行前后进行配置合法性校验和输出质量检查。

关键约束：hook 失败只打印警告，不中断 CrewAI 流程。
"""

from dataclasses import dataclass, field


# ---------------------------------------------------------------------------
# 数据结构
# ---------------------------------------------------------------------------

@dataclass
class HookResult:
    """单次 hook 检查的结果。"""
    passed: bool
    message: str
    details: dict = field(default_factory=dict)


# ---------------------------------------------------------------------------
# 各模式下架构师输出应包含的关键章节
# ---------------------------------------------------------------------------
_ARCH_REQUIRED_SECTIONS: dict[str, list[str]] = {
    "feature": ["## API 契约", "## 文件清单"],
    "upgrade": ["## 变更清单", "## 迁移步骤"],
    "bugfix":  ["## 根因分析", "## 受影响文件"],
    "ui-beautify": ["## 变更清单", "## 设计规范"],
}

# 开发工程师角色集合（输出质量检查需要验证工具调用）
_DEV_ROLES = frozenset({"frontend_dev", "backend_dev"})
# Review Agent 角色集合
_REVIEW_ROLES = frozenset({"reviewer"})
# 代码写入成功关键字
_WRITE_SUCCESS_KEYWORDS = ("Successfully wrote", "Successfully patched")
# Review 结论关键字（中英文均兼容）
_REVIEW_CONCLUSION_KEYWORDS = (
    "校验完成", "审查完成", "一致性校验", "检查完成",
    "patch_code_tool", "no issues", "已修复", "修复完成",
)


# ---------------------------------------------------------------------------
# Pre-task hook
# ---------------------------------------------------------------------------

def pre_task_hook(
    agent_role: str,
    mode: str,
    task_description: str,
    context_output: str,
) -> HookResult:
    """在 Task 执行前调用，检查前置任务输出是否符合要求。

    Args:
        agent_role: 当前 Agent 的角色标识（architect/ui_designer/
                    frontend_dev/backend_dev/reviewer）
        mode: 任务模式（feature/upgrade/bugfix/ui-beautify）
        task_description: 当前 Task 的描述文字（用于上下文日志）
        context_output: 前置 Task 的输出内容（架构师之后的 Agent 使用）

    Returns:
        HookResult
    """
    # 架构师是第一个执行的，无前置依赖，直接通过
    if agent_role == "architect":
        return HookResult(
            passed=True,
            message="架构师任务无前置依赖，pre-flight 检查通过",
            details={"agent_role": agent_role, "mode": mode},
        )

    # 若没有前置输出，只给出警告，不阻断
    if not context_output or not context_output.strip():
        return HookResult(
            passed=False,
            message=f"⚠️  [{agent_role}] 前置任务输出为空，可能影响执行质量",
            details={"agent_role": agent_role, "mode": mode, "context_length": 0},
        )

    # 对架构师之后的 Agent：检查架构师输出是否包含必要章节
    required = _ARCH_REQUIRED_SECTIONS.get(mode, [])
    missing = [sec for sec in required if sec not in context_output]
    if missing:
        return HookResult(
            passed=False,
            message=(
                f"⚠️  [{agent_role}] 架构师输出缺少必要章节：{missing}，"
                "可能影响后续任务执行质量"
            ),
            details={
                "agent_role": agent_role,
                "mode": mode,
                "missing_sections": missing,
                "required_sections": required,
            },
        )

    return HookResult(
        passed=True,
        message=f"✅ [{agent_role}] 前置输出包含所有必要章节，pre-task 检查通过",
        details={
            "agent_role": agent_role,
            "mode": mode,
            "found_sections": required,
        },
    )


# ---------------------------------------------------------------------------
# Post-task hook
# ---------------------------------------------------------------------------

def post_task_hook(agent_role: str, mode: str, task_output: str) -> HookResult:
    """在 Task 执行后调用，检查输出质量。

    Args:
        agent_role: Agent 角色标识
        mode: 任务模式
        task_output: 该 Task 的输出内容

    Returns:
        HookResult
    """
    output_text = task_output or ""

    # 开发工程师：验证实际调用了文件写入工具
    if agent_role in _DEV_ROLES:
        wrote = any(kw in output_text for kw in _WRITE_SUCCESS_KEYWORDS)
        if not wrote:
            return HookResult(
                passed=False,
                message=(
                    f"⚠️  [{agent_role}] 输出中未检测到 'Successfully wrote' 或 "
                    "'Successfully patched'，可能未实际调用文件写入工具"
                ),
                details={
                    "agent_role": agent_role,
                    "mode": mode,
                    "checked_keywords": list(_WRITE_SUCCESS_KEYWORDS),
                    "output_length": len(output_text),
                },
            )
        return HookResult(
            passed=True,
            message=f"✅ [{agent_role}] 检测到文件写入成功记录，post-task 检查通过",
            details={"agent_role": agent_role, "mode": mode},
        )

    # Review Agent：验证包含校验结论
    if agent_role in _REVIEW_ROLES:
        has_conclusion = any(kw in output_text for kw in _REVIEW_CONCLUSION_KEYWORDS)
        if not has_conclusion:
            return HookResult(
                passed=False,
                message=(
                    f"⚠️  [{agent_role}] 输出中未检测到校验结论关键词，"
                    "请确认 Review Agent 是否完成了代码审查"
                ),
                details={
                    "agent_role": agent_role,
                    "mode": mode,
                    "checked_keywords": list(_REVIEW_CONCLUSION_KEYWORDS),
                    "output_length": len(output_text),
                },
            )
        return HookResult(
            passed=True,
            message=f"✅ [{agent_role}] 检测到校验结论，post-task 检查通过",
            details={"agent_role": agent_role, "mode": mode},
        )

    # 其他 Agent（architect/ui_designer）：直接通过
    return HookResult(
        passed=True,
        message=f"✅ [{agent_role}] post-task 检查通过（无特殊输出质量要求）",
        details={"agent_role": agent_role, "mode": mode},
    )


# ---------------------------------------------------------------------------
# 汇总报告
# ---------------------------------------------------------------------------

def run_hooks_report(results: list) -> str:
    """将多个 HookResult 汇总为可读报告字符串。

    Args:
        results: HookResult 对象列表

    Returns:
        str: 格式化的报告文字
    """
    if not results:
        return "📋 [Hook 报告] 无检查结果"

    total = len(results)
    passed = sum(1 for r in results if r.passed)
    failed = total - passed

    lines = [
        "=" * 60,
        f"📋 Hook 质量检查报告  |  总计: {total}  ✅ 通过: {passed}  ⚠️ 警告: {failed}",
        "=" * 60,
    ]
    for i, r in enumerate(results, 1):
        status = "✅ PASS" if r.passed else "⚠️  WARN"
        lines.append(f"  [{i:02d}] {status}  {r.message}")
        if not r.passed and r.details:
            for k, v in r.details.items():
                lines.append(f"        {k}: {v}")
    lines.append("=" * 60)
    return "\n".join(lines)
