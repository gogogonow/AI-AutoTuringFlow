import os
from dotenv import load_dotenv
from crewai import Agent, Task, Crew, Process, LLM

from tools.github_tools import (
    fetch_requirement_tool, create_pr_direct, parse_issue_config,
)
from tools.file_tools import (
    write_code_tool, read_code_tool, list_files_tool,
    patch_code_tool, search_code_tool,
)

# 加载本地 .env 文件（如果是本地调试的话）
load_dotenv()

# ==========================================
# 0. 初始化底层大模型 (接入 OAIPro)
# ==========================================
# 优化：仅保留 2 个 LLM 层级（去掉 llm_light，DevOps 已去 Agent 化）
# 所有模型统一通过 LiteLLM + openai/ 前缀路由到 OAIPro 兼容端点。
print("正在连接大模型神经中枢...")

oaipro_key = os.environ.get("OAIPRO_API_KEY", "")

llm_reasoning = LLM(
    # 架构设计与 Review：使用 o4-mini 进行深度推理（通过 OAIPro OpenAI 兼容接口）
    # 相比 gpt-3.5-turbo 大幅提升推理和结构化输出能力
    model="openai/o4-mini",
    max_tokens=8192,
    api_key=oaipro_key,
    base_url="https://api.oaipro.com/v1",
    is_litellm=True,
)

llm_coding = LLM(
    # 代码生成：使用 Claude Sonnet 4.5 进行高质量代码编写
    # 必须使用 openai/ 前缀，anthropic/ 会导致工具调用被静默丢弃
    model="openai/claude-sonnet-4-5-20250929",
    max_tokens=8192,
    api_key=oaipro_key,
    base_url="https://api.oaipro.com/v1",
    is_litellm=True,
)


# ==========================================
# 1. 解析 Issue 配置（模式 + 影响范围）
# ==========================================

config = parse_issue_config()
MODE = config["mode"]       # "upgrade" | "feature" | "bugfix" | "ui-beautify"
SCOPE = config["scope"]     # "frontend" | "backend" | "fullstack"
ISSUE_TITLE = config["title"]

print(f"📋 任务模式: {MODE} | 影响范围: {SCOPE}")

needs_frontend = SCOPE in ("frontend", "fullstack")
needs_backend = SCOPE in ("backend", "fullstack")


# ==========================================
# 2. 精简的 Agent 定义（优化：去除 QA 和 DevOps Agent）
# ==========================================
# Agent 精简为：架构师 + [UI设计师] + [前端] + [后端] + Review
# - QA 职责合并到前端/后端开发 Agent（同时写代码和测试）
# - DevOps 任务改为 Crew 结束后直接 Python 调用（无需 LLM）

# --- 架构师配置（精简 backstory，强化结构化输出指令） ---
ARCHITECT_CONFIG = {
    "feature": {
        "goal": (
            "分析需求，输出结构化架构设计：API 契约（路径、方法、请求/响应字段）、"
            "数据库表结构、目录规划和文件清单。"
        ),
        "backstory": "精通系统架构设计，擅长定义前后端 API 契约和数据模型，输出可直接执行的技术方案。",
    },
    "upgrade": {
        "goal": (
            "分析升级需求，读取现有代码和依赖配置，输出结构化的变更规约："
            "受影响文件清单、每个文件的具体修改内容、文件间一致性约束、分步迁移计划。"
        ),
        "backstory": "精通依赖管理和版本迁移，擅长分析 breaking changes 并制定安全升级路径。",
    },
    "bugfix": {
        "goal": (
            "分析 Bug 描述，读取相关源码定位根因，输出结构化诊断报告："
            "根因分析、受影响文件和代码位置、修复方案、回归风险。"
        ),
        "backstory": "精通系统排障，擅长从 Bug 报告快速定位问题代码并制定最小影响修复方案。",
    },
    "ui-beautify": {
        "goal": (
            "分析 UI 美化需求，读取现有前端代码，输出 UI 优化方案："
            "视觉问题诊断、设计改进规范（色彩/排版/间距/动效）、文件变更清单。"
        ),
        "backstory": "兼具技术深度和审美品位的前端架构师，精通现代 Web UI 设计体系。",
    },
}

architect_cfg = ARCHITECT_CONFIG[MODE]
architect_tools = [fetch_requirement_tool]
if MODE in ("upgrade", "bugfix", "ui-beautify"):
    architect_tools.extend([read_code_tool, list_files_tool, search_code_tool])

