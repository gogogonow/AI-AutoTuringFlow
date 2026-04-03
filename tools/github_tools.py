import os
import subprocess
from github import Github
from crewai.tools import tool

# Lazy-initialised at module level; requires GITHUB_TOKEN, REPO_NAME and
# ISSUE_NUMBER env vars that are only available inside the CI workflow.
# Using .get() with a fallback prevents KeyError when the module is imported
# in environments that don't set these variables (e.g. Railway deployment).
_github_token = os.environ.get("GITHUB_TOKEN", "")
_repo_name = os.environ.get("REPO_NAME", "")
_issue_number_str = os.environ.get("ISSUE_NUMBER", "0")

g = Github(_github_token) if _github_token else None
repo = g.get_repo(_repo_name) if g and _repo_name else None
issue_number = int(_issue_number_str)


def parse_issue_config() -> dict:
    """从 Issue 的 labels 中解析任务模式和影响范围配置。

    模式 labels（互斥，默认 feature）：
      - mode:upgrade      → 依赖升级或替换
      - mode:feature      → 新功能需求
      - mode:bugfix       → Bug 修复
      - mode:ui-beautify  → UI 美化（界面设计优化 + 前端增量开发）

    影响范围 labels（互斥，默认 fullstack；ui-beautify 模式强制为 frontend）：
      - scope:frontend  → 仅前端
      - scope:backend   → 仅后端
      - scope:fullstack → 全栈

    Returns:
        dict: {"mode": str, "scope": str, "labels": list[str], "title": str}
    """
    if repo is None:
        raise RuntimeError(
            "GitHub client is not initialised. "
            "Ensure GITHUB_TOKEN and REPO_NAME environment variables are set."
        )
    issue = repo.get_issue(number=issue_number)
    label_names = [label.name for label in issue.labels]

    # 解析模式
    mode = "feature"  # 默认为新功能模式
    for name in label_names:
        if name.startswith("mode:"):
            parsed = name.split(":", 1)[1].strip()
            if parsed in ("upgrade", "feature", "bugfix", "ui-beautify"):
                mode = parsed
                break

    # 解析影响范围
    scope = "fullstack"  # 默认全栈
    for name in label_names:
        if name.startswith("scope:"):
            parsed = name.split(":", 1)[1].strip()
            if parsed in ("frontend", "backend", "fullstack"):
                scope = parsed
                break

    # ui-beautify 模式强制前端范围（UI 美化不涉及后端）
    if mode == "ui-beautify":
        scope = "frontend"

    return {"mode": mode, "scope": scope, "labels": label_names, "title": issue.title}


@tool("Fetch Requirement from Issue")
def fetch_requirement_tool() -> str:
    """获取当前触发执行的 GitHub Issue 的标题、内容及配置（模式和影响范围），作为初始需求。"""
    if repo is None:
        raise RuntimeError(
            "GitHub client is not initialised. "
            "Ensure GITHUB_TOKEN and REPO_NAME environment variables are set."
        )
    issue = repo.get_issue(number=issue_number)
    config = parse_issue_config()
    return (
        f"Title: {issue.title}\n"
        f"Body: {issue.body}\n"
        f"---\n"
        f"任务模式: {config['mode']} (upgrade=依赖升级/替换, feature=新功能, bugfix=Bug修复, ui-beautify=UI美化)\n"
        f"影响范围: {config['scope']} (frontend=仅前端, backend=仅后端, fullstack=全栈)\n"
        f"所有标签: {', '.join(config['labels'])}"
    )

