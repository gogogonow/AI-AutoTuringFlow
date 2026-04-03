import os
from dotenv import load_dotenv
from crewai import Agent, Task, Crew, Process, LLM

from tools.github_tools import fetch_requirement_tool, create_pr_tool, parse_issue_config
from tools.file_tools import write_code_tool, read_code_tool, list_files_tool

# 加载本地 .env 文件（如果是本地调试的话）
load_dotenv()

# ==========================================
# 0. 初始化底层大模型 (接入 OAIPro)
# ==========================================
print("正在连接大模型神经中枢...")

oaipro_key = os.environ.get("OAIPRO_API_KEY", "")

# 使用 LiteLLM 统一路由所有 LLM 调用（通过 is_litellm=True 跳过 CrewAI 原生 SDK）。
# 原生 OpenAI/Anthropic SDK 客户端直连 OAIPro 代理时，工具调用后的响应可能返回空内容；
# LiteLLM 对代理端点的兼容性更好，能正确处理参数转换和响应解析。

llm_reasoning = LLM(
    # 架构设计与 UI 设计：使用 GPT-5 进行深度推理（通过 OAIPro OpenAI 兼容接口）
    model="openai/gpt-3.5-turbo",
    max_tokens=4096,
    api_key=oaipro_key,
    base_url="https://api.oaipro.com/v1",
    is_litellm=True,
)

llm_coding = LLM(
    # 代码生成：使用 Claude Sonnet 4.5 进行高质量代码编写（通过 OAIPro OpenAI 兼容接口）
    # 注意：必须使用 openai/ 前缀 + /v1 端点，anthropic/ 前缀会导致 LiteLLM 使用原生
    # Anthropic API 格式发送工具调用，但 OAIPro 仅提供 OpenAI 兼容接口，造成工具调用被静默丢弃。
    model="openai/claude-sonnet-4-5-20250929",
    max_tokens=8192,
    api_key=oaipro_key,
    base_url="https://api.oaipro.com/v1",
    is_litellm=True,
)

llm_light = LLM(
    # DevOps 轻量级任务：使用 GPT-4o-mini 节省成本（通过 OAIPro OpenAI 兼容接口）
    model="openai/gpt-4o-mini",
    max_tokens=4096,
    api_key=oaipro_key,
    base_url="https://api.oaipro.com/v1",
    is_litellm=True,
)


# ==========================================
# 1. 解析 Issue 配置（模式 + 影响范围）
# ==========================================

config = parse_issue_config()
MODE = config["mode"]      # "upgrade" | "feature" | "bugfix" | "ui-beautify"
SCOPE = config["scope"]    # "frontend" | "backend" | "fullstack"

print(f"📋 任务模式: {MODE} | 影响范围: {SCOPE}")

needs_frontend = SCOPE in ("frontend", "fullstack")
needs_backend = SCOPE in ("backend", "fullstack")


# ==========================================
# 2. 模式感知的 Agent 定义
# ==========================================

# --- 各模式下架构师的差异化配置 ---
ARCHITECT_CONFIG = {
    "feature": {
        "goal": "分析原始需求，输出包含技术栈选型、前后端 API 契约设计、以及跨语言调用的核心架构文档。",
        "backstory": (
            "你是一位拥有深厚通信研发工具链和硬件自动化设计经验的首席架构师。"
            "你擅长规划高可用、可扩展的系统，精通 Java/Web 技术栈与 Python 自动化脚本的融合。"
            "你能够精准定义前后端交互协议，并擅长拆解复杂的业务流程，为多团队协作奠定基础。"
        ),
    },
    "upgrade": {
        "goal": "分析升级/替换需求，读取现有代码中的依赖配置和受影响的源码文件，输出包含兼容性影响评估、迁移步骤和变更清单的升级方案。",
        "backstory": (
            "你是一位精通技术栈演进和依赖管理的首席架构师。"
            "你擅长分析 pom.xml、package.json 等依赖配置，识别 API 破坏性变更（如 javax→jakarta 迁移），"
            "制定安全的升级路径。你会先使用 read_code_tool 和 list_files_tool 仔细审查现有代码结构，"
            "再给出精确的变更清单。"
        ),
    },
    "bugfix": {
        "goal": "分析 Bug 描述，定位可能的根因，读取相关源码文件，输出包含问题分析、修复方案和回归测试建议的诊断报告。",
        "backstory": (
            "你是一位拥有丰富系统排障经验的首席架构师。"
            "你擅长从 Bug 报告中提取关键线索，利用 read_code_tool 和 list_files_tool 快速定位问题代码，"
            "分析调用链路和数据流向，找出根因并制定最小影响的修复方案。"
        ),
    },
    "ui-beautify": {
        "goal": "分析 UI 美化需求，读取现有前端代码和样式文件，评估当前界面的视觉与交互问题，输出包含设计改进方向、色彩/排版/动效规范和组件优化清单的 UI 优化方案。",
        "backstory": (
            "你是一位兼具技术深度和审美品位的前端架构师，精通现代 Web UI 设计体系。"
            "你擅长审视现有界面，从视觉层次、色彩搭配、间距节奏、动效体验等维度分析问题，"
            "并使用 read_code_tool 和 list_files_tool 深入了解现有 CSS/HTML/JS 结构，"
            "制定渐进式的 UI 优化方案，确保美化改动不破坏现有功能。"
        ),
    },
}

