# 架构设计方案：单模块详情新增多厂家信息

## 1. 任务概述

**需求**：在光模块详情页面新增多厂家信息展示功能，每个模块可关联多个厂家记录，包含厂家名称、流程状态、时间信息、技术参数、测试数据等详细信息。

**模式**：feature（新功能）

**影响范围**：fullstack（全栈）

**核心目标**：
- 支持一个光模块关联多个厂家信息记录
- 在模块详情页展示所有厂家信息列表
- 提供厂家信息的增删改查功能
- 支持测试报告链接跳转

---

## 2. 影响分析

### 2.1 数据模型变更
- 新增 `VendorInfo` 实体（一对多关联到 `Module`）
- `Module` 实体保持不变，通过 JPA `@OneToMany` 关联

### 2.2 前端变更
- `ModuleDetails.js` 组件需新增厂家信息列表展示区域
- 新增 `VendorInfoForm.js` 组件处理厂家信息的增删改

### 2.3 后端变更
- 新增 `VendorInfo` Entity、DTO、Repository、Service、Controller
- 新增 API 端点用于厂家信息的 CRUD 操作

### 2.4 不影响现有功能
- 现有光模块的基本信息管理功能不受影响
- 操作历史记录功能保持独立

---

## 3. API 契约

### 3.1 查询模块的所有厂家信息

**端点**：`GET /api/modules/{moduleId}/vendor-info`

**路径参数**：
- `moduleId`（Long）：光模块 ID

**请求示例**：
```
GET /api/modules/123/vendor-info
```

