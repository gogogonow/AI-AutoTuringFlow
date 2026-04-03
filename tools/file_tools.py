import os
import re
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
def patch_code_tool(file_path: str, search_block: str, replace_block: str) -> str:
    """在已有文件中搜索 search_block 并替换为 replace_block（增量编辑，节省 token）。
    search_block 必须与文件中的内容完全匹配（包括缩进和换行）。
    仅替换第一次出现的匹配。用于 upgrade/bugfix 场景的增量修改。"""
    if not os.path.isfile(file_path):
        return f"Error: File not found: {file_path}"
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        if search_block not in content:
            return (
                f"Error: search_block not found in {file_path}. "
                "Ensure the search text matches exactly (including whitespace and indentation)."
            )
        new_content = content.replace(search_block, replace_block, 1)
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        return f"Successfully patched {file_path}"
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