# --- 架构师（始终参与） ---
architect_cfg = ARCHITECT_CONFIG[MODE]
# 升级和 bugfix 模式下，架构师需要读取现有代码
architect_tools = [fetch_requirement_tool]
if MODE in ("upgrade", "bugfix", "ui-beautify"):
    architect_tools.extend([read_code_tool, list_files_tool])

architect = Agent(
    role='首席系统架构师',
    goal=architect_cfg["goal"],
    backstory=architect_cfg["backstory"],
    verbose=True,
    allow_delegation=False,
    tools=architect_tools,
    llm=llm_reasoning,
)

# --- UI/UX 设计师的差异化配置 ---
UI_DESIGNER_CONFIG = {
    "feature": {
        "goal": "基于架构师的需求，设计全局 UI 规范、界面布局结构以及交互流程（输出 Markdown 描述）。",
        "backstory": (
            '你精通 B 端复杂工程系统和工具链界面的设计。'
            '你深谙用户体验，擅长规划包含复杂参数配置面板等视图容器交互逻辑，'
            '能为前端开发提供清晰的组件划分建议。'
        ),
    },
    "ui-beautify": {
        "goal": "基于架构师的 UI 优化方案和现有界面分析，设计详细的视觉升级规范，包括配色方案、字体排版、间距系统、圆角阴影、动效曲线和组件样式改造细节。",
        "backstory": (
            '你是一位拥有极致审美追求的 UI 视觉设计师，精通现代设计趋势和 CSS 实现技巧。'
            '你擅长将粗糙的界面改造为精致、专业的产品级 UI，'
            '深谙色彩心理学、视觉层次理论和微交互设计，'
            '能输出前端工程师可直接落地的详细视觉规范（包含具体的颜色值、尺寸、动画参数等）。'
        ),
    },
}

# --- UI/UX 设计师（在 feature 模式 + 前端范围时参与，或在 ui-beautify 模式时始终参与） ---
ui_designer = None
if (MODE == "feature" and needs_frontend) or MODE == "ui-beautify":
    designer_cfg = UI_DESIGNER_CONFIG.get(MODE, UI_DESIGNER_CONFIG["feature"])
    ui_designer = Agent(
        role='UI/UX 交互设计师',
        goal=designer_cfg["goal"],
        backstory=designer_cfg["backstory"],
        verbose=True,
        allow_delegation=False,
        llm=llm_reasoning,
    )

