# 光模块管理系统 - 自动化测试文档

## 📋 测试概览

本测试套件为光模块管理系统提供全面的自动化测试覆盖，包括：
- **后端单元测试** (JUnit 5)
- **后端集成测试** (Spring Boot Test)
- **Python 脚本测试** (Pytest)
- **前端单元测试** (Jest)
- **前端组件测试** (Jest + Testing Library)

## 🎯 测试覆盖范围

### 后端测试 (Java)

#### 1. ModuleServiceTest.java
- ✅ CRUD 操作完整测试
- ✅ 异常处理和边界值测试
- ✅ 并发更新测试
- ✅ 历史记录追踪测试
- ✅ 事务管理验证

**测试用例：**
- `testGetAllModules_Success()` - 获取所有模块
- `testGetAllModules_EmptyList()` - 空列表处理
- `testGetModuleById_Success()` - 根据 ID 获取
- `testGetModuleById_NotFound()` - 404 异常处理
- `testCreateModule_Success()` - 创建模块
- `testCreateModule_NullModule()` - 空对象验证
- `testUpdateModule_Success()` - 更新模块
- `testUpdateModule_NotFound()` - 更新不存在的模块
- `testDeleteModule_Success()` - 删除模块
- `testDeleteModule_NotFound()` - 删除不存在的模块
- `testGetModuleHistory_Success()` - 获取历史记录
- `testWavelengthBoundary()` - 波长边界测试
- `testZeroTransmissionDistance()` - 零值测试
- `testConcurrentUpdate_TimestampValidation()` - 并发测试

#### 2. ModuleControllerTest.java
- ✅ REST API 端点完整测试
- ✅ HTTP 状态码验证
- ✅ JSON 序列化/反序列化
- ✅ CORS 预检请求测试
- ✅ 大数据量响应测试

**测试用例：**
- `testGetAllModules_Success()` - GET /api/modules
- `testGetModuleById_Success()` - GET /api/modules/{id}
- `testGetModuleById_NotFound()` - 404 响应
- `testCreateModule_Success()` - POST /api/modules
- `testCreateModule_InvalidBody()` - 400 验证错误
- `testUpdateModule_Success()` - PUT /api/modules/{id}
- `testDeleteModule_Success()` - DELETE /api/modules/{id}
- `testGetModuleHistory_Success()` - GET /api/modules/{id}/history
- `testCorsPreflightRequest()` - OPTIONS 请求
- `testConcurrentRequests()` - 并发请求
- `testLargeDataResponse()` - 100 条记录性能测试

### Python 测试

#### test_data_parser.py
- ✅ CSV 文件解析测试
- ✅ 数据验证和标准化
- ✅ 异常处理和错误恢复
- ✅ 大文件性能测试
- ✅ 特殊字符处理

**测试用例：**
- `test_parse_valid_csv()` - 解析有效 CSV
- `test_parse_empty_csv()` - 空文件处理
- `test_parse_nonexistent_file()` - 文件不存在
- `test_validate_complete_data()` - 数据验证
- `test_validate_missing_required_fields()` - 缺失字段
- `test_standardize_wavelength_with_unit()` - 波长标准化
- `test_standardize_invalid_wavelength()` - 无效波长
- `test_parse_power_value_with_unit()` - 功率值解析
- `test_export_to_csv()` - 导出 CSV
- `test_batch_processing_with_errors()` - 批量处理错误
- `test_special_characters_handling()` - 特殊字符
- `test_large_file_processing()` - 1000 条记录性能
- `test_boundary_values()` - 边界值测试

### 前端测试 (JavaScript)

#### test_api.js
- ✅ API 调用完整测试
- ✅ 错误处理和重试逻辑
- ✅ 工具函数测试
- ✅ 性能和超时测试

**测试用例：**
- `getAllModules()` - 获取所有模块 API
- `getModuleById()` - 获取单个模块 API
- `createModule()` - 创建模块 API
- `updateModule()` - 更新模块 API
- `deleteModule()` - 删除模块 API
- `getModuleHistory()` - 获取历史 API
- `formatDate()` - 日期格式化工具
- `showLoading()/hideLoading()` - 加载动画
- `showToast()` - 消息提示
- `confirmDelete()` - 确认对话框
- 错误处理测试（网络错误、JSON 解析错误、超时）
- 批量请求性能测试

#### test_components.js
- ✅ React 组件渲染测试
- ✅ 用户交互测试
- ✅ 表单验证测试
- ✅ 集成测试场景

**测试用例：**
- **ModuleList 组件：**
  - 渲染模块列表
  - 空状态显示
  - 删除操作（确认/取消）
  - 查看详情导航
  - 错误处理

- **ModuleForm 组件：**
  - 创建表单渲染
  - 编辑表单数据填充
  - 必填字段验证
  - 数值范围验证
  - 表单提交成功/失败

- **ModuleDetails 组件：**
  - 详情页面渲染
  - 编辑和历史按钮
  - 模块不存在处理
  - 数值格式化显示

- **集成测试：**
  - 完整用户流程：列表 -> 详情 -> 编辑 -> 列表

## 🚀 运行测试

### 环境准备

#### 后端测试环境
```bash
# 确保已安装 Java 11+ 和 Maven 3.6+
java -version
mvn -version

# 安装依赖
cd backend
mvn clean install
```

#### Python 测试环境
```bash
# 安装 Python 3.8+
python --version

# 安装依赖
pip install -r backend/scripts/requirements.txt
pip install pytest pytest-cov
```

#### 前端测试环境
```bash
# 安装 Node.js 16+ 和 npm
node -version
npm -version

# 安装测试依赖
cd tests
npm install
```

### 运行所有测试

```bash
# 在 tests/ 目录下运行
npm run test:all
```

