# Optical Modules Management System - Backend

## 项目简介

这是一个光模块管理系统的后端服务，基于 Spring Boot 框架开发，提供 RESTful API 接口用于管理光模块信息、操作历史记录、厂家信息，并支持用户认证与授权。

## 技术栈

- **Java 21** (LTS, 支持 Virtual Threads)
- **Spring Boot 3.4.5**
- **Spring Security 6.4.10**（显式覆盖，修复 6.4.x 授权绕过 CVE）
- **Spring Data JPA**
- **Spring Security + JWT**（用户认证与授权）
- **MySQL 8.0**
- **HikariCP**（连接池）
- **Flyway**（数据库版本管理与迁移）
- **Apache POI 5.3.0**（Excel 导入/导出）
- **Maven**（构建工具）

## 项目结构

```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── backend/
│       │               ├── BackendApplication.java
│       │               ├── config/
│       │               │   ├── DatabaseInitializer.java
│       │               │   ├── FlywayRepairConfig.java
│       │               │   ├── JacksonConfig.java
│       │               │   ├── SecurityConfig.java
│       │               │   └── WebConfig.java
│       │               ├── controller/
│       │               │   ├── AuthController.java
│       │               │   ├── HealthController.java
│       │               │   ├── HistoryController.java
│       │               │   ├── ModuleController.java
│       │               │   ├── ModuleImportExportController.java
│       │               │   └── ModuleVendorInfoController.java
│       │               ├── dto/
│       │               │   ├── ErrorResponse.java
│       │               │   ├── HistoryDto.java
│       │               │   ├── LoginRequest.java
│       │               │   ├── LoginResponse.java
│       │               │   ├── ModuleDto.java
│       │               │   ├── ModuleVendorInfoDto.java
│       │               │   ├── RegisterRequest.java
│       │               │   ├── StatusChangeRequest.java
│       │               │   └── UserDto.java
│       │               ├── exception/
│       │               │   ├── GlobalExceptionHandler.java
│       │               │   └── ResourceNotFoundException.java
│       │               ├── model/
│       │               │   ├── FiberType.java
│       │               │   ├── History.java
│       │               │   ├── LifecycleStatus.java
│       │               │   ├── LightType.java
│       │               │   ├── Module.java
│       │               │   ├── ModuleStatus.java
│       │               │   ├── ModuleVendorInfo.java
│       │               │   ├── OperationType.java
│       │               │   ├── Role.java
│       │               │   └── User.java
│       │               ├── repository/
│       │               │   ├── HistoryRepository.java
│       │               │   ├── ModuleRepository.java
│       │               │   ├── ModuleVendorInfoRepository.java
│       │               │   ├── RoleRepository.java
│       │               │   └── UserRepository.java
│       │               ├── security/
│       │               │   ├── CustomUserDetailsService.java
│       │               │   ├── JwtAuthenticationFilter.java
│       │               │   └── JwtUtil.java
│       │               └── service/
│       │                   ├── AuthService.java
│       │                   ├── HistoryService.java
│       │                   ├── HistoryServiceImpl.java
│       │                   ├── ModuleService.java
│       │                   ├── ModuleServiceImpl.java
│       │                   ├── ModuleVendorInfoService.java
│       │                   └── ModuleVendorInfoServiceImpl.java
│       └── resources/
│           ├── application.properties
│           └── db/migration/        # Flyway 迁移脚本
├── database/
│   ├── schema.sql                   # 参考用初始化脚本（生产环境由 Flyway 管理）
│   └── data.sql
├── scripts/
│   └── README.md
├── pom.xml
└── README.md
```

## API 接口文档

### 认证接口（`/api/auth`）

#### 用户登录
- **URL**: `POST /api/auth/login`
- **请求体**: `{ "username": "admin", "password": "password" }`
- **响应**: 200 OK，返回 JWT Token 及用户信息

#### 用户注册
- **URL**: `POST /api/auth/register`
- **请求体**: `{ "username": "user", "password": "password", "email": "..." }`
- **响应**: 201 Created，返回新建用户信息

#### 获取当前用户
- **URL**: `GET /api/auth/me`（需携带 Authorization: Bearer `<token>`）
- **响应**: 200 OK，返回当前登录用户信息

---

### 光模块接口（`/api/modules`）

#### 分页查询光模块列表（支持多条件搜索）
- **URL**: `GET /api/modules`
- **Query 参数**（均可选）: `serialNumber`、`speed`、`wavelength`、`transmissionDistance`、`connectorType`、`lifecycleStatus`、`packageForm`、`fiberType`、`lightType`、`page`（默认 0）、`size`（默认 20）、`sortBy`、`sortDir`
- **响应**: 200 OK，返回分页光模块列表

#### 根据 ID 获取光模块
- **URL**: `GET /api/modules/{id}`
- **响应**: 200 OK / 404 Not Found

#### 根据序列号获取光模块
- **URL**: `GET /api/modules/serial/{serialNumber}`
- **响应**: 200 OK / 404 Not Found

#### 创建光模块（入库）
- **URL**: `POST /api/modules`
- **响应**: 201 Created / 400 Bad Request