architect = Agent(
    role='首席系统架构师',
    goal=architect_cfg["goal"],
    backstory=architect_cfg["backstory"],
    verbose=True,
    allow_delegation=False,
    tools=architect_tools,
    llm=llm_reasoning,
)

# --- UI/UX 设计师（feature+前端 或 ui-beautify 时参与） ---
UI_DESIGNER_CONFIG = {
    "feature": {
        "goal": "基于架构设计，输出 UI 布局规范：组件划分、交互流程、界面结构。",
        "backstory": "精通 B 端界面设计，擅长规划复杂交互逻辑和组件结构。",
    },
    "ui-beautify": {
        "goal": "基于架构师的 UI 分析，输出详细视觉升级规范：配色方案、字体排版、间距系统、动效参数。",
        "backstory": "追求极致审美的 UI 设计师，能输出工程师可直接落地的视觉规范。",
    },
}

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

# --- 前端/后端工程师配置（优化：合并 QA 职责，使用 patch_code_tool） ---
# 工具集：feature 模式用 write；upgrade/bugfix/ui-beautify 增加 read/list/patch/search
dev_tools_feature = [write_code_tool]
dev_tools_modify = [
    read_code_tool, list_files_tool, search_code_tool,
    patch_code_tool, write_code_tool,
]

DEV_CONFIG = {
    "feature": {
        "frontend_goal": (
            "遵循 UI 规范和 API 契约，编写前端代码并编写对应的单元测试。"
            "使用 write_code_tool 写入 frontend/ 和 tests/ 目录。"
        ),
        "frontend_backstory": "精通现代 Web 技术栈，擅长构建高性能响应式前端，同时编写测试确保代码质量。",
        "backend_goal": (
            "基于架构设计，编写后端 API、数据模型和业务逻辑，并编写对应的单元测试。"
            "使用 write_code_tool 写入 backend/ 和 tests/ 目录。"
            "严格按照以下顺序处理文件：数据库DDL/migration → Entity模型 → Repository → Service → Controller → DTO。"
        ),
        "backend_backstory": "精通 Java/Spring Boot 开发，编写严谨的后端代码并同步编写测试用例。",
    },
    "upgrade": {
        "frontend_goal": (
            "按架构师的变更规约，对前端代码执行增量迁移。\n"
            "优先使用 patch_code_tool 做增量修改（仅输出变更部分），仅在创建新文件时使用 write_code_tool。\n"
            "同时编写回归测试到 tests/ 目录确保升级不破坏功能。"
        ),
        "frontend_backstory": "精通前端依赖升级和框架迁移，擅长增量修改代码。",
        "backend_goal": (
            "按架构师的变更规约，对后端代码执行增量迁移。\n"
            "严格按照以下顺序处理：pom.xml依赖 → 数据库DDL → Entity → Repository → Service → Controller → 配置文件。\n"
            "优先使用 patch_code_tool 做增量修改，仅在创建新文件时使用 write_code_tool。\n"
            "同时编写回归测试到 tests/ 目录确保升级不破坏功能。"
        ),
        "backend_backstory": "精通 Java/Spring Boot 版本迁移，擅长处理 Maven 依赖变更和 API 废弃替换。",
    },
    "bugfix": {
        "frontend_goal": (
            "按架构师的诊断报告，修复前端 Bug。\n"
            "使用 search_code_tool 定位问题，用 patch_code_tool 做最小化修复。\n"
            "同时编写回归测试到 tests/ 目录确保 Bug 不复现。"
        ),
        "frontend_backstory": "擅长前端 Bug 排查和精确修复，同时编写回归测试。",
        "backend_goal": (
            "按架构师的诊断报告，修复后端 Bug。\n"
            "使用 search_code_tool 定位问题，用 patch_code_tool 做最小化修复。\n"
            "同时编写回归测试到 tests/ 目录确保 Bug 不复现。"
        ),
        "backend_backstory": "擅长后端 Bug 排查、Java 异常追踪和并发问题诊断。",
    },
    "ui-beautify": {
        "frontend_goal": (
            "按 UI 设计师的视觉规范，对前端代码做增量美化改造。\n"
            "使用 patch_code_tool 修改 CSS 样式和 HTML 结构，保持功能逻辑不变。\n"
            "同时编写视觉回归测试到 tests/ 目录。"
        ),
        "frontend_backstory": "对像素级细节有极致追求的前端视觉工程师，精通 CSS3 和现代布局技术。",
        "backend_goal": "",
        "backend_backstory": "",
    },
}

