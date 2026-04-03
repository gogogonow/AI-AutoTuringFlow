import os
import re
import subprocess
from crewai.tools import tool


# ==========================================================
# 文件写入工具
# ==========================================================

@tool("Write Code to File")
def write_code_tool(file_path: str, code: str) -> str:
    """将代码写入指定文件（创建新文件或完全重写）。目录不存在会自动创建。"""
    dir_name = os.path.dirname(file_path)
    if dir_name:
        os.makedirs(dir_name, exist_ok=True)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)
    return f"Successfully wrote code to {file_path}"


@tool("Patch Code in File")
def patch_code_tool(file_path: str, search_string: str, replace_string: str) -> str:
    """在已有文件中精确搜索 search_string 并替换为 replace_string（增量编辑，节省 token）。
    search_string 必须与文件中的内容完全匹配（包括缩进和换行）。
    要求 search_string 在文件中恰好出现一次：
      - 若匹配次数为 0，返回错误提示，请用 read_code_tool 重新确认代码片段再重试。
      - 若匹配次数超过 1，返回错误提示，请提供更长、更精确的代码片段。
    用于 upgrade/bugfix 场景的增量修改。"""
    if not os.path.isfile(file_path):
        return f"Error: File not found: {file_path}"
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        count = content.count(search_string)
        if count == 0:
            return (
                f"❌ Error: search_string not found in {file_path}. "
                "Please use read_code_tool to verify the exact code snippet (including indentation and line breaks) and retry."
            )
        if count > 1:
            return (
                f"❌ Error: search_string matched {count} locations in {file_path}. "
                "Please provide a longer, more unique code snippet to avoid ambiguous replacement."
            )
        new_content = content.replace(search_string, replace_string)
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        return f"✅ Successfully patched {file_path}"
    except Exception as e:
        return f"Error patching {file_path}: {e}"


# ==========================================================
# 文件读取工具
# ==========================================================

@tool("Read Code from File")
def read_code_tool(file_path: str, max_lines: int = 0) -> str:
    """读取指定文件内容。可设置 max_lines 限制返回行数（0=全部），避免大文件浪费 token。"""
    if not os.path.isfile(file_path):
        return f"Error: File not found: {file_path}"
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            if max_lines > 0:
                lines = []
                for i, line in enumerate(f):
                    if i >= max_lines:
                        lines.append(f"\n... (truncated after {max_lines} lines)")
                        break
                    lines.append(line)
                content = "".join(lines)
            else:
                content = f.read()
        return f"=== Content of {file_path} ===\n{content}"
    except Exception as e:
        return f"Error reading {file_path}: {e}"


@tool("Search Code in Directory")
def search_code_tool(directory: str, pattern: str) -> str:
    """在目录中递归搜索包含指定文本的文件，返回匹配的文件路径和行号。
    用于快速定位代码位置，避免盲目读取大量文件。最多返回 30 条结果。"""
    if not os.path.isdir(directory):
        return f"Error: Directory not found: {directory}"

    # 跳过的目录和二进制扩展名
    skip_dirs = {'.git', 'node_modules', '__pycache__', 'target', '.idea', 'dist', 'build'}
    binary_exts = {'.class', '.jar', '.png', '.jpg', '.gif', '.ico', '.woff', '.ttf', '.pdf', '.zip', '.gz'}

    matches = []
    max_results = 30
    try:
        compiled = re.compile(pattern, re.IGNORECASE)
    except re.error:
        compiled = re.compile(re.escape(pattern), re.IGNORECASE)

    for root, dirs, files in os.walk(directory):
        dirs[:] = [d for d in dirs if d not in skip_dirs and not d.startswith('.')]
        for fname in sorted(files):
            if fname.startswith('.'):
                continue
            if os.path.splitext(fname)[1].lower() in binary_exts:
                continue
            fpath = os.path.join(root, fname)
            try:
                with open(fpath, "r", encoding="utf-8", errors="ignore") as f:
                    for lineno, line in enumerate(f, 1):
                        if compiled.search(line):
                            rel = os.path.relpath(fpath, directory)
                            matches.append(f"  {rel}:{lineno}: {line.rstrip()[:120]}")
                            if len(matches) >= max_results:
                                matches.append(f"  ... (stopped at {max_results} results)")
                                return f"=== Search '{pattern}' in {directory} ===\n" + "\n".join(matches)
            except (OSError, UnicodeDecodeError):
                continue

    if not matches:
        return f"No matches found for '{pattern}' in {directory}"
    return f"=== Search '{pattern}' in {directory} ===\n" + "\n".join(matches)