#### 批量入库
- **URL**: `POST /api/modules/batch`
- **请求体**: ModuleDto 数组
- **响应**: 201 Created，返回批量创建结果

#### 更新光模块信息
- **URL**: `PUT /api/modules/{id}`
- **响应**: 200 OK / 404 Not Found / 400 Bad Request

#### 删除光模块
- **URL**: `DELETE /api/modules/{id}`
- **响应**: 204 No Content / 404 Not Found

#### 导出光模块列表（Excel）
- **URL**: `GET /api/modules/export`
- **响应**: 200 OK，返回 `.xlsx` 文件下载

#### 从 Excel 导入光模块
- **URL**: `POST /api/modules/import`
- **请求**: `multipart/form-data`，字段名 `file`
- **响应**: 200 OK，返回导入统计信息

---

### 厂家信息接口（`/api/modules/{moduleId}/vendor-infos`）

| 方法 | URL | 说明 |
|------|-----|------|
| `GET` | `/api/modules/{moduleId}/vendor-infos` | 获取指定模块的所有厂家信息 |
| `GET` | `/api/modules/{moduleId}/vendor-infos/{id}` | 获取单条厂家信息 |
| `POST` | `/api/modules/{moduleId}/vendor-infos` | 新增厂家信息 |
| `PUT` | `/api/modules/{moduleId}/vendor-infos/{id}` | 更新厂家信息 |
| `DELETE` | `/api/modules/{moduleId}/vendor-infos/{id}` | 删除厂家信息 |

---

### 操作历史接口（`/api/histories`）

| 方法 | URL | 说明 |
|------|-----|------|
| `GET` | `/api/histories` | 分页查询（支持 `moduleId`、`operationType`、`operator`、`startTime`、`endTime` 筛选） |
| `GET` | `/api/histories/{id}` | 根据 ID 获取历史记录 |
| `GET` | `/api/histories/module/{moduleId}` | 获取指定模块的全部历史（列表） |
| `GET` | `/api/histories/module/{moduleId}/page` | 获取指定模块的历史（分页） |
| `GET` | `/api/histories/statistics/operation-type` | 各操作类型数量统计 |

---

### 健康检查（`/api/health`）

- **URL**: `GET /api/health`
- **响应**: 200 OK，服务存活确认

## 数据库配置

本项目使用 **Flyway** 进行数据库版本管理，启动时自动执行迁移脚本。

### 修改配置文件
编辑 `src/main/resources/application.properties`，配置数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/optical_modules
spring.datasource.username=your_username
spring.datasource.password=your_password
```

> 首次启动时 Flyway 会自动创建数据库表结构，无需手动执行 SQL 脚本。

## 运行项目

### 前置要求
- Java 21+
- Maven 3.6+
- MySQL 8.0

### 使用 Maven 运行
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 使用 JAR 包运行
```bash
mvn clean package
java -jar target/optical-modules-backend-1.0.0.jar
```

服务将在 `http://localhost:8080` 启动。

## 功能特性

1. **用户认证与授权**：Spring Security + JWT，支持注册、登录、获取当前用户
2. **完整的光模块 CRUD 操作**：包括批量入库和按序列号查询
3. **高级分页搜索**：支持按多个字段组合筛选与分页排序
4. **厂家信息管理**：每个光模块可关联多条厂家规格信息
5. **Excel 导入/导出**：使用 Apache POI 读写 `.xlsx` 文件
6. **操作历史追踪**：自动记录所有关键操作，支持多维度筛选与统计
7. **数据库版本管理**：使用 Flyway 管理迁移脚本，保证环境一致性
8. **异常处理**：完善的全局异常处理机制，返回统一错误格式
9. **数据验证**：Bean Validation 对请求体字段进行校验
10. **CORS 支持**：允许跨域请求，方便前端调用
11. **连接池优化**：使用 HikariCP 连接池，提高数据库访问性能

## 测试

### 使用 curl 测试

```bash
# 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# 分页查询光模块（需携带 Token）
curl http://localhost:8080/api/modules \
  -H "Authorization: Bearer <token>"

# 入库新模块
curl -X POST http://localhost:8080/api/modules \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"serialNumber":"SN-TEST-001","speed":"10G","wavelength":"1310nm"}'

# 导出 Excel
curl http://localhost:8080/api/modules/export \
  -H "Authorization: Bearer <token>" \
  -o modules.xlsx

# 获取历史记录（按模块）
curl http://localhost:8080/api/histories/module/1 \
  -H "Authorization: Bearer <token>"
```

## 注意事项

1. 确保 MySQL 8.0 服务已启动并创建了目标数据库
2. 确保数据库用户有 DDL 权限（Flyway 需要创建/修改表）
3. Flyway 迁移脚本位于 `src/main/resources/db/migration/`，不得随意修改已执行的脚本
4. 上传文件默认保存在 `uploads/` 目录，可通过 `app.upload.dir` 属性配置
5. 生产环境建议配置更强的 JWT 密钥并缩短 Token 有效期
6. 建议定期备份数据库
