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
