import os
from dotenv import load_dotenv
from crewai import Agent, Task, Crew, Process, LLM
# from langchain_openai import ChatOpenAI
# from langchain_openrouter import ChatOpenRouter

# 导入你之前写好的工具 (假设路径为 tools/github_tools.py 和 tools/file_tools.py)
from tools.github_tools import fetch_requirement_tool, create_pr_tool
from tools.file_tools import write_code_tool

# 加载本地 .env 文件（如果是本地调试的话）
load_dotenv()

# ==========================================
# 0. 初始化底层大模型 (接入 GitHub Models)
# ==========================================
print("正在连接大模型神经中枢...")
# llm = ChatOpenAI(
#     model_name="gpt-5", 
#     temperature=0.2,
#     base_url="https://models.inference.ai.azure.com", 
#     api_key=os.environ.get("GITHUB_TOKEN") 
# )

# llm = ChatOpenAI(
#     # OpenRouter 的专属模型命名格式：提供商/模型名
#     model_name="anthropic/claude-4.6-sonnet", 
#     temperature=0.2, # 保持严谨的代码生成温度
#     # 强制将请求发给 OpenRouter 的网关
#     base_url="https://openrouter.ai/api/v1", 
#     # 读取我们刚才配好的 OpenRouter Key
#     api_key=os.environ.get("OPENROUTER_API_KEY")
# )

# llm = ChatOpenRouter(
#     # OpenRouter 的专属模型命名格式：提供商/模型名
#     model="anthropic/claude-4.6-sonnet", 
#     temperature=0.2, # 保持严谨的代码生成温度
#     # 读取我们刚才配好的 OpenRouter Key
#     api_key=os.environ.get("OPENROUTER_API_KEY"),

#     # ------------------ 3. 高级玩法：防 403 拦截的自动容灾 (Fallback) ------------------
#     # 依然可以通过 model_kwargs 向底层注入 OpenRouter 特有的路由机制。
#     # 如果 Claude 傲娇拒答，瞬间无缝切换到备胎模型，保证流水线不中断！
#     model_kwargs={
#         "extra_body": {
#             "route": "fallback",
#             "models": [
#                 "openai/gpt-5", # 首选：最强代码大脑
#                 "deepseek/deepseek-coder"      # 备胎 1：纯粹的代码神仙，毫无安全审查包袱
#             ]
#         }
#     }
# )


llm_reasoning = LLM(
    # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
    model="openai/DeepSeek-R1", 
    temperature=0.2,
    api_key=os.environ.get("GITHUB_TOKEN") ,
    base_url="https://models.inference.ai.azure.com"
)

llm_coding = LLM(
    # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
    model="openai/gpt-4o", 
    temperature=0.2,
    api_key=os.environ.get("GITHUB_TOKEN") ,
    base_url="https://models.inference.ai.azure.com"
)

# llm_gpt_5_2_pro = LLM(
#     # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
#     model="openrouter/openai/gpt-5.2-pro", 
#     temperature=0.2,
#     max_tokens=8192,
#     api_key=os.environ.get("OPENROUTER_API_KEY"),
#     base_url="https://openrouter.ai/api/v1",
#     extra_headers={
#         "HTTP-Referer": "https://github.com/gogogonow/AI-AutoTuringFlow",
#         "X-Title": "AI-AutoTuringFlow-Factory"
#     }
# )

# llm_deepseek_r1 = LLM(
#     # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
#     model="openrouter/deepseek/deepseek-r1", 
#     temperature=0.2,
#     max_tokens=8192,
#     api_key=os.environ.get("OPENROUTER_API_KEY"),
#     base_url="https://openrouter.ai/api/v1",
#     extra_headers={
#         "HTTP-Referer": "https://github.com/gogogonow/AI-AutoTuringFlow",
#         "X-Title": "AI-AutoTuringFlow-Factory"
#     }
# )