dev_cfg = DEV_CONFIG[MODE]
dev_tools = dev_tools_modify if MODE in ("upgrade", "bugfix", "ui-beautify") else dev_tools_feature

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

# --- Review Agent（新增：一致性校验，替代原 QA + 新增交叉验证） ---
REVIEW_CONFIG = {
    "feature": {
        "goal": (
            "审查所有生成的代码文件，验证一致性：\n"
            "1. 后端 Entity 字段与 Controller DTO 是否一一对应\n"
            "2. 前端 API 调用路径/参数是否与后端端点匹配\n"
            "3. 数据库 DDL（如有）是否与 Entity 注解一致\n"
            "4. 是否存在重复文件或遗漏文件\n"
            "发现问题时使用 patch_code_tool 直接修复。"
        ),
    },
    "upgrade": {
        "goal": (
            "审查所有升级变更，验证一致性：\n"
            "1. 架构师变更规约中的每个条目是否都已执行\n"
            "2. Entity 字段与 Controller/DTO 是否一致\n"
            "3. 依赖版本是否在所有配置文件中保持一致\n"
            "4. 是否存在遗漏的 import 更新或 API 调用替换\n"
            "发现问题时使用 patch_code_tool 直接修复。"
        ),
    },
    "bugfix": {
        "goal": (
            "审查 Bug 修复代码，验证：\n"
            "1. 修复是否完整覆盖了所有受影响的代码路径\n"
            "2. 修复是否引入了新的不一致\n"
            "3. 相关联的文件是否都已同步更新\n"
            "发现问题时使用 patch_code_tool 直接修复。"
        ),
    },
    "ui-beautify": {
        "goal": (
            "审查 UI 美化变更，验证：\n"
            "1. 功能逻辑代码未被意外修改\n"
            "2. CSS 类名和 id 引用在 HTML/CSS/JS 中保持一致\n"
            "3. HTML 结构完整性未被破坏\n"
            "发现问题时使用 patch_code_tool 直接修复。"
        ),
    },
}

review_tools = [read_code_tool, list_files_tool, search_code_tool, patch_code_tool]

reviewer = Agent(
    role='代码审查与一致性校验工程师',
    goal=REVIEW_CONFIG[MODE]["goal"],
    backstory="资深代码审查专家，擅长发现文件间的不一致、遗漏和潜在缺陷，并直接修复。",
    verbose=True,
    tools=review_tools,
    allow_delegation=False,
    llm=llm_coding,
)


# ==========================================
# 3. 模式感知的 Task 定义（优化：结构化输出 + 合并测试）
# ==========================================

# --- 架构任务（强化结构化输出要求） ---
TASK_ARCH_DESC = {
    "feature": (
        '调用 fetch_requirement_tool 获取 Issue 需求。\n'
        '输出结构化架构设计，必须包含以下章节：\n'
        '## API 契约\n列出每个接口的 HTTP 方法、路径、请求参数、响应格式\n'
        '## 数据库设计\n列出表名、字段名、类型、约束\n'
        '## 文件清单\n列出需要创建/修改的每个文件路径及其职责\n'
        '## 一致性约束\n列出文件间必须保持一致的规则（如 Entity 字段必须与 DTO 一一对应）'
    ),
    "upgrade": (
        '调用 fetch_requirement_tool 获取升级需求。\n'
        '使用 list_files_tool 和 read_code_tool 分析现有代码。\n'
        '输出结构化变更规约，必须包含以下章节：\n'
        '## 版本差异分析\n当前版本 vs 目标版本的 breaking changes\n'
        '## 变更清单\n逐文件列出：文件路径、修改类型（modify/create/delete）、具体修改内容\n'
        '## 一致性约束\n列出变更后必须保持一致的规则\n'
        '## 迁移步骤\n按依赖顺序排列的执行步骤'
    ),
    "bugfix": (
        '调用 fetch_requirement_tool 获取 Bug 描述。\n'
        '使用 list_files_tool、search_code_tool 和 read_code_tool 定位问题。\n'
        '输出结构化诊断报告，必须包含以下章节：\n'
        '## 根因分析\n问题的根本原因\n'
        '## 受影响文件\n逐文件列出：文件路径、问题代码位置、修复方案\n'
        '## 一致性约束\n修复后需要保持一致的规则\n'
        '## 回归风险\n需要关注的副作用'
    ),
    "ui-beautify": (
        '调用 fetch_requirement_tool 获取 UI 美化需求。\n'
        '使用 list_files_tool 查看 frontend/ 结构，用 read_code_tool 读取现有文件。\n'
        '输出 UI 优化方案：\n'
        '## 视觉问题诊断\n当前界面的具体问题\n'
        '## 变更清单\n逐文件列出修改内容\n'
        '## 设计规范\n色彩、排版、间距、动效的具体参数值\n'
        '## 一致性约束\n美化改动不能破坏的功能逻辑'
    ),
}