**响应格式**（200 OK）：
```json
{
  "content": [
    {
      "id": 1,
      "moduleId": 123,
      "vendorName": "华为",
      "processStatus": "IN_PROGRESS",
      "entryTime": "2024-01-15T10:30:00Z",
      "exitTime": null,
      "ld": "LD-001",
      "pd": "PD-002",
      "laLdo": "LA-LDO-003",
      "tia": "TIA-004",
      "mcu": "MCU-005",
      "pcnChangePoint": "版本升级至 V2.1",
      "highSpeedTestRecommended": true,
      "acquisition": "已获取",
      "eyeDiagramData": "Eye diagram: 0.3UI",
      "coveredBoards": "板卡A, 板卡B",
      "testReportUrl": "https://example.com/reports/test-123.pdf",
      "remark": "重点关注高温测试",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-16T08:20:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### 3.2 创建厂家信息

**端点**：`POST /api/modules/{moduleId}/vendor-info`

**路径参数**：
- `moduleId`（Long）：光模块 ID

**请求体**：
```json
{
  "vendorName": "华为",
  "processStatus": "IN_PROGRESS",
  "entryTime": "2024-01-15T10:30:00Z",
  "exitTime": null,
  "ld": "LD-001",
  "pd": "PD-002",
  "laLdo": "LA-LDO-003",
  "tia": "TIA-004",
  "mcu": "MCU-005",
  "pcnChangePoint": "版本升级至 V2.1",
  "highSpeedTestRecommended": true,
  "acquisition": "已获取",
  "eyeDiagramData": "Eye diagram: 0.3UI",
  "coveredBoards": "板卡A, 板卡B",
  "testReportUrl": "https://example.com/reports/test-123.pdf",
  "remark": "重点关注高温测试"
}
```

**字段说明**：
- `vendorName`（String，必填）：厂家名称
- `processStatus`（String，必填）：流程状态枚举（见 3.6 节）
- `entryTime`（String，必填）：进入时间（ISO 8601 格式）
- `exitTime`（String，可选）：退出时间
- `ld`（String，可选）：LD 参数
- `pd`（String，可选）：PD 参数
- `laLdo`（String，可选）：LA+LDO 参数
- `tia`（String，可选）：TIA 参数
- `mcu`（String，可选）：MCU 参数
- `pcnChangePoint`（String，可选）：PCN 变更点
- `highSpeedTestRecommended`（Boolean，默认 false）：是否建议高速重点测试
- `acquisition`（String，可选）：获取性
- `eyeDiagramData`（String，可选）：电眼数据
- `coveredBoards`（String，可选）：已覆盖单板（逗号分隔）
- `testReportUrl`（String，可选）：测试报告链接
- `remark`（String，可选）：备注

**响应格式**（201 Created）：
```json
{
  "id": 1,
  "moduleId": 123,
  "vendorName": "华为",
  ...（同查询响应）
}
```

**错误响应**（400 Bad Request）：
```json
{
  "error": "字段校验失败",
  "code": "VALIDATION_ERROR",
  "details": [
    "vendorName: 厂家名称不能为空",
    "processStatus: 流程状态不能为空"
  ]
}
```

### 3.3 更新厂家信息

**端点**：`PUT /api/modules/{moduleId}/vendor-info/{vendorInfoId}`

**路径参数**：
- `moduleId`（Long）：光模块 ID
- `vendorInfoId`（Long）：厂家信息 ID

**请求体**：同 3.2 节

**响应格式**（200 OK）：同查询响应

**错误响应**（404 Not Found）：
```json
{
  "error": "厂家信息不存在",
  "code": "VENDOR_INFO_NOT_FOUND"
}
```

### 3.4 删除厂家信息

**端点**：`DELETE /api/modules/{moduleId}/vendor-info/{vendorInfoId}`

**路径参数**：
- `moduleId`（Long）：光模块 ID
- `vendorInfoId`（Long）：厂家信息 ID

**响应格式**（204 No Content）：无响应体

**错误响应**（404 Not Found）：同 3.3 节

### 3.5 获取单个厂家信息详情

**端点**：`GET /api/modules/{moduleId}/vendor-info/{vendorInfoId}`

**路径参数**：
- `moduleId`（Long）：光模块 ID
- `vendorInfoId`（Long）：厂家信息 ID

**响应格式**（200 OK）：
```json
{
  "id": 1,
  "moduleId": 123,
  "vendorName": "华为",
  ...（完整字段）
}
```

### 3.6 流程状态枚举（Process Status）

```
PENDING         → 待处理
IN_PROGRESS     → 进行中
TESTING         → 测试中
COMPLETED       → 已完成
SUSPENDED       → 已暂停
CANCELLED       → 已取消
```

---

## 4. 数据库设计

### 4.1 新增表：vendor_info

**表名**：`vendor_info`

**字段列表**：

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| `module_id` | BIGINT | NOT NULL, FOREIGN KEY → module(id) | 关联光模块 |
| `vendor_name` | VARCHAR(255) | NOT NULL | 厂家名称 |
| `process_status` | VARCHAR(50) | NOT NULL | 流程状态（枚举） |
| `entry_time` | TIMESTAMP | NOT NULL | 进入时间 |
| `exit_time` | TIMESTAMP | NULL | 退出时间 |
| `ld` | VARCHAR(100) | NULL | LD 参数 |
| `pd` | VARCHAR(100) | NULL | PD 参数 |
| `la_ldo` | VARCHAR(100) | NULL | LA+LDO 参数 |
| `tia` | VARCHAR(100) | NULL | TIA 参数 |
| `mcu` | VARCHAR(100) | NULL | MCU 参数 |
| `pcn_change_point` | TEXT | NULL | PCN 变更点 |
| `high_speed_test_recommended` | BOOLEAN | DEFAULT FALSE | 是否建议高速重点测试 |
| `acquisition` | VARCHAR(255) | NULL | 获取性 |
| `eye_diagram_data` | TEXT | NULL | 电眼数据 |
| `covered_boards` | TEXT | NULL | 已覆盖单板（逗号分隔） |
| `test_report_url` | VARCHAR(500) | NULL | 测试报告链接 |
| `remark` | TEXT | NULL | 备注 |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- `idx_module_id` ON `module_id`（加速查询某模块的所有厂家信息）
- `idx_vendor_name` ON `vendor_name`（支持按厂家名称筛选）
- `idx_process_status` ON `process_status`（支持按流程状态筛选）

**外键约束**：
```sql
FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE
```

### 4.2 现有表无需修改

`module` 表和 `history` 表保持不变，通过 JPA 的 `@OneToMany` 注解实现关联关系。

---

## 5. 文件清单

### 5.1 后端文件（backend/）

#### 新增文件

1. **`src/main/java/com/example/backend/model/VendorInfo.java`**
   - 职责：JPA 实体，映射 `vendor_info` 表
   - 关键注解：`@Entity`, `@Table(name="vendor_info")`, `@ManyToOne(fetch=LAZY)` 关联 `Module`
   - 使用 `jakarta.persistence.*`（不使用 `javax.persistence.*`）

2. **`src/main/java/com/example/backend/dto/VendorInfoDto.java`**
   - 职责：API 响应 DTO
   - 字段：与 `VendorInfo` 实体一一对应（camelCase 命名）

3. **`src/main/java/com/example/backend/dto/VendorInfoCreateRequest.java`**
   - 职责：创建厂家信息的请求 DTO
   - 包含 Bean Validation 注解（`@NotBlank`, `@NotNull`）

4. **`src/main/java/com/example/backend/dto/VendorInfoUpdateRequest.java`**
   - 职责：更新厂家信息的请求 DTO
   - 字段同 `VendorInfoCreateRequest`

5. **`src/main/java/com/example/backend/repository/VendorInfoRepository.java`**
   - 职责：数据访问层，继承 `JpaRepository<VendorInfo, Long>`
   - 自定义查询方法：`List<VendorInfo> findByModuleId(Long moduleId)`

6. **`src/main/java/com/example/backend/service/VendorInfoService.java`**
   - 职责：业务逻辑层接口
   - 方法：
     - `List<VendorInfoDto> getVendorInfoByModuleId(Long moduleId)`
     - `VendorInfoDto createVendorInfo(Long moduleId, VendorInfoCreateRequest request)`
     - `VendorInfoDto updateVendorInfo(Long moduleId, Long vendorInfoId, VendorInfoUpdateRequest request)`
     - `void deleteVendorInfo(Long moduleId, Long vendorInfoId)`
     - `VendorInfoDto getVendorInfoById(Long moduleId, Long vendorInfoId)`

7. **`src/main/java/com/example/backend/service/impl/VendorInfoServiceImpl.java`**
   - 职责：业务逻辑实现
   - 依赖注入：`VendorInfoRepository`, `ModuleRepository`
   - 业务规则：
     - 创建/更新时校验 `moduleId` 是否存在
     - 删除/更新时校验 `vendorInfoId` 是否存在且属于指定模块
     - Entity ↔ DTO 转换逻辑

8. **`src/main/java/com/example/backend/controller/VendorInfoController.java`**
   - 职责：REST API 控制器
   - 路径前缀：`/api/modules/{moduleId}/vendor-info`
   - 端点：
     - `GET /` → `getVendorInfoList()`
     - `POST /` → `createVendorInfo()`
     - `GET /{vendorInfoId}` → `getVendorInfo()`
     - `PUT /{vendorInfoId}` → `updateVendorInfo()`
     - `DELETE /{vendorInfoId}` → `deleteVendorInfo()`
   - 使用 `@Valid` 校验请求体

9. **`src/main/java/com/example/backend/model/ProcessStatus.java`**
   - 职责：流程状态枚举
   - 枚举值：`PENDING`, `IN_PROGRESS`, `TESTING`, `COMPLETED`, `SUSPENDED`, `CANCELLED`

#### 修改文件

10. **`src/main/java/com/example/backend/model/Module.java`**
    - 修改点：新增 `@OneToMany` 关联到 `VendorInfo`
    - 代码示例：
      ```java
      @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
      private List<VendorInfo> vendorInfoList = new ArrayList<>();
      ```

#### 测试文件

11. **`src/test/java/com/example/backend/service/VendorInfoServiceTest.java`**
    - 职责：Service 层单元测试

12. **`src/test/java/com/example/backend/controller/VendorInfoControllerTest.java`**
    - 职责：Controller 层集成测试（使用 `@WebMvcTest`）

### 5.2 前端文件（frontend/）

#### 新增文件

13. **`frontend/js/components/VendorInfoForm.js`**
    - 职责：厂家信息表单组件（新增/编辑）
    - 功能：
      - 表单字段映射 API 契约中的所有字段
      - 前端校验（必填、日期格式）
      - 调用 `API.createVendorInfo()` / `API.updateVendorInfo()`
    - 挂载到 `window.VendorInfoForm`

14. **`frontend/styles/vendor-info.css`**
    - 职责：厂家信息相关的样式
    - 复用 `components.css` 中的 `.card`, `.table`, `.form-group` 等基础类
    - 新增专属样式：`.vendor-info-list`, `.vendor-info-item`, `.test-report-link`

#### 修改文件

15. **`frontend/js/components/ModuleDetails.js`**
    - 修改点：
      - 在模块基本信息下方新增"厂家信息"区域
      - 调用 `API.getVendorInfoByModuleId(moduleId)` 获取数据
      - 渲染厂家信息列表（表格形式）
      - 新增"添加厂家信息"按钮，点击打开 `VendorInfoForm` 表单模态框
      - 每行厂家信息提供"编辑"和"删除"按钮
    - 使用 `Utils.formatDateTime()` 格式化时间
    - 测试报告链接使用 `<a>` 标签跳转

16. **`frontend/js/api.js`**
    - 修改点：新增以下方法
      - `API.getVendorInfoByModuleId(moduleId)`
      - `API.createVendorInfo(moduleId, data)`
      - `API.updateVendorInfo(moduleId, vendorInfoId, data)`
      - `API.deleteVendorInfo(moduleId, vendorInfoId)`
      - `API.getVendorInfo(moduleId, vendorInfoId)`

17. **`frontend/js/config.js`**
    - 修改点：新增流程状态映射
      ```javascript
      CONFIG.PROCESS_STATUS_TEXT = {
          PENDING: '待处理',
          IN_PROGRESS: '进行中',
          TESTING: '测试中',
          COMPLETED: '已完成',
          SUSPENDED: '已暂停',
          CANCELLED: '已取消'
      };
      ```

18. **`frontend/js/utils.js`**
    - 修改点：新增辅助方法（如有需要）
      - `Utils.getProcessStatusText(status)` → 返回流程状态中文文本
      - `Utils.getProcessStatusClass(status)` → 返回状态对应的 CSS class

19. **`frontend/index.html`**
    - 修改点：在 `<head>` 中引入 `vendor-info.css`
    - 修改点：在 `<body>` 底部引入 `VendorInfoForm.js`（在 `app.js` 之前）

20. **`frontend/styles/components.css`**
    - 修改点：新增流程状态徽章样式
      ```css
      .status-badge.status-pending { ... }
      .status-badge.status-in_progress { ... }
      .status-badge.status-testing { ... }
      .status-badge.status-completed { ... }
      .status-badge.status-suspended { ... }
      .status-badge.status-cancelled { ... }
      ```

---

## 6. 一致性约束

### 6.1 Entity ↔ DTO 字段一致性

| Entity 字段（Java） | DTO 字段（JSON） | 类型 | 说明 |
|-------------------|-----------------|------|------|
| `id` | `id` | Long | 主键 |
| `moduleId` | `moduleId` | Long | 关联模块 ID |
| `vendorName` | `vendorName` | String | 厂家名称 |
| `processStatus` | `processStatus` | String | 流程状态枚举 |
| `entryTime` | `entryTime` | String(ISO 8601) | 进入时间 |
| `exitTime` | `exitTime` | String(ISO 8601) | 退出时间 |
| `ld` | `ld` | String | LD 参数 |
| `pd` | `pd` | String | PD 参数 |
| `laLdo` | `laLdo` | String | LA+LDO 参数 |
| `tia` | `tia` | String | TIA 参数 |
| `mcu` | `mcu` | String | MCU 参数 |
| `pcnChangePoint` | `pcnChangePoint` | String | PCN 变更点 |
| `highSpeedTestRecommended` | `highSpeedTestRecommended` | Boolean | 是否建议高速重点测试 |
| `acquisition` | `acquisition` | String | 获取性 |
| `eyeDiagramData` | `eyeDiagramData` | String | 电眼数据 |
| `coveredBoards` | `coveredBoards` | String | 已覆盖单板 |
| `testReportUrl` | `testReportUrl` | String | 测试报告链接 |
| `remark` | `remark` | String | 备注 |
| `createdAt` | `createdAt` | String(ISO 8601) | 创建时间 |
| `updatedAt` | `updatedAt` | String(ISO 8601) | 更新时间 |

**约束规则**：
- Entity 字段名使用 camelCase（Java 约定）
- DTO 字段名使用 camelCase（Jackson 默认序列化）
- 数据库列名使用 snake_case（JPA `@Column(name="xxx")` 映射）
- 时间字段在 Entity 中用 `java.time.Instant`，在 DTO 中序列化为 ISO 8601 字符串

### 6.2 前端 ↔ 后端 API 字段一致性

- 前端 `api.js` 中的请求体字段名必须与后端 DTO 完全一致（camelCase）
- 前端解析响应时，字段名必须与后端 DTO 完全一致
- 枚举值字符串（如 `processStatus`）前后端必须完全一致（大写下划线格式）

### 6.3 流程状态枚举一致性

**后端**（`ProcessStatus.java`）：
```java
public enum ProcessStatus {
    PENDING,
    IN_PROGRESS,
    TESTING,
    COMPLETED,
    SUSPENDED,
    CANCELLED
}
```

**前端**（`config.js`）：
```javascript
CONFIG.PROCESS_STATUS_TEXT = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    TESTING: '测试中',
    COMPLETED: '已完成',
    SUSPENDED: '已暂停',
    CANCELLED: '已取消'
};
```

### 6.4 数据库列名 ↔ Entity 字段映射

所有 Entity 字段必须通过 `@Column(name="xxx")` 明确映射到数据库列名（snake_case），示例：

```java
@Column(name = "vendor_name", nullable = false)
private String vendorName;

