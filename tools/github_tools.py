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
    subprocess.run(["git", "config", "--local", "user.name", "AI-AutoTuringFlow-Bot"], check=True)
    subprocess.run(["git", "config", "--local", "user.email", "ai-factory@noreply.example.com"], check=True)
    subprocess.run(["git", "checkout", "-b", branch_name], check=True)
    subprocess.run(["git", "add", "."], check=True)
    subprocess.run(["git", "commit", "-m", commit_message], check=True)
    subprocess.run(["git", "push", "origin", branch_name], check=True)

    # 使用 GitHub API 创建 PR
    pr = repo.create_pull(title=pr_title, body=f"Closes #{issue_number}", head=branch_name, base="main")
    return f"Pull Request created successfully: {pr.html_url}"