# --- 各模式下前端/后端工程师的差异化配置 ---
DEV_CONFIG = {
    "feature": {
        "frontend_goal": "严格遵循 UI 规范和 API 契约，编写高质量的现代前端代码。你必须使用 write_code_tool 工具将每个代码文件写入磁盘，禁止仅输出文字描述。",
        "frontend_backstory": (
            "你精通现代 Web 技术栈。你不仅擅长构建响应式的组件库，还拥有丰富的浏览器端渲染经验，"
            "能够从容应对包含复杂 DOM 结构或200行+大表的高性能前端开发需求。"
            "你的工作方式是：先构思代码，然后立即调用 write_code_tool 将完整代码写入文件。"
        ),
        "backend_goal": "基于架构师的设计，编写健壮的后端服务、核心算法逻辑以及自动化解析脚本。你必须使用 write_code_tool 工具将每个代码文件写入磁盘，禁止仅输出文字描述。",
        "backend_backstory": (
            "你是一个极其严谨的后端极客，精通高并发架构。你擅长使用 Java 和 Python 开发后端服务。"
            "你编写的代码逻辑清晰且包含完善的异常处理。"
            "你的工作方式是：先构思代码，然后立即调用 write_code_tool 将完整代码写入文件。"
        ),
    },
    "upgrade": {
        "frontend_goal": "基于架构师的升级方案，使用 read_code_tool 读取现有前端代码，按迁移清单逐文件修改。你必须使用 write_code_tool 将修改后的完整文件写入磁盘。",
        "frontend_backstory": (
            "你精通前端依赖管理和框架版本迁移。你擅长处理 npm 依赖升级、API 兼容性适配和 breaking changes 修复。"
            "你的工作方式是：先用 read_code_tool 读取现有文件，理解当前代码，然后按升级方案修改，"
            "最后用 write_code_tool 写入修改后的完整文件。"
        ),
        "backend_goal": "基于架构师的升级方案，使用 read_code_tool 读取现有后端代码，按迁移清单逐文件修改。你必须使用 write_code_tool 将修改后的完整文件写入磁盘。",
        "backend_backstory": (
            "你精通 Java/Spring Boot 版本迁移和依赖升级。你擅长处理 Maven 依赖变更、API 废弃替换（如 javax→jakarta）、"
            "配置文件格式迁移等工作。你的工作方式是：先用 read_code_tool 读取现有文件，理解当前代码，"
            "然后按升级方案修改，最后用 write_code_tool 写入修改后的完整文件。"
        ),
    },
    "bugfix": {
        "frontend_goal": "基于架构师的诊断报告，使用 read_code_tool 定位前端 Bug 代码，编写修复补丁。你必须使用 write_code_tool 将修复后的完整文件写入磁盘。",
        "frontend_backstory": (
            "你是一位擅长排查前端 Bug 的高级工程师。你精通浏览器调试、DOM 操作和异步逻辑排障。"
            "你的工作方式是：先用 read_code_tool 读取问题文件，定位 Bug 根因，"
            "然后编写最小化修复代码，最后用 write_code_tool 写入修复后的完整文件。"
        ),
        "backend_goal": "基于架构师的诊断报告，使用 read_code_tool 定位后端 Bug 代码，编写修复补丁。你必须使用 write_code_tool 将修复后的完整文件写入磁盘。",
        "backend_backstory": (
            "你是一位擅长排查后端 Bug 的高级工程师。你精通 Java 异常追踪、SQL 调试和并发问题诊断。"
            "你的工作方式是：先用 read_code_tool 读取问题文件，定位 Bug 根因，"
            "然后编写最小化修复代码，最后用 write_code_tool 写入修复后的完整文件。"
        ),
    },
    "ui-beautify": {
        "frontend_goal": (
            "基于 UI 设计师的视觉升级规范，使用 read_code_tool 读取现有前端代码（HTML/CSS/JS），"
            "对界面进行增量美化改造。你必须使用 write_code_tool 将修改后的完整文件写入磁盘。\n"
            "重点关注：CSS 样式优化（配色、排版、间距、阴影、圆角）、HTML 结构微调（语义化标签、无障碍属性）、"
            "JS 交互增强（过渡动画、微交互效果、加载状态优化）。\n"
            "【重要】保持现有功能逻辑不变，仅做视觉和交互层面的增量改进。"
        ),
        "frontend_backstory": (
            "你是一位对像素级细节有极致追求的前端视觉工程师，精通 CSS3 动画、现代布局技术和响应式设计。"
            "你擅长将设计稿精准还原为代码，对色彩、间距、字体有敏锐的感知力。"
            "你的工作方式是：先用 read_code_tool 读取现有样式和页面文件，理解当前视觉状态，"
            "然后按照设计师的视觉规范进行增量修改，最后用 write_code_tool 写入修改后的完整文件。"
        ),
        "backend_goal": "",
        "backend_backstory": "",
    },
}

dev_cfg = DEV_CONFIG[MODE]

# 升级、bugfix 和 ui-beautify 模式下，开发人员需要读取现有代码
dev_tools = [write_code_tool]
if MODE in ("upgrade", "bugfix", "ui-beautify"):
    dev_tools = [read_code_tool, list_files_tool, write_code_tool]

