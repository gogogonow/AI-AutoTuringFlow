# Optical Modules Management System - Backend

## 项目简介

这是一个光模块管理系统的后端服务，基于 Spring Boot 框架开发，提供 RESTful API 接口用于管理光模块信息及其操作历史记录。

## 技术栈

- **Java 11**
- **Spring Boot 2.7.14**
- **Spring Data JPA**
- **MySQL 8.0**
- **HikariCP** (连接池)
- **Maven** (构建工具)

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
│       │               │   ├── JacksonConfig.java
│       │               │   └── WebConfig.java
│       │               ├── controller/
│       │               │   └── ModuleController.java
│       │               ├── exception/
│       │               │   ├── GlobalExceptionHandler.java
│       │               │   └── ResourceNotFoundException.java
│       │               ├── model/
│       │               │   ├── Module.java
│       │               │   └── History.java
│       │               ├── repository/
│       │               │   ├── ModuleRepository.java
│       │               │   └── HistoryRepository.java
│       │               └── service/
│       │                   └── ModuleService.java
│       └── resources/
│           └── application.properties
├── database/
│   ├── schema.sql
│   └── data.sql
├── pom.xml
└── README.md
```

## API 接口文档

### 1. 获取所有模块列表
- **URL**: `GET /api/modules`
- **响应**: 200 OK，返回模块列表

### 2. 获取单个模块详情
- **URL**: `GET /api/modules/{id}`
- **响应**: 
  - 200 OK，返回模块详情
  - 404 Not Found，模块不存在

### 3. 创建新模块
- **URL**: `POST /api/modules`
- **请求体**:
```json
{
  "code": "MOD-2024-001",
  "status": "Active",
  "vendor": "Vendor A",
  "ld": "LD-001",
  "pd": "PD-001",
  "remarks": "备注信息"
}
```
- **响应**: 
  - 201 Created，返回创建的模块
  - 400 Bad Request，参数错误

### 4. 更新模块信息
- **URL**: `PUT /api/modules/{id}`
- **请求体**: 同创建接口
- **响应**:
  - 200 OK，返回更新后的模块
  - 404 Not Found，模块不存在
  - 400 Bad Request，参数错误

### 5. 删除模块
- **URL**: `DELETE /api/modules/{id}`
- **响应**:
  - 200 OK，删除成功
  - 404 Not Found，模块不存在

### 6. 获取模块历史记录
- **URL**: `GET /api/modules/{id}/history`
- **响应**:
  - 200 OK，返回历史记录列表
  - 404 Not Found，模块不存在

## 数据库配置

### 1. 创建数据库
```sql
CREATE DATABASE optical_modules CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行数据库脚本
```bash
mysql -u root -p optical_modules < database/schema.sql
mysql -u root -p optical_modules < database/data.sql
```

### 3. 修改配置文件
编辑 `src/main/resources/application.properties`，修改数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/optical_modules
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 运行项目

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

1. **完整的 CRUD 操作**：支持光模块的创建、读取、更新和删除
2. **历史记录追踪**：自动记录所有操作历史，包括创建、更新和删除
3. **异常处理**：完善的异常处理机制，返回友好的错误信息
4. **数据验证**：对必填字段和唯一性约束进行验证
5. **CORS 支持**：支持跨域请求，方便前端调用
6. **连接池优化**：使用 HikariCP 连接池，提高数据库访问性能
7. **事务管理**：关键操作使用事务保证数据一致性

## 测试

### 使用 curl 测试

```bash
# 获取所有模块
curl http://localhost:8080/api/modules

# 创建新模块
curl -X POST http://localhost:8080/api/modules \
  -H "Content-Type: application/json" \
  -d '{"code":"TEST-001","status":"Active","vendor":"Test Vendor"}'

# 更新模块
curl -X PUT http://localhost:8080/api/modules/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"Inactive"}'

# 获取历史记录
curl http://localhost:8080/api/modules/1/history

# 删除模块
curl -X DELETE http://localhost:8080/api/modules/1
```

## 注意事项

1. 确保 MySQL 服务已启动
2. 确保数据库用户有足够的权限
3. 首次运行时会自动创建表结构（ddl-auto=update）
4. 生产环境建议将 `spring.jpa.hibernate.ddl-auto` 设置为 `validate` 或 `none`
5. 建议定期备份数据库

## 后续优化建议

1. 添加单元测试和集成测试
2. 实现数据分页和排序功能
3. 添加搜索和过滤功能
4. 实现用户认证和授权
5. 添加日志记录和监控
6. 实现缓存机制（Redis）
7. 添加 API 文档（Swagger）