# llm_gemini_3_1_pro_preview = LLM(
#     # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
#     model="openrouter/google/gemini-3.1-pro-preview", 
#     temperature=0.2,
#     max_tokens=8192,
#     api_key=os.environ.get("OPENROUTER_API_KEY"),
#     base_url="https://openrouter.ai/api/v1",
#     extra_headers={
#         "HTTP-Referer": "https://github.com/gogogonow/AI-AutoTuringFlow",
#         "X-Title": "AI-AutoTuringFlow-Factory"
#     }
# )

# llm_kimi_k2_thinking = LLM(
#     # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
#     model="openrouter/moonshotai/kimi-k2-thinking", 
#     temperature=0.2,
#     max_tokens=8192,
#     api_key=os.environ.get("OPENROUTER_API_KEY"),
#     base_url="https://openrouter.ai/api/v1",
#     extra_headers={
#         "HTTP-Referer": "https://github.com/gogogonow/AI-AutoTuringFlow",
#         "X-Title": "AI-AutoTuringFlow-Factory"
#     }
# )

# llm_gpt_5_3_chat = LLM(
#     # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
#     model="openrouter/openai/gpt-5.3-chat", 
#     temperature=0.2,
#     max_tokens=8192,
#     api_key=os.environ.get("OPENROUTER_API_KEY"),
#     base_url="https://openrouter.ai/api/v1",
#     extra_headers={
#         "HTTP-Referer": "https://github.com/gogogonow/AI-AutoTuringFlow",
#         "X-Title": "AI-AutoTuringFlow-Factory"
#     }
# )

# llm_qwen3_235b_a22b = LLM(
#     # 只要加上 openrouter/ 前缀，底层就会自动使用 OpenRouter 的通道
#     model="openrouter/qwen/qwen3-235b-a22b", 
#     temperature=0.2,
#     max_tokens=8192,
#     api_key=os.environ.get("OPENROUTER_API_KEY"),
#     base_url="https://openrouter.ai/api/v1",
#     extra_headers={
#         "HTTP-Referer": "https://github.com/gogogonow/AI-AutoTuringFlow",
#         "X-Title": "AI-AutoTuringFlow-Factory"
#     }
# )


# ==========================================
# 1. 定义 Agents (专家团队)
# ==========================================

architect = Agent(
    role='首席系统架构师',
    goal='分析原始需求，输出包含技术栈选型、前后端 API 契约设计、以及跨语言调用的核心架构文档。',
    backstory='你是一位拥有深厚通信研发工具链和硬件自动化设计经验的首席架构师。你擅长规划高可用、可扩展的系统，精通 Java/Web 技术栈与 Python 自动化脚本的融合。你能够精准定义前后端交互协议，并擅长拆解复杂的业务流程，为多团队协作奠定基础。',
    verbose=True,
    allow_delegation=False,
    llm=llm_reasoning
)

ui_designer = Agent(
    role='UI/UX 交互设计师',
    goal='基于架构师的需求，设计全局 UI 规范、界面布局结构以及交互流程（输出 Markdown 描述）。',
    backstory='你精通 B 端复杂工程系统和工具链界面的设计。你深谙用户体验，擅长规划包含复杂参数配置面板、以及用于承载 WebGL/3D 渲染模型（如 ODB++ 或 STP 文件解析结果）的视图容器交互逻辑，能为前端开发提供清晰的组件划分建议。',
    verbose=True,
    allow_delegation=False,
    llm=llm_reasoning
)

frontend_dev = Agent(
    role='高级前端工程师',
    goal='严格遵循 UI 规范和 API 契约，编写高质量的现代前端代码，并保存到本地文件。',
    backstory='你精通现代 Web 技术栈。你不仅擅长构建响应式的组件库，还拥有丰富的浏览器端渲染经验，能够从容应对包含复杂 DOM 结构或 Three.js 可视化视窗的高性能前端开发需求。',
    verbose=True,
    tools=[write_code_tool],
    allow_delegation=False,
    llm=llm_coding
)

backend_dev = Agent(
    role='高级后端工程师',
    goal='基于架构师的设计，编写健壮的后端服务、核心算法逻辑以及自动化解析脚本，并保存到本地文件。',
    backstory='你是一个极其严谨的后端极客，精通高并发架构。你擅长使用 Java 和 Python 开发自动化任务调度、CAD 自动化脚本、以及解析大型复杂工程文件结构。你编写的代码逻辑清晰且包含完善的异常处理。',
    verbose=True,
    tools=[write_code_tool],
    allow_delegation=False,
    llm=llm_coding
)