# --- 前端工程师（根据影响范围决定是否参与） ---
frontend_dev = None
if needs_frontend:
    frontend_dev = Agent(
        role='高级前端工程师',
        goal=dev_cfg["frontend_goal"],
        backstory=dev_cfg["frontend_backstory"],
        verbose=True,
        tools=dev_tools,
        allow_delegation=False,
        llm=llm_coding,
    )

# --- 后端工程师（根据影响范围决定是否参与） ---
backend_dev = None
if needs_backend:
    backend_dev = Agent(
        role='高级后端工程师',
        goal=dev_cfg["backend_goal"],
        backstory=dev_cfg["backend_backstory"],
        verbose=True,
        tools=dev_tools,
        allow_delegation=False,
        llm=llm_coding,
    )

# --- QA 工程师的差异化配置 ---
QA_CONFIG = {
    "feature": {
        "goal": "审查前后端生成的代码，编写并完善单元测试和接口集成测试脚本。你必须使用 write_code_tool 工具将每个测试文件写入磁盘，禁止仅输出文字描述。",
        "backstory": (
            '你拥有"破坏者"的思维，对代码缺陷有着敏锐的嗅觉。你精通现代测试框架，'
            '致力于通过高覆盖率的测试用例（尤其是针对核心算法和数据解析模块）确保代码的健壮性和边界异常处理能力。'
            '你的工作方式是：先构思测试用例，然后立即调用 write_code_tool 将完整测试代码写入文件。'
        ),
    },
    "upgrade": {
        "goal": "针对升级变更编写回归测试，确保升级后原有功能不受影响。使用 read_code_tool 读取已有测试，补充升级相关的兼容性测试用例。你必须使用 write_code_tool 将测试文件写入磁盘。",
        "backstory": (
            '你是一位专注于版本兼容性验证的测试专家。你擅长分析升级变更的影响面，'
            '编写针对性的回归测试用例，确保 API 契约不变、数据格式兼容、边界行为一致。'
            '你的工作方式是：先用 read_code_tool 读取已有测试，理解当前测试覆盖范围，'
            '然后补充升级相关的回归测试，最后用 write_code_tool 写入测试文件。'
        ),
    },
    "bugfix": {
        "goal": "针对 Bug 修复编写回归测试，确保 Bug 不会复现。使用 read_code_tool 读取已有测试，补充复现 Bug 的测试用例。你必须使用 write_code_tool 将测试文件写入磁盘。",
        "backstory": (
            '你是一位擅长编写 Bug 复现用例的测试专家。你会根据 Bug 描述编写能精确复现问题的测试用例，'
            '确保修复后 Bug 不会回归。你的工作方式是：先用 read_code_tool 读取已有测试，'
            '然后编写针对 Bug 的回归测试，最后用 write_code_tool 写入测试文件。'
        ),
    },
    "ui-beautify": {
        "goal": (
            "针对 UI 美化变更编写视觉回归和交互测试，确保样式改动不破坏现有功能和布局。"
            "使用 read_code_tool 读取已有测试和修改后的前端文件，验证 HTML 结构完整性和 CSS 类名一致性。"
            "你必须使用 write_code_tool 将测试文件写入磁盘。"
        ),
        "backstory": (
            '你是一位专注于前端视觉质量保证的测试专家。你擅长编写针对 UI 变更的回归测试，'
            '验证样式修改不会导致布局错乱、元素溢出或交互失效。'
            '你的工作方式是：先用 read_code_tool 读取已有测试和修改后的前端文件，'
            '然后编写针对 UI 变更的视觉和交互测试，最后用 write_code_tool 写入测试文件。'
        ),
    },
}

qa_cfg = QA_CONFIG[MODE]
qa_tools = [write_code_tool]
if MODE in ("upgrade", "bugfix", "ui-beautify"):
    qa_tools = [read_code_tool, list_files_tool, write_code_tool]

qa_engineer = Agent(
    role='自动化测试工程师 (SDET)',
    goal=qa_cfg["goal"],
    backstory=qa_cfg["backstory"],
    verbose=True,
    tools=qa_tools,
    allow_delegation=False,
    llm=llm_coding,
)

# --- DevOps 工程师（始终参与） ---
devops_engineer = Agent(
    role='DevOps 与发布工程师',
    goal='在代码写入完成后，将工作区提交到远端并创建 Pull Request。',
    backstory='你是 CI/CD 流水线的大师，熟练掌握 Git 工作流和 GitHub API，确保代码从开发环境平滑过渡到版本库，为最终的人工 Review 做好准备。',
    verbose=True,
    tools=[create_pr_tool],
    allow_delegation=False,
    llm=llm_light,
)