# ==========================================================
# 目录浏览工具
# ==========================================================

@tool("List Files in Directory")
def list_files_tool(directory: str) -> str:
    """列出指定目录下的所有文件和子目录（递归，最多3层），用于了解代码库结构。"""
    if not os.path.isdir(directory):
        return f"Error: Directory not found: {directory}"

    results = []
    base_depth = directory.rstrip(os.sep).count(os.sep)
    for root, dirs, files in os.walk(directory):
        current_depth = root.count(os.sep) - base_depth
        if current_depth >= 3:
            dirs.clear()
            continue
        dirs[:] = sorted(d for d in dirs if not d.startswith('.'))
        indent = "  " * current_depth
        rel_root = os.path.relpath(root, directory)
        if rel_root == ".":
            rel_root = directory
        results.append(f"{indent}{rel_root}/")
        for f in sorted(files):
            if not f.startswith('.'):
                results.append(f"{indent}  {f}")

    return "\n".join(results) if results else f"Directory {directory} is empty."


# ==========================================================
# 沙盒命令执行工具（Read-Evaluate-Execute 闭环）
# ==========================================================

# 允许执行的安全命令前缀白名单（仅限测试/只读命令）
_ALLOWED_CMD_PREFIXES = (
    "pytest", "python -m pytest",
    "npm test", "npm run test", "npm run lint",
    "mvn test", "mvn verify",
    "eslint", "flake8", "mypy",
)


@tool("Execute Command")
def execute_command_tool(command: str) -> str:
    """在沙盒中执行只读或测试 shell 命令（如 pytest、npm test、mvn test）并返回 stdout 和 stderr。
    仅允许测试/检查类命令，超时时间为 30 秒。
    可用于实现"写代码 → 运行测试 → 根据错误自动修复"的 Read-Evaluate-Execute 闭环。
    不得用于写入文件、删除文件或修改系统状态的命令。"""
    import shlex
    stripped = command.strip()
    if not any(stripped.startswith(prefix) for prefix in _ALLOWED_CMD_PREFIXES):
        allowed = ", ".join(f"`{p}`" for p in _ALLOWED_CMD_PREFIXES)
        return (
            f"❌ Command not allowed: '{stripped}'. "
            f"Only the following command prefixes are permitted: {allowed}"
        )
    # Reject shell metacharacters to prevent injection via ; && | ` $( etc.
    for meta in (';', '&&', '||', '|', '`', '$('):
        if meta in stripped:
            return (
                f"❌ Command not allowed: shell metacharacter '{meta}' detected. "
                "Provide a single test command without chaining operators."
            )
    try:
        args = shlex.split(stripped)
    except ValueError as e:
        return f"❌ Invalid command syntax: {e}"
    try:
        proc = subprocess.run(
            args,
            shell=False,
            capture_output=True,
            text=True,
            timeout=30,
        )
        stdout = proc.stdout.strip() if proc.stdout else ""
        stderr = proc.stderr.strip() if proc.stderr else ""
        exit_code = proc.returncode
        result_parts = [f"exit_code: {exit_code}"]
        if stdout:
            result_parts.append(f"stdout:\n{stdout}")
        if stderr:
            result_parts.append(f"stderr:\n{stderr}")
        return "\n".join(result_parts)
    except subprocess.TimeoutExpired:
        return "❌ Command timed out after 30 seconds."
    except Exception as e:
        return f"❌ Error executing command: {e}"