TASK_ARCH_OUTPUT = {
    "feature": "包含 API 契约、数据库设计、文件清单和一致性约束的结构化架构文档。",
    "upgrade": "包含版本差异、逐文件变更清单、一致性约束和迁移步骤的结构化变更规约。",
    "bugfix": "包含根因分析、受影响文件清单、修复方案和回归风险的结构化诊断报告。",
    "ui-beautify": "包含视觉诊断、变更清单、设计规范和一致性约束的 UI 优化方案。",
}

task_architecture = Task(
    description=TASK_ARCH_DESC[MODE],
    expected_output=TASK_ARCH_OUTPUT[MODE],
    agent=architect,
    tools=architect_tools,
)

# --- UI 设计任务 ---
task_ui_design = None
if ui_designer is not None:
    task_ui_design = Task(
        description='基于架构设计，输出 UI 布局规范和交互流程，详细描述组件划分和视觉参数。',
        expected_output='前端 UI 布局与交互规范文档。',
        agent=ui_designer,
        context=[task_architecture],
    )

# --- 前端任务（合并了原 QA 的测试编写职责） ---
TASK_FRONTEND_DESC = {
    "feature": (
        '基于 UI 规范和 API 契约编写前端代码，同时编写对应的测试用例。\n'
        '使用 write_code_tool 将代码写入 frontend/ 目录，测试写入 tests/ 目录。\n'
        '每个文件必须通过 write_code_tool 实际写入。'
    ),
    "upgrade": (
        '按架构师的变更规约，逐条执行前端迁移：\n'
        '1. 用 search_code_tool 定位需修改的代码\n'
        '2. 用 read_code_tool 读取目标文件\n'
        '3. 用 patch_code_tool 做增量修改（仅输出变更部分，节省 token）\n'
        '4. 新文件用 write_code_tool 创建\n'
        '5. 编写回归测试到 tests/ 目录\n'
        '完成后检查变更规约中的每个前端条目是否都已处理。'
    ),
    "bugfix": (
        '按诊断报告修复前端 Bug：\n'
        '1. 用 search_code_tool 定位问题代码\n'
        '2. 用 patch_code_tool 做最小化修复\n'
        '3. 编写回归测试到 tests/ 目录'
    ),
    "ui-beautify": (
        '按视觉规范对前端做增量美化：\n'
        '1. 用 read_code_tool 读取现有文件\n'
        '2. 用 patch_code_tool 修改样式（配色、排版、间距等）\n'
        '3. 保持功能逻辑不变\n'
        '4. 编写视觉回归测试到 tests/ 目录'
    ),
}

task_frontend = None
if frontend_dev is not None:
    frontend_context = [task_ui_design] if task_ui_design else [task_architecture]
    task_frontend = Task(
        description=TASK_FRONTEND_DESC[MODE],
        expected_output='已将前端代码和测试文件写入磁盘，每个文件返回了确认信息。',
        agent=frontend_dev,
        context=frontend_context,
    )

# --- 后端任务（合并了原 QA 的测试编写职责） ---
TASK_BACKEND_DESC = {
    "feature": (
        '基于架构设计编写后端代码和测试。\n'
        '严格按顺序处理：数据库DDL → Entity → Repository → Service → Controller → DTO。\n'
        '使用 write_code_tool 将代码写入 backend/ 目录，测试写入 tests/ 目录。\n'
        '每个文件必须通过 write_code_tool 实际写入。'
    ),
    "upgrade": (
        '按架构师的变更规约，逐条执行后端迁移：\n'
        '严格按顺序处理：pom.xml → 数据库DDL → Entity → Repository → Service → Controller → 配置文件。\n'
        '1. 用 search_code_tool 定位需修改的代码\n'
        '2. 用 read_code_tool 读取目标文件\n'
        '3. 用 patch_code_tool 做增量修改（仅输出变更部分，节省 token）\n'
        '4. 新文件用 write_code_tool 创建\n'
        '5. 编写回归测试到 tests/ 目录\n'
        '完成后检查变更规约中的每个后端条目是否都已处理。'
    ),
    "bugfix": (
        '按诊断报告修复后端 Bug：\n'
        '1. 用 search_code_tool 定位问题代码\n'
        '2. 用 patch_code_tool 做最小化修复\n'
        '3. 编写回归测试到 tests/ 目录'
    ),
    "ui-beautify": '（UI 美化模式不涉及后端变更。）',
}