# ==========================================
# 3. 模式感知的 Task 定义
# ==========================================

# --- 各模式下架构任务的差异化描述 ---
TASK_ARCH_DESC = {
    "feature": (
        '调用 fetch_requirement_tool 获取原始 Issue 需求。'
        '基于需求，输出详细的架构设计，包括目录结构、API 接口定义以及数据库/数据结构设计。'
    ),
    "upgrade": (
        '调用 fetch_requirement_tool 获取升级需求描述。\n'
        '然后使用 list_files_tool 查看现有项目结构，使用 read_code_tool 读取关键配置文件（如 pom.xml、package.json 等）。\n'
        '基于现有代码分析和升级需求，输出详细的升级方案，包括：\n'
        '1. 当前版本与目标版本的差异分析\n'
        '2. 受影响的文件清单\n'
        '3. 破坏性变更（breaking changes）及其应对策略\n'
        '4. 分步迁移计划'
    ),
    "bugfix": (
        '调用 fetch_requirement_tool 获取 Bug 描述。\n'
        '然后使用 list_files_tool 和 read_code_tool 查看和分析相关源码文件。\n'
        '基于 Bug 现象和代码分析，输出诊断报告，包括：\n'
        '1. Bug 根因分析\n'
        '2. 受影响的文件和代码位置\n'
        '3. 推荐的修复方案\n'
        '4. 需要关注的回归风险'
    ),
    "ui-beautify": (
        '调用 fetch_requirement_tool 获取 UI 美化需求描述。\n'
        '然后使用 list_files_tool 查看 frontend/ 目录结构，使用 read_code_tool 读取现有 HTML、CSS、JS 文件。\n'
        '基于现有界面代码和美化需求，输出 UI 优化方案，包括：\n'
        '1. 当前界面视觉问题诊断（配色、排版、间距、一致性等）\n'
        '2. 需要修改的文件清单及优化方向\n'
        '3. 设计改进规范（色彩体系、字体层级、间距系统、圆角/阴影规范）\n'
        '4. 交互增强建议（过渡动画、悬停效果、加载状态等）\n'
        '5. 分步实施计划（确保增量改动不破坏现有功能）'
    ),
}

task_architecture = Task(
    description=TASK_ARCH_DESC[MODE],
    expected_output={
        "feature": "一份结构清晰的系统架构设计文档。",
        "upgrade": "一份包含版本差异分析、受影响文件清单和分步迁移计划的升级方案文档。",
        "bugfix": "一份包含根因分析、修复方案和回归风险评估的 Bug 诊断报告。",
        "ui-beautify": "一份包含视觉问题诊断、改进规范、文件变更清单和分步实施计划的 UI 优化方案文档。",
    }[MODE],
    agent=architect,
    tools=architect_tools,
)

# --- UI 设计任务（仅 feature 模式 + 前端） ---
task_ui_design = None
if ui_designer is not None:
    task_ui_design = Task(
        description='读取架构师的架构设计文档。为该系统设计交互流程和界面布局，详细描述各个组件的功能、位置。',
        expected_output='一份详细的前端 UI 布局与交互规范文档。',
        agent=ui_designer,
        context=[task_architecture],
    )

