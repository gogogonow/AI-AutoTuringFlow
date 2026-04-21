# 角色权限管控实现说明

## 概述

本实现为光模块管理系统添加了完整的角色权限管控功能，支持两种角色：
1. **OWNER** - 器件owner角色，拥有所有权限（增删改查）
2. **READER** - 只读角色，仅能查看数据，不能进行增删改操作

## 实现功能

### 1. 数据库层

**新增表：**
- `role` - 角色表，预置 OWNER 和 READER 两个角色
- `user` - 用户表，存储用户信息和角色关联

**迁移脚本：**
- `V8__add_user_and_role_tables.sql` - 创建用户和角色表，并插入默认数据
- 默认管理员账号：username: `admin`, password: `admin123`

### 2. 后端实现

**依赖添加：**
- Spring Security - 认证和授权框架
- JWT (jjwt) - Token生成和验证
- Flyway - 数据库版本管理

**核心组件：**

1. **实体类**
   - `Role.java` - 角色实体
   - `User.java` - 用户实体

2. **Repository**
   - `RoleRepository.java` - 角色数据访问
   - `UserRepository.java` - 用户数据访问

3. **DTO**
   - `LoginRequest.java` - 登录请求
   - `LoginResponse.java` - 登录响应（包含JWT token）
   - `RegisterRequest.java` - 注册请求
   - `UserDto.java` - 用户信息

4. **安全配置**
   - `SecurityConfig.java` - Spring Security配置，定义权限规则
   - `JwtUtil.java` - JWT工具类，生成和验证token
   - `JwtAuthenticationFilter.java` - JWT认证过滤器
   - `CustomUserDetailsService.java` - 用户详情服务

5. **服务层**
   - `AuthService.java` - 认证服务，处理登录、注册逻辑

6. **控制器**
   - `AuthController.java` - 认证端点
     - `POST /api/auth/login` - 用户登录
     - `POST /api/auth/register` - 用户注册
     - `GET /api/auth/me` - 获取当前用户信息

**权限规则：**
- 公开端点：`/api/auth/**`, `/api/health`
- GET请求（读操作）：所有已认证用户可访问
- POST/PUT/DELETE（写操作）：仅 OWNER 角色可访问
- 未认证请求返回 401
- 权限不足返回 403

### 3. 前端实现

**新增组件：**
- `Login.js` - 登录页面组件

**修改组件：**
- `Header.js` - 显示用户信息和退出按钮
- `Sidebar.js` - 根据角色隐藏"入库登记"菜单
- `ModuleList.js` - 根据角色隐藏导入、批量入库、编辑、删除按钮
- `ModuleDetails.js` - 根据角色隐藏编辑、删除、新增厂家按钮
- `App.js` - 添加路由守卫，未认证跳转登录页

**API客户端：**
- `api.js` - 添加JWT token自动注入到请求头
- 401响应自动跳转登录页
- token存储在localStorage

**样式：**
- `login.css` - 登录页面样式

### 4. 测试用例

**测试类：**
1. `AuthControllerTest.java` - 认证功能测试
   - 用户注册
   - 用户登录
   - 获取当前用户
   - 各种错误场景

2. `RoleAuthorizationTest.java` - 权限控制测试
   - OWNER可以创建/更新/删除模块
   - READER不能创建/更新/删除模块
   - 两种角色都可以读取数据
   - 未认证用户被拒绝访问

## 使用说明

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

### 默认账号

系统初始化时会自动创建一个管理员账号：
- 用户名：`admin`
- 密码：`admin123`
- 角色：OWNER

### API使用示例

**1. 登录获取Token**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

响应：
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "OWNER",
  "email": "admin@example.com"
}
```

**2. 注册新用户（只读角色）**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "reader1",
    "password": "password123",
    "email": "reader@example.com",
    "role": "READER"
  }'
```

**3. 使用Token访问API**
```bash
# 查询模块列表（OWNER和READER都可以）
curl http://localhost:8080/api/modules \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 创建模块（仅OWNER可以）
curl -X POST http://localhost:8080/api/modules \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "serialNumber": "TEST-001",
    "model": "TEST-MODEL",
    "inboundTime": "2024-01-01T00:00:00"
  }'
```

### 前端使用

1. 访问系统会自动跳转到登录页
2. 使用默认账号或注册的账号登录
3. 登录成功后：
   - 右上角显示用户名和角色标识
   - OWNER角色：可以看到所有功能按钮
   - READER角色：只能看到查看相关按钮，增删改按钮被隐藏

## 测试验证

### 手动测试步骤

1. **登录功能测试**
   - [ ] 使用正确的账号密码登录成功
   - [ ] 使用错误的账号密码登录失败
   - [ ] 登录后token存储在localStorage
   - [ ] 登录后显示用户信息

2. **OWNER角色测试**
   - [ ] 可以看到"入库登记"菜单
   - [ ] 可以看到"导入Excel"、"批量入库"按钮
   - [ ] 可以创建新模块
   - [ ] 可以编辑模块
   - [ ] 可以删除模块
   - [ ] 可以添加厂家信息

3. **READER角色测试**
   - [ ] 不能看到"入库登记"菜单
   - [ ] 不能看到"导入Excel"、"批量入库"按钮
   - [ ] 可以查看模块列表
   - [ ] 可以查看模块详情
   - [ ] 不能看到编辑、删除按钮
   - [ ] 不能看到添加厂家按钮

4. **权限控制测试**
   - [ ] READER尝试调用POST /api/modules返回403
   - [ ] READER尝试调用PUT /api/modules/{id}返回403
   - [ ] READER尝试调用DELETE /api/modules/{id}返回403
   - [ ] 未登录访问任何API返回401

5. **退出登录测试**
   - [ ] 点击退出按钮
   - [ ] Token被清除
   - [ ] 跳转到登录页

## 注意事项

1. **生产环境配置**
   - 修改JWT secret为更安全的随机字符串
   - 修改默认管理员密码
   - 配置HTTPS

2. **密码安全**
   - 密码使用BCrypt加密存储
   - 不允许明文密码

3. **Token管理**
   - Token有效期默认24小时
   - Token存储在localStorage
   - 401响应自动清除token并跳转登录

4. **数据库**
   - 使用Flyway管理数据库版本
   - User和Role表名使用反引号避免H2关键字冲突

## 技术栈

- Spring Boot 3.4.5
- Spring Security 6.4.10（显式覆盖，修复 6.4.x 授权绕过 CVE）
- JWT (jjwt 0.12.3)
- Flyway
- H2 (测试) / MySQL (生产)