qa_engineer = Agent(
    role='自动化测试工程师 (SDET)',
    goal='审查前后端生成的代码，编写并完善单元测试和接口集成测试脚本，并保存到本地。',
    backstory='你拥有“破坏者”的思维，对代码缺陷有着敏锐的嗅觉。你精通现代测试框架，致力于通过高覆盖率的测试用例（尤其是针对核心算法和数据解析模块）确保代码的健壮性和边界异常处理能力。',
    verbose=True,
    tools=[write_code_tool], 
    allow_delegation=False,
    llm=llm_coding
)

devops_engineer = Agent(
    role='DevOps 与发布工程师',
    goal='在代码写入完成后，将工作区提交到远端并创建 Pull Request。',
    backstory='你是 CI/CD 流水线的大师，熟练掌握 Git 工作流和 GitHub API，确保代码从开发环境平滑过渡到版本库，为最终的人工 Review 做好准备。',
    verbose=True,
    tools=[create_pr_tool],
    allow_delegation=False,
    llm=llm_reasoning
)

# ==========================================
# 2. 定义 Tasks (流水线节点)
# ==========================================

task_architecture = Task(
    description='调用 fetch_requirement_tool 获取原始 Issue 需求。基于需求，输出详细的架构设计，包括目录结构、API 接口定义以及数据库/数据结构设计。',
    expected_output='一份结构清晰的系统架构设计文档。',
    agent=architect,
    tools=[fetch_requirement_tool]
)

task_ui_design = Task(
    description='读取架构师的架构设计文档。为该系统设计交互流程和界面布局，详细描述各个组件的功能、位置以及可能需要的 3D 渲染视图区域。',
    expected_output='一份详细的前端 UI 布局与交互规范文档。',
    agent=ui_designer
)

task_frontend = Task(
    description='基于 UI 规范和架构 API 契约，编写完整的前端页面或组件代码（HTML/CSS/JS 或对应的框架代码）。必须调用 write_code_tool 将代码写入本地的 `frontend/` 目录。',
    expected_output='前端代码已成功生成并写入本地 frontend 目录。',
    agent=frontend_dev
)

task_backend = Task(
    description='基于架构设计文档，编写完整的后端 API 逻辑、数据解析脚本或核心算法。必须调用 write_code_tool 将代码写入本地的 `backend/` 目录。',
    expected_output='后端代码已成功生成并写入本地 backend 目录。',
    agent=backend_dev
)

task_qa = Task(
    description='审查前端和后端生成的代码。针对后端的解析逻辑或核心 API，以及前端的关键组件，编写自动化测试用例。必须调用 write_code_tool 将测试代码写入本地的 `tests/` 目录。',
    expected_output='自动化测试用例已成功生成并写入本地 tests 目录。',
    agent=qa_engineer
)

task_devops = Task(
    description='确认所有代码（frontend, backend, tests）都已写入完毕后，调用 create_pr_tool 创建一个新的 Git 分支（如 feature/ai-auto-dev）并提交 Pull Request。',
    expected_output='成功创建 GitHub Pull Request 的 URL。',
    agent=devops_engineer
)

# ==========================================
# 3. 组装 Crew 并执行
# ==========================================

software_factory = Crew(
    agents=[architect, ui_designer, frontend_dev, backend_dev, qa_engineer, devops_engineer],
    tasks=[task_architecture, task_ui_design, task_frontend, task_backend, task_qa, task_devops],
    process=Process.sequential # 严格按照瀑布流顺序执行
)

if __name__ == "__main__":
    print("🚀 启动 6 人全栈 AI 研发团队...")
    try:
        result = software_factory.kickoff()
        print("\n✅ 流水线执行完毕！最终报告：")
        print(result)
    except Exception as e:
        print(f"\n❌ 执行过程中出现异常: {str(e)}")