# --- 前端任务（根据影响范围） ---
TASK_FRONTEND_DESC = {
    "feature": (
        '基于 UI 规范和架构 API 契约，编写完整的前端页面或组件代码（HTML/CSS/JS 或对应的框架代码）。\n'
        '【重要】你必须对每个代码文件调用 write_code_tool 工具来写入磁盘。\n'
        '调用示例：write_code_tool(file_path="frontend/index.html", code="<!DOCTYPE html>...")\n'
        '请为每个文件分别调用一次 write_code_tool，将代码写入 `frontend/` 目录下。\n'
        '不要只描述你将要做什么——你必须实际执行工具调用来写入文件。'
    ),
    "upgrade": (
        '基于架构师的升级方案，对前端代码执行迁移。\n'
        '1. 先用 list_files_tool 查看 frontend/ 目录结构\n'
        '2. 用 read_code_tool 逐一读取需要修改的文件\n'
        '3. 按升级方案修改代码（更新依赖引用、适配新 API、修复废弃用法等）\n'
        '4. 用 write_code_tool 将修改后的完整文件写入磁盘\n'
        '【重要】每个修改的文件都必须通过 write_code_tool 写入完整内容。'
    ),
    "bugfix": (
        '基于架构师的 Bug 诊断报告，修复前端代码中的缺陷。\n'
        '1. 用 read_code_tool 读取诊断报告中指出的问题文件\n'
        '2. 定位并修复 Bug（保持最小化变更原则）\n'
        '3. 用 write_code_tool 将修复后的完整文件写入磁盘\n'
        '【重要】每个修改的文件都必须通过 write_code_tool 写入完整内容。'
    ),
    "ui-beautify": (
        '基于 UI 设计师的视觉升级规范，对前端代码进行增量美化改造。\n'
        '1. 先用 list_files_tool 查看 frontend/ 目录结构\n'
        '2. 用 read_code_tool 逐一读取需要修改的 HTML、CSS、JS 文件\n'
        '3. 按照视觉规范修改样式（配色、排版、间距、圆角、阴影等）\n'
        '4. 增强交互体验（过渡动画、悬停效果、加载状态动画等）\n'
        '5. 优化 HTML 结构（语义化标签、无障碍属性等）\n'
        '6. 用 write_code_tool 将修改后的完整文件写入磁盘\n'
        '【重要】保持现有功能逻辑完全不变，仅做视觉和交互层面的增量改进。\n'
        '每个修改的文件都必须通过 write_code_tool 写入完整内容。'
    ),
}

task_frontend = None
if frontend_dev is not None:
    # 前端任务的上下文：feature 模式依赖 UI 设计，其他模式依赖架构分析
    frontend_context = [task_ui_design] if task_ui_design else [task_architecture]
    task_frontend = Task(
        description=TASK_FRONTEND_DESC[MODE],
        expected_output='已通过 write_code_tool 将所有前端代码文件写入 frontend/ 目录，每个文件都返回了 "Successfully wrote code to ..." 的确认信息。',
        agent=frontend_dev,
        context=frontend_context,
    )

# --- 后端任务（根据影响范围） ---
TASK_BACKEND_DESC = {
    "feature": (
        '基于架构设计文档，编写完整的后端 API 逻辑、数据解析脚本或核心算法。\n'
        '【重要】你必须对每个代码文件调用 write_code_tool 工具来写入磁盘。\n'
        '调用示例：write_code_tool(file_path="backend/app.py", code="from flask import Flask...")\n'
        '请为每个文件分别调用一次 write_code_tool，将代码写入 `backend/` 目录下。\n'
        '不要只描述你将要做什么——你必须实际执行工具调用来写入文件。'
    ),
    "upgrade": (
        '基于架构师的升级方案，对后端代码执行迁移。\n'
        '1. 先用 list_files_tool 查看 backend/ 目录结构\n'
        '2. 用 read_code_tool 逐一读取需要修改的文件（pom.xml、Java 源码、配置文件等）\n'
        '3. 按升级方案修改代码（更新依赖版本、替换废弃 API、调整配置格式等）\n'
        '4. 用 write_code_tool 将修改后的完整文件写入磁盘\n'
        '【重要】每个修改的文件都必须通过 write_code_tool 写入完整内容。'
    ),
    "bugfix": (
        '基于架构师的 Bug 诊断报告，修复后端代码中的缺陷。\n'
        '1. 用 read_code_tool 读取诊断报告中指出的问题文件\n'
        '2. 定位并修复 Bug（保持最小化变更原则）\n'
        '3. 用 write_code_tool 将修复后的完整文件写入磁盘\n'
        '【重要】每个修改的文件都必须通过 write_code_tool 写入完整内容。'
    ),
    "ui-beautify": (
        '（UI 美化模式不涉及后端变更。）'
    ),
}

task_backend = None
if backend_dev is not None:
    task_backend = Task(
        description=TASK_BACKEND_DESC[MODE],
        expected_output='已通过 write_code_tool 将所有后端代码文件写入 backend/ 目录，每个文件都返回了 "Successfully wrote code to ..." 的确认信息。',
        agent=backend_dev,
        context=[task_architecture],
    )