task_backend = None
if backend_dev is not None:
    task_backend = Task(
        description=TASK_BACKEND_DESC[MODE],
        expected_output='已将后端代码和测试文件写入磁盘，每个文件返回了确认信息。',
        agent=backend_dev,
        context=[task_architecture],
    )

# --- Review 任务（新增：一致性校验，替代原 QA 的代码审查 + 新增交叉验证） ---
TASK_REVIEW_DESC = {
    "feature": (
        '审查所有已生成的代码，执行一致性校验：\n'
        '1. 用 list_files_tool 扫描 frontend/、backend/、tests/ 目录\n'
        '2. 用 read_code_tool 读取关键文件\n'
        '3. 验证：Entity↔DTO 字段一致、前端 API 调用↔后端端点匹配、无重复/遗漏文件\n'
        '4. 发现不一致时用 patch_code_tool 直接修复'
    ),
    "upgrade": (
        '审查升级变更的完整性和一致性：\n'
        '1. 对照架构师的变更规约逐条检查\n'
        '2. 用 search_code_tool 搜索可能遗漏的旧 API/旧依赖引用\n'
        '3. 验证 Entity↔DTO↔DDL 一致性\n'
        '4. 发现遗漏或不一致时用 patch_code_tool 修复'
    ),
    "bugfix": (
        '审查 Bug 修复的完整性：\n'
        '1. 验证修复覆盖了所有受影响代码路径\n'
        '2. 用 search_code_tool 搜索是否有类似问题的其他位置\n'
        '3. 检查关联文件的一致性\n'
        '4. 发现问题时用 patch_code_tool 修复'
    ),
    "ui-beautify": (
        '审查 UI 美化变更：\n'
        '1. 验证功能逻辑代码未被修改\n'
        '2. 检查 CSS 类名在 HTML/CSS/JS 中的引用一致性\n'
        '3. 验证 HTML 结构完整性\n'
        '4. 发现问题时用 patch_code_tool 修复'
    ),
}

# Review 的上下文：收集所有开发任务的输出
review_context = []
if task_frontend is not None:
    review_context.append(task_frontend)
if task_backend is not None:
    review_context.append(task_backend)
if not review_context:
    review_context.append(task_architecture)

task_review = Task(
    description=TASK_REVIEW_DESC[MODE],
    expected_output='一致性校验报告：列出检查项及结果，已发现的问题已通过 patch_code_tool 修复。',
    agent=reviewer,
    context=review_context,
)


# ==========================================
# 4. 动态组装 Crew 并执行 + 直接创建 PR（去 Agent 化）
# ==========================================

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

agents.append(reviewer)
tasks.append(task_review)

software_factory = Crew(
    agents=agents,
    tasks=tasks,
    process=Process.sequential,
)

MODE_LABELS = {"feature": "新功能", "upgrade": "依赖升级", "bugfix": "Bug修复", "ui-beautify": "UI美化"}
SCOPE_LABELS = {"frontend": "前端", "backend": "后端", "fullstack": "全栈"}

if __name__ == "__main__":
    print(f"🚀 启动 AI 研发团队 [{MODE_LABELS[MODE]}模式 | {SCOPE_LABELS[SCOPE]}范围]")
    print(f"   参与的 Agent: {', '.join(a.role for a in agents)}")

    try:
        result = software_factory.kickoff()
        print("\n✅ Crew 执行完毕！最终报告：")
        print(result)
    except Exception as e:
        print(f"\n❌ Crew 执行异常: {str(e)}")
        raise

    # --- DevOps 去 Agent 化：直接 Python 调用创建 PR（无需 LLM 推理） ---
    issue_num = int(os.environ.get("ISSUE_NUMBER", "0"))
    branch_name = f"{MODE}/ai-issue-{issue_num}"
    pr_title = f"[AI-{MODE}] #{issue_num}: {ISSUE_TITLE}"
    commit_msg = f"[AI-{MODE}] Auto-generated changes for issue #{issue_num}"

    print(f"\n📦 正在创建 PR: {pr_title}")
    try:
        pr_url = create_pr_direct(branch_name, pr_title, commit_msg)
        print(f"\n✅ {pr_url}")
    except Exception as e:
        print(f"\n❌ PR 创建失败: {str(e)}")
        raise