@tool("Create Pull Request")
def create_pr_tool(branch_name: str, pr_title: str, commit_message: str) -> str:
    """在本地代码写入完成后，调用此工具将代码推送到新分支并创建 PR。"""
    github_token = os.environ.get("GITHUB_TOKEN")
    repo_name = os.environ.get("REPO_NAME")
    if not github_token:
        raise ValueError("环境变量 GITHUB_TOKEN 未设置")
    if not repo_name:
        raise ValueError("环境变量 REPO_NAME 未设置")

    subprocess.run(["git", "config", "--local", "user.name", "AI-AutoTuringFlow-Bot"], check=True)
    subprocess.run(["git", "config", "--local", "user.email", "ai-factory@noreply.example.com"], check=True)

    # 嵌入 token 到远端 URL，确保推送时有权限
    authenticated_url = f"https://x-access-token:{github_token}@github.com/{repo_name}.git"
    clean_url = f"https://github.com/{repo_name}.git"
    subprocess.run(["git", "remote", "set-url", "origin", authenticated_url], check=True)

    try:
        # 如果分支已存在则直接切换，否则创建新分支
        result = subprocess.run(["git", "checkout", "-b", branch_name], capture_output=True, text=True)
        if result.returncode != 0:
            subprocess.run(["git", "checkout", branch_name], check=True)

        subprocess.run(["git", "add", "."], check=True)
        # 如果没有新内容可提交，commit 可能会失败，此处忽略该错误
        subprocess.run(["git", "commit", "-m", commit_message])

        # 先尝试普通 push；若远端分支已有不同提交则先 rebase 再推，
        # 若 rebase 也失败（如冲突），则用 --force-with-lease 强制推送
        push_result = subprocess.run(["git", "push", "origin", branch_name], capture_output=True, text=True)
        if push_result.returncode != 0:
            rebase_result = subprocess.run(
                ["git", "pull", "--rebase", "origin", branch_name],
                capture_output=True, text=True
            )
            if rebase_result.returncode == 0:
                subprocess.run(["git", "push", "origin", branch_name], check=True)
            else:
                # rebase 失败时中止，改用 force-with-lease 推送
                subprocess.run(["git", "rebase", "--abort"], capture_output=True)
                subprocess.run(["git", "push", "--force-with-lease", "origin", branch_name], check=True)
    finally:
        # 清除 URL 中的 token，防止泄露到日志
        subprocess.run(["git", "remote", "set-url", "origin", clean_url], check=True)

    # 使用 GitHub API 创建 PR
    if repo is None:
        raise RuntimeError(
            "GitHub client is not initialised. "
            "Ensure GITHUB_TOKEN and REPO_NAME environment variables are set."
        )
    pr = repo.create_pull(title=pr_title, body=f"Closes #{issue_number}", head=branch_name, base="main")
    return f"Pull Request created successfully: {pr.html_url}"


def create_pr_direct(branch_name: str, pr_title: str, commit_message: str) -> str:
    """直接调用（非 LLM Tool）：将代码推送到新分支并创建 PR。
    用于 DevOps 去 Agent 化后，在 Crew 执行完毕后由 Python 代码直接调用。
    """
    github_token = os.environ.get("GITHUB_TOKEN")
    repo_name_env = os.environ.get("REPO_NAME")
    if not github_token:
        raise ValueError("环境变量 GITHUB_TOKEN 未设置")
    if not repo_name_env:
        raise ValueError("环境变量 REPO_NAME 未设置")

    subprocess.run(["git", "config", "--local", "user.name", "AI-AutoTuringFlow-Bot"], check=True)
    subprocess.run(["git", "config", "--local", "user.email", "ai-factory@noreply.example.com"], check=True)

    authenticated_url = f"https://x-access-token:{github_token}@github.com/{repo_name_env}.git"
    clean_url = f"https://github.com/{repo_name_env}.git"
    subprocess.run(["git", "remote", "set-url", "origin", authenticated_url], check=True)

    try:
        result = subprocess.run(["git", "checkout", "-b", branch_name], capture_output=True, text=True)
        if result.returncode != 0:
            subprocess.run(["git", "checkout", branch_name], check=True)

        subprocess.run(["git", "add", "."], check=True)
        # commit 在无变更时可能失败，此处忽略该错误（与 create_pr_tool 行为一致）
        subprocess.run(["git", "commit", "-m", commit_message])

        push_result = subprocess.run(["git", "push", "origin", branch_name], capture_output=True, text=True)
        if push_result.returncode != 0:
            rebase_result = subprocess.run(
                ["git", "pull", "--rebase", "origin", branch_name],
                capture_output=True, text=True
            )
            if rebase_result.returncode == 0:
                subprocess.run(["git", "push", "origin", branch_name], check=True)
            else:
                subprocess.run(["git", "rebase", "--abort"], capture_output=True)
                subprocess.run(["git", "push", "--force-with-lease", "origin", branch_name], check=True)
    finally:
        subprocess.run(["git", "remote", "set-url", "origin", clean_url], check=True)

    if repo is None:
        raise RuntimeError(
            "GitHub client is not initialised. "
            "Ensure GITHUB_TOKEN and REPO_NAME environment variables are set."
        )
    pr = repo.create_pull(title=pr_title, body=f"Closes #{issue_number}", head=branch_name, base="main")
    return f"Pull Request created successfully: {pr.html_url}"