### 分别运行各类测试

#### 1. 运行后端 Java 测试
```bash
cd backend
mvn test

# 生成测试报告
mvn test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

#### 2. 运行 Python 测试
```bash
# 运行所有 Python 测试
python -m pytest tests/backend/test_data_parser.py -v

# 生成覆盖率报告
python -m pytest tests/backend/test_data_parser.py --cov=backend/scripts --cov-report=html

# 查看覆盖率报告
open htmlcov/index.html
```

#### 3. 运行前端 Jest 测试
```bash
cd tests

# 运行所有前端测试
npm test

# 监听模式（开发时使用）
npm run test:watch

# 生成覆盖率报告
npm run test:coverage

# 查看覆盖率报告
open coverage/lcov-report/index.html
```

#### 4. 运行特定测试文件
```bash
# 运行单个 Java 测试类
mvn test -Dtest=ModuleServiceTest

# 运行单个测试方法
mvn test -Dtest=ModuleServiceTest#testCreateModule_Success

# 运行单个 Jest 测试文件
npm test test_api.js

# 运行单个 Python 测试方法
python -m pytest tests/backend/test_data_parser.py::TestDataParser::test_parse_valid_csv -v
```

## 📊 测试覆盖率目标

| 模块 | 行覆盖率 | 分支覆盖率 | 函数覆盖率 |
|------|----------|------------|------------|
| 后端 Service 层 | ≥ 85% | ≥ 80% | ≥ 85% |
| 后端 Controller 层 | ≥ 80% | ≥ 75% | ≥ 80% |
| Python 脚本 | ≥ 80% | ≥ 75% | ≥ 80% |
| 前端 API 层 | ≥ 80% | ≥ 75% | ≥ 80% |
| 前端组件 | ≥ 75% | ≥ 70% | ≥ 75% |

## 🔍 CI/CD 集成

### GitHub Actions 配置示例

```yaml
name: Automated Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Run Backend Tests
        run: |
          cd backend
          mvn clean test jacoco:report
      - name: Upload Coverage
        uses: codecov/codecov-action@v3

  python-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.9'
      - name: Install Dependencies
        run: |
          pip install -r backend/scripts/requirements.txt
          pip install pytest pytest-cov
      - name: Run Python Tests
        run: |
          python -m pytest tests/backend/test_data_parser.py --cov --cov-report=xml

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '16'
      - name: Install Dependencies
        run: |
          cd tests
          npm install
      - name: Run Frontend Tests
        run: |
          cd tests
          npm run test:coverage
```

## 🐛 调试测试

### Java 测试调试
```bash
# 在 IntelliJ IDEA 中：
# 1. 右键点击测试类/方法
# 2. 选择 "Debug 'TestName'"

# 使用 Maven 调试
mvn test -Dmaven.surefire.debug
```

### Jest 测试调试
```bash
# 在 VS Code 中添加 launch.json 配置
{
  "type": "node",
  "request": "launch",
  "name": "Jest Debug",
  "program": "${workspaceFolder}/node_modules/.bin/jest",
  "args": ["--runInBand", "--no-cache"],
  "console": "integratedTerminal",
  "internalConsoleOptions": "neverOpen"
}

# 或使用命令行
node --inspect-brk node_modules/.bin/jest --runInBand
```

### Python 测试调试
```bash
# 使用 pytest 调试模式
python -m pytest tests/backend/test_data_parser.py -v -s --pdb

# 在 VS Code 中使用断点调试
```

## 📈 性能测试

### 测试响应时间基准
- API 请求：< 200ms
- 数据库查询：< 100ms
- CSV 解析（1000 条）：< 10s
- 前端组件渲染：< 50ms

### 负载测试
```bash
# 使用 Apache JMeter 或 k6 进行负载测试
k6 run tests/load/api_load_test.js
```

## 📝 测试最佳实践

1. **遵循 AAA 模式**：Arrange（准备）、Act（执行）、Assert（断言）
2. **测试独立性**：每个测试应该独立运行，不依赖其他测试
3. **清晰命名**：测试名称应清楚描述测试内容和预期结果
4. **边界测试**：测试边界值、空值、极值
5. **异常测试**：验证错误处理和异常情况
6. **Mock 使用**：隔离外部依赖，使用 mock 对象
7. **持续更新**：代码变更时同步更新测试

## 🔧 故障排查

### 常见问题

**问题 1：Java 测试失败 - 数据库连接错误**
```
解决方案：
1. 检查 application.properties 配置
2. 确保 H2 内存数据库依赖已添加
3. 使用 @DataJpaTest 注解
```

**问题 2：Jest 测试超时**
```
解决方案：
1. 增加超时时间：jest.setTimeout(10000)
2. 检查异步操作是否正确处理
3. 使用 async/await 或 done() 回调
```

**问题 3：Python 测试导入错误**
```
解决方案：
1. 检查 PYTHONPATH 设置
2. 确保 __init__.py 文件存在
3. 使用相对导入或绝对导入
```

## 📚 参考资源

- [JUnit 5 官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Jest 官方文档](https://jestjs.io/docs/getting-started)
- [Pytest 官方文档](https://docs.pytest.org/)
- [Spring Boot Testing 指南](https://spring.io/guides/gs/testing-web/)
- [Testing Library 文档](https://testing-library.com/docs/)

## 🤝 贡献指南

添加新测试时请遵循以下步骤：

1. 为新功能编写测试用例
2. 确保所有现有测试通过
3. 运行覆盖率检查，确保达到目标
4. 更新本文档，记录新的测试用例
5. 提交 Pull Request，包含测试结果截图

---

**测试套件版本：** 1.0.0  
**最后更新：** 2024-01-15  
**维护者：** SDET Team
