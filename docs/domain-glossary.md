# 领域词典（Domain Glossary）

> 本文件定义光模块管理系统中使用的所有核心业务术语、字段名映射与状态枚举。
> Multi-agent 在进行任何推理前必须先参照本词典，确保术语一致，避免歧义。

---

## 1. 核心业务实体

### 1.1 光模块（Module / Transceiver）

系统的核心管理对象，代表一个物理光模块设备。

| 中文名 | 英文名/代码名 | 说明 |
|--------|-------------|------|
| 光模块 | `module` / `transceiver` | 物理光收发器设备 |
| 序列号 | `serialNumber` / `sn` | 唯一标识符，全局唯一 |
| 型号 | `model` / `modelName` | 产品型号，如 SFP+10G-SR |
| 厂商/供应商 | `vendor` / `manufacturer` | 生产厂商名称 |
| 端口速率 | `speed` / `portSpeed` | 传输速率，如 1G / 10G / 25G / 100G |
| 波长 | `wavelength` | 工作波长，单位 nm，如 850nm / 1310nm / 1550nm |
| 传输距离 | `transmissionDistance` / `maxDistance` | 最大传输距离，单位 m 或 km |
| 接口类型 | `connectorType` | 接口形式，如 LC / SC / MPO |
| 模块状态 | `status` | 见状态枚举（1.3节） |
| 入库时间 | `inboundTime` / `createdAt` | 首次入库时间戳 |
| 备注 | `remark` / `note` | 自由文本补充说明 |

### 1.2 操作历史（History / OperationLog）

记录每一次光模块状态变更或操作事件。

| 中文名 | 英文名/代码名 | 说明 |
|--------|-------------|------|
| 历史记录 | `history` / `operationLog` | 单条操作记录 |
| 操作类型 | `operationType` / `action` | 见操作类型枚举（1.4节） |
| 操作时间 | `operationTime` / `timestamp` | 操作发生时间戳 |
| 操作人 | `operator` / `operatedBy` | 操作人用户名或 ID |
| 关联模块 | `moduleId` / `module` | 被操作的光模块 ID |
| 操作前状态 | `previousStatus` | 操作前的模块状态 |
| 操作后状态 | `nextStatus` | 操作后的模块状态 |
| 备注 | `remark` | 操作备注 |

---

## 2. 业务流程术语

| 中文名 | 英文名 | 说明 |
|--------|--------|------|
| 入库 | `inbound` / `stockIn` | 光模块首次登记进入库存 |
| 出库 | `outbound` / `stockOut` | 光模块从库存中取出（部署/报废） |
| 在库 | `inStock` / `available` | 光模块处于库存待用状态 |
| 部署 | `deployed` / `inUse` | 光模块已安装到设备端口上 |
| 故障 | `faulty` / `broken` | 光模块发生故障 |
| 维修中 | `underRepair` / `maintenance` | 光模块正在维修 |
| 报废 | `scrapped` / `retired` | 光模块已达到使用寿命，永久退出管理 |
| 兼容性检查 | `compatibilityCheck` | 验证光模块是否匹配目标设备端口 |
| 批量导入 | `batchImport` | 从 Excel/CSV 批量导入多条光模块数据 |
| 批量导出 | `batchExport` | 将库存数据导出为 Excel/CSV |

---

## 3. 状态枚举

### 3.1 模块状态（Module Status）

```
IN_STOCK      → 在库（待用）
DEPLOYED      → 已部署（在用）
FAULTY        → 故障
UNDER_REPAIR  → 维修中
SCRAPPED      → 已报废
```

### 3.2 操作类型枚举（Operation Type）

```
INBOUND         → 入库
OUTBOUND        → 出库
DEPLOY          → 部署
RETRIEVE        → 收回（从设备卸下回库）
MARK_FAULTY     → 标记故障
SEND_REPAIR     → 送修
RETURN_REPAIR   → 维修归还
SCRAP           → 报废
UPDATE_INFO     → 更新信息
```

---

## 4. 接口与字段命名规范

### 4.1 JSON 字段命名

- 使用 **camelCase**（小驼峰）
- 时间戳字段后缀统一用 `At` 或 `Time`（如 `createdAt`、`operationTime`）
- ID 字段统一用 `id`（主键）或 `xxxId`（外键引用）
- 布尔字段用 `is` 前缀（如 `isCompatible`）或形容词（如 `active`）

### 4.2 API 路径命名

- 使用 **kebab-case**（短横线）
- 资源名用复数（如 `/api/modules`、`/api/histories`）
- 操作使用 RESTful 动词（GET/POST/PUT/DELETE）
- 特殊动作用 `/api/modules/{id}/actions/inbound` 形式

### 4.3 Java 类命名

- 实体类：`Module`、`History`（**不加** `Entity` 后缀）
- DTO：`ModuleDto`、`ModuleCreateRequest`、`ModuleUpdateRequest`
- Controller：`ModuleController`、`HistoryController`
- Service：`ModuleService`、`HistoryService`
- Repository：`ModuleRepository`、`HistoryRepository`
- 包名：`com.example.backend.*`

### 4.4 前端变量命名

- JS 变量/函数：camelCase
- CSS 类名：kebab-case（如 `.module-status-badge`）
- HTML ID：kebab-case（如 `#module-list-table`）

---

## 5. 常见歧义消除表

以下是用户描述、前端代码、后端代码中可能出现的同义词对照，agent 在处理时应做术语归一化：

| 用户可能说 | 前端可能叫 | 后端/DB 叫 | 标准术语 |
|-----------|-----------|-----------|---------|
| 模块、设备、收发器 | `module`、`device` | `module`、`transceiver` | **光模块（Module）** |
| 序列号、SN、编号 | `sn`、`serialNo` | `serialNumber` | **序列号（serialNumber）** |
| 状态、当前状态 | `status`、`state` | `status` | **状态（status）** |
| 波长、频率 | `wavelength`、`freq` | `wavelength` | **波长（wavelength）** |
| 速率、带宽、速度 | `speed`、`bandwidth` | `speed`、`portSpeed` | **速率（speed）** |
| 厂家、品牌、供应商 | `vendor`、`brand` | `vendor`、`manufacturer` | **供应商（vendor）** |
| 历史、日志、操作记录 | `history`、`log` | `history`、`operationLog` | **操作历史（History）** |