# --- QA 任务（始终参与，但上下文根据范围调整） ---
TASK_QA_DESC = {
    "feature": (
        '审查前端和后端生成的代码。针对后端的解析逻辑或核心 API，以及前端的关键组件，编写自动化测试用例。\n'
        '【重要】你必须对每个测试文件调用 write_code_tool 工具来写入磁盘。\n'
        '调用示例：write_code_tool(file_path="tests/test_api.py", code="import pytest...")\n'
        '请为每个文件分别调用一次 write_code_tool，将测试代码写入 `tests/` 目录下。\n'
        '不要只描述你将要做什么——你必须实际执行工具调用来写入文件。'
    ),
    "upgrade": (
        '针对本次升级变更编写回归测试。\n'
        '1. 先用 list_files_tool 查看 tests/ 目录结构，用 read_code_tool 读取已有测试\n'
        '2. 分析升级变更可能影响的功能点\n'
        '3. 编写回归测试用例，确保升级后原有功能正常\n'
        '4. 用 write_code_tool 将测试文件写入 tests/ 目录\n'
        '【重要】每个测试文件都必须通过 write_code_tool 写入磁盘。'
    ),
    "bugfix": (
        '针对本次 Bug 修复编写回归测试。\n'
        '1. 先用 read_code_tool 读取已有测试，了解现有覆盖范围\n'
        '2. 编写能精确复现原始 Bug 的测试用例\n'
        '3. 确保修复后测试能通过\n'
        '4. 用 write_code_tool 将测试文件写入 tests/ 目录\n'
        '【重要】每个测试文件都必须通过 write_code_tool 写入磁盘。'
    ),
    "ui-beautify": (
        '针对本次 UI 美化变更编写视觉回归测试。\n'
        '1. 先用 read_code_tool 读取已有测试和修改后的前端文件\n'
        '2. 验证修改后的 HTML 结构完整性（关键元素存在、id/class 一致）\n'
        '3. 验证 CSS 文件语法正确、关键样式规则存在\n'
        '4. 编写交互逻辑回归测试，确保美化改动不影响现有功能\n'
        '5. 用 write_code_tool 将测试文件写入 tests/ 目录\n'
        '【重要】每个测试文件都必须通过 write_code_tool 写入磁盘。'
    ),
}

# QA 的上下文：收集所有实际执行的开发任务
qa_context = []
if task_backend is not None:
    qa_context.append(task_backend)
if task_frontend is not None:
    qa_context.append(task_frontend)
# 如果没有开发任务（理论上不会发生），至少依赖架构分析
if not qa_context:
    qa_context.append(task_architecture)

task_qa = Task(
    description=TASK_QA_DESC[MODE],
    expected_output='已通过 write_code_tool 将所有测试代码文件写入 tests/ 目录，每个文件都返回了 "Successfully wrote code to ..." 的确认信息。',
    agent=qa_engineer,
    context=qa_context,
)

# --- DevOps 任务（始终参与） ---
task_devops = Task(
    description='确认所有代码（frontend, backend, tests）都已写入完毕后，调用 create_pr_tool 创建一个新的 Git 分支（如 feature/ai-auto-dev）并提交 Pull Request。',
    expected_output='成功创建 GitHub Pull Request 的 URL。',
    agent=devops_engineer,
    context=[task_qa],
)


# ==========================================
# 4. 动态组装 Crew 并执行
# ==========================================

# 按照执行顺序组装 agents 和 tasks
agents = [architect]
tasks = [task_architecture]

if ui_designer is not None:
    agents.append(ui_designer)
    tasks.append(task_ui_design)

if frontend_dev is not None:
    agents.append(frontend_dev)
    tasks.append(task_frontend)

if backend_dev is not None:
    agents.append(backend_dev)
    tasks.append(task_backend)

agents.append(qa_engineer)
tasks.append(task_qa)

agents.append(devops_engineer)
tasks.append(task_devops)

software_factory = Crew(
    agents=agents,
    tasks=tasks,
    process=Process.sequential,  # 严格按照瀑布流顺序执行
)

MODE_LABELS = {"feature": "新功能", "upgrade": "依赖升级", "bugfix": "Bug修复", "ui-beautify": "UI美化"}
SCOPE_LABELS = {"frontend": "前端", "backend": "后端", "fullstack": "全栈"}

if __name__ == "__main__":
    print(f"🚀 启动 AI 研发团队 [{MODE_LABELS[MODE]}模式 | {SCOPE_LABELS[SCOPE]}范围]")
    print(f"   参与的 Agent: {', '.join(a.role for a in agents)}")
    try:
        result = software_factory.kickoff()
        print("\n✅ 流水线执行完毕！最终报告：")
        print(result)
    except Exception as e:
        print(f"\n❌ 执行过程中出现异常: {str(e)}")
        raise
