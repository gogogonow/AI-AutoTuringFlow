import os
import sys
from crewai import Agent, Task, Crew, Process
from langchain_openai import ChatOpenAI
from tools.github_tools import fetch_requirement_tool, create_pr_tool
from tools.file_tools import write_code_tool

# 验证必需的环境变量
required_env_vars = ["OPENAI_API_KEY", "GITHUB_TOKEN", "REPO_NAME", "ISSUE_NUMBER"]
missing = [v for v in required_env_vars if not os.environ.get(v)]
if missing:
    print(f"❌ Missing required environment variables: {', '.join(missing)}")
    sys.exit(1)

# 初始化底层大模型
# 现在改为调用 GitHub 提供的 GPT-4o 接口：
llm = ChatOpenAI(
    model_name="gpt-4o", 
    temperature=0.2,
    api_key=os.environ["GITHUB_TOKEN"], # 直接使用 GitHub 的 Token
    base_url="https://models.inference.ai.azure.com" # 指向 GitHub Models 的网关
)

# ==========================================
# 1. 定义 Agents (专家团队)
# ==========================================

# 架构师 Agent
architect = Agent(
    role='系统架构师',
    goal='分析需求，设计清晰的软件架构、技术栈选择和文件结构。',
    backstory='你是一位拥有多年工具链开发经验的资深架构师，精通 Python、Java 以及自动化脚本设计。你总是能将模糊的需求转化为高度结构化的设计文档。',
    verbose=True,
    allow_delegation=False,
    llm=llm
)

# 研发 Agent
developer = Agent(
    role='高级软件工程师',
    goal='严格按照架构师的设计编写健壮、可维护的代码，并保存到本地文件系统。',
    backstory='你是一个极其严谨的程序员，擅长编写各种自动化脚本和后端逻辑。你能完美地调用工具把代码写入正确的文件路径。',
    verbose=True,
    tools=[write_code_tool],
    allow_delegation=False,
    llm=llm
)

# DevOps Agent
devops = Agent(
    role='DevOps 工程师',
    goal='将本地写好的代码提交到远端仓库，并创建 Pull Request。',
    backstory='你负责整个研发流程的最后一环，熟练掌握 Git 工作流和 GitHub API。',
    verbose=True,
    tools=[create_pr_tool],
    allow_delegation=False,
    llm=llm
)

# ==========================================
# 2. 定义 Tasks (工作流)
# ==========================================

# 任务 1：解析需求并设计
design_task = Task(
    description='调用 fetch_requirement_tool 获取 Issue 需求。基于需求，输出详细的架构设计，包括必须创建的文件路径及其包含的类或函数的详细说明。',
    expected_output='一份包含目录结构和各文件职责说明的架构设计方案。',
    agent=architect,
    tools=[fetch_requirement_tool]
)

# 任务 2：编写代码
coding_task = Task(
    description='基于架构师的设计，生成完整的、可运行的代码。必须调用 write_code_tool 将代码写入对应的文件路径中。不要只写片段，要写完整的生产级代码。',
    expected_output='所有的代码文件都已成功写入本地文件系统。',
    agent=developer
)

# 任务 3：提交 PR
pr_task = Task(
    description='代码写入完成后，调用 create_pr_tool 创建一个新的 Git 分支并提交 Pull Request。分支名请以 "feature/ai-" 开头。',
    expected_output='成功创建 PR 的 URL 链接。',
    agent=devops
)

# ==========================================
# 3. 组装 Crew 并执行
# ==========================================

software_factory = Crew(
    agents=[architect, developer, devops],
    tasks=[design_task, coding_task, pr_task],
    process=Process.sequential # 顺序执行：设计 -> 编码 -> 提PR
)

if __name__ == "__main__":
    print("🚀 AI Multi-Agent Software Factory Starting...")
    result = software_factory.kickoff()
    print("✅ Workflow Completed. Result:")
    print(result)
