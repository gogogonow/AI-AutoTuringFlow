import os
import subprocess
from github import Github
from crewai.tools import tool

g = Github(os.environ["GITHUB_TOKEN"])
repo = g.get_repo(os.environ["REPO_NAME"])
issue_number = int(os.environ["ISSUE_NUMBER"])

@tool("Fetch Requirement from Issue")
def fetch_requirement_tool() -> str:
    """获取当前触发执行的 GitHub Issue 的标题和内容，作为初始需求。"""
    issue = repo.get_issue(number=issue_number)
    return f"Title: {issue.title}\nBody: {issue.body}"

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
        subprocess.run(["git", "commit", "-m", commit_message], check=True)
        subprocess.run(["git", "push", "origin", branch_name], check=True)
    finally:
        # 清除 URL 中的 token，防止泄露到日志
        subprocess.run(["git", "remote", "set-url", "origin", clean_url], check=True)

    # 使用 GitHub API 创建 PR
    pr = repo.create_pull(title=pr_title, body=f"Closes #{issue_number}", head=branch_name, base="main")
    return f"Pull Request created successfully: {pr.html_url}"
