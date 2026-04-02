# 数据处理脚本

本目录包含用于数据导入、导出和批量处理的 Python 脚本。

## 脚本列表

### 1. data_parser.py
基础数据解析和导入/导出工具。

**功能**:
- 从 CSV 文件导入数据到系统
- 从系统导出数据到 CSV 文件
- 数据标准化和验证

**使用方法**:
```bash
# 安装依赖
pip install -r requirements.txt

# 导入数据
python data_parser.py import sample_data.csv

# 指定 API URL
python data_parser.py import sample_data.csv --api-url http://localhost:8080/api

# 导出数据
python data_parser.py export output.csv
```

### 2. batch_processor.py
高级批量处理工具，支持大规模数据导入。

**功能**:
- 批量数据处理（可配置批次大小）
- 数据校验
- 失败重试机制（指数退避）
- 错误记录保存
- 详细日志记录

**使用方法**:
```bash
# 处理文件
python batch_processor.py sample_data.csv

# 指定 API URL
python batch_processor.py sample_data.csv http://localhost:8080/api
```

**特性**:
- 自动重试（默认 3 次）
- 批量处理（默认每批 50 条）
- 生成详细日志文件
- 保存失败记录到 `failed_records.json`
- 保存无效数据到 `invalid_data_*.json`

### 3. sample_data.csv
示例数据文件，用于测试导入功能。

## CSV 文件格式

CSV 文件应包含以下列:

| 列名 | 说明 | 必填 |
|------|------|------|
| code | 模块代码 | 是 |
| status | 状态 | 是 |
| vendor | 供应商 | 否 |
| process_status | 处理状态 | 否 |
| ld | LD 信息 | 否 |
| pd | PD 信息 | 否 |
| remarks | 备注 | 否 |

**示例**:
```csv
code,status,vendor,process_status,ld,pd,remarks
MOD-2024-001,Active,Vendor A,In Production,LD-001,PD-001,测试模块
MOD-2024-002,Testing,Vendor B,Quality Check,LD-002,PD-002,另一个测试
```

## 依赖项

```
requests==2.31.0
```

安装依赖:
```bash
pip install -r requirements.txt
```

## 日志文件

批量处理脚本会生成日志文件，命名格式:
- `batch_import_YYYYMMDD_HHMMSS.log`

日志包含:
- 处理进度
- 成功/失败记录
- 错误详情
- 性能统计

## 错误处理

### 失败记录
处理失败的记录会保存到 `failed_records.json`，可以修复后重新导入。

### 无效数据
校验失败的数据会保存到 `invalid_data_*.json`，包含错误原因。

## 最佳实践

1. **大文件处理**: 使用 `batch_processor.py` 而不是 `data_parser.py`
2. **数据验证**: 导入前先检查 CSV 格式
3. **错误恢复**: 检查日志和失败记录文件，修复后重新导入
4. **备份数据**: 导入前先导出现有数据作为备份

## 注意事项

1. 确保后端服务已启动
2. 确保 CSV 文件编码为 UTF-8
3. code 字段必须唯一
4. 大量数据导入时建议在非高峰期进行
5. 监控服务器资源使用情况