@Column(name = "process_status", nullable = false)
@Enumerated(EnumType.STRING)
private ProcessStatus processStatus;

@Column(name = "la_ldo")
private String laLdo;

@Column(name = "high_speed_test_recommended")
private Boolean highSpeedTestRecommended = false;
```

### 6.5 外键关联约束

- `VendorInfo.module` 必须通过 `@ManyToOne` 关联到 `Module`
- `Module.vendorInfoList` 必须通过 `@OneToMany(mappedBy="module")` 反向关联
- 数据库外键约束必须设置 `ON DELETE CASCADE`，确保删除模块时同步删除关联的厂家信息

### 6.6 前端组件复用规则

- 厂家信息表单必须复用 `components.css` 中的 `.form-group`, `.form-control`, `.btn` 等类
- 厂家信息列表必须复用 `.table-container`, `.table` 等类
- 状态徽章必须复用 `.status-badge` 并新增 `.status-pending` 等子类
- 错误状态必须使用 `Utils.renderErrorState()`
- 空数据状态必须使用 `Utils.renderEmptyState()`
- 日期格式化必须使用 `Utils.formatDateTime()`
- 流程状态文本必须使用 `Utils.getProcessStatusText(status)`（新增方法）

---

## 7. 实现指导

### 7.1 后端实现顺序

1. 创建 `ProcessStatus` 枚举类
2. 创建 `VendorInfo` Entity（先不关联 `Module`）
3. 修改 `Module` Entity，添加 `@OneToMany` 关联
4. 创建 `VendorInfoDto`、`VendorInfoCreateRequest`、`VendorInfoUpdateRequest`
5. 创建 `VendorInfoRepository`
6. 创建 `VendorInfoService` 接口和 `VendorInfoServiceImpl` 实现类
7. 创建 `VendorInfoController`
8. 编写单元测试和集成测试
9. 运行 `mvn clean test` 验证后端功能

### 7.2 前端实现顺序

1. 更新 `config.js`，添加 `PROCESS_STATUS_TEXT`
2. 更新 `utils.js`，添加 `getProcessStatusText()` 和 `getProcessStatusClass()`
3. 更新 `api.js`，添加厂家信息相关的 5 个方法
4. 创建 `VendorInfoForm.js` 组件（表单逻辑）
5. 更新 `ModuleDetails.js`，新增厂家信息列表区域
6. 创建 `vendor-info.css`，定义样式
7. 更新 `components.css`，添加流程状态徽章样式
8. 更新 `index.html`，引入新的 CSS 和 JS 文件
9. 本地测试：打开模块详情页，验证厂家信息列表、新增、编辑、删除功能

### 7.3 测试验收要点

**后端测试**：
- ✅ 创建厂家信息成功，返回 201 + 完整对象
- ✅ 创建时必填字段为空，返回 400 + 错误详情
- ✅ 查询不存在的模块 ID，返回 404
- ✅ 查询模块的厂家信息，返回正确的列表
- ✅ 更新厂家信息成功，字段正确更新
- ✅ 删除厂家信息成功，数据库记录被删除
- ✅ 删除模块时，关联的厂家信息自动删除（CASCADE）

**前端测试**：
- ✅ 打开模块详情页，正确展示"厂家信息"区域
- ✅ 列表为空时，显示"暂无厂家信息"提示
- ✅ 点击"添加厂家信息"按钮，弹出表单模态框
- ✅ 表单必填字段未填写时，显示错误提示，不允许提交
- ✅ 表单提交成功后，模态框关闭，列表自动刷新
- ✅ 点击"编辑"按钮，表单预填充现有数据
- ✅ 编辑后保存成功，列表中该行数据更新
- ✅ 点击"删除"按钮，弹出确认对话框，确认后删除成功
- ✅ 测试报告链接可点击，在新标签页打开
- ✅ 流程状态显示正确的中文文本和徽章样式
- ✅ 日期时间格式化显示正确（YYYY-MM-DD HH:mm）

### 7.4 注意事项

- **后端**：所有写操作（创建/更新/删除）必须在 Service 层加 `@Transactional` 注解
- **后端**：Entity 必须使用 `jakarta.persistence.*`，不能使用 `javax.persistence.*`
- **后端**：删除操作必须先验证 `vendorInfoId` 是否属于指定的 `moduleId`，防止越权删除
- **前端**：表单提交前必须进行前端校验，但最终校验由后端 `@Valid` 负责
- **前端**：测试报告链接必须使用 `target="_blank" rel="noopener noreferrer"` 属性
- **前端**：日期选择器建议使用 `<input type="datetime-local">` 原生控件
- **前端**：布尔字段（是否建议高速重点测试）建议使用 `<input type="checkbox">`
- **CSS**：新增样式必须写入 `vendor-info.css`，不得使用内联 `style=` 属性
- **CSS**：流程状态徽章必须复用 `.status-badge` 基础类，通过子类（如 `.status-