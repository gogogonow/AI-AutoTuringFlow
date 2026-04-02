import os
from crewai.tools import tool


@tool("Write Code to File")
def write_code_tool(file_path: str, code: str) -> str:
    """将生成的代码字符串写入指定的本地文件路径。如果目录不存在会自动创建。"""
    dir_name = os.path.dirname(file_path)
    if dir_name:
        os.makedirs(dir_name, exist_ok=True)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)
    return f"Successfully wrote code to {file_path}"


@tool("Read Code from File")
def read_code_tool(file_path: str) -> str:
    """读取指定文件的完整内容并返回。用于在修改代码之前了解现有代码结构。"""
    if not os.path.isfile(file_path):
        return f"Error: File not found: {file_path}"
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        return f"=== Content of {file_path} ===\n{content}"
    except Exception as e:
        return f"Error reading {file_path}: {e}"


@tool("List Files in Directory")
def list_files_tool(directory: str) -> str:
    """列出指定目录下的所有文件和子目录（递归，最多3层），用于了解现有代码库的结构。"""
    if not os.path.isdir(directory):
        return f"Error: Directory not found: {directory}"

    results = []
    base_depth = directory.rstrip(os.sep).count(os.sep)
    for root, dirs, files in os.walk(directory):
        current_depth = root.count(os.sep) - base_depth
        if current_depth >= 3:
            dirs.clear()
            continue
        # 跳过隐藏目录（如 .git）
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
