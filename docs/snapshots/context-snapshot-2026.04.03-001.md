# 项目上下文快照 — 初始化快照

## 快照元数据

| 字段 | 内容 |
|------|------|
| **快照版本** | v2026.04.03.001 |
| **生成时间** | 2026-04-03T09:07:18Z |
| **生成方式** | `scripts/init-context.sh` 自动生成 |
| **触发原因** | 项目上下文初始化 |
| **关联 PR/Issue** | （初始化，无关联） |
| **生成人/Agent** | copilot-swe-agent[bot] |

## 1. 初始化时扫描到的文件结构

### 前端文件

  Files:
    - frontend/index.html
    - frontend/js/api.js
    - frontend/js/app.js
    - frontend/js/components/Header.js
    - frontend/js/components/HistoryList.js
    - frontend/js/components/ModuleDetails.js
    - frontend/js/components/ModuleForm.js
    - frontend/js/components/ModuleList.js
    - frontend/js/components/Sidebar.js
    - frontend/styles/components.css
    - frontend/styles/main.css

### 后端文件

  Java source files:
    - backend/src/main/java/com/example/backend/BackendApplication.java
    - backend/src/main/java/com/example/backend/config/DatabaseInitializer.java
    - backend/src/main/java/com/example/backend/config/JacksonConfig.java
    - backend/src/main/java/com/example/backend/config/WebConfig.java
    - backend/src/main/java/com/example/backend/controller/HealthController.java
    - backend/src/main/java/com/example/backend/controller/ModuleController.java
    - backend/src/main/java/com/example/backend/exception/GlobalExceptionHandler.java
    - backend/src/main/java/com/example/backend/exception/ResourceNotFoundException.java
    - backend/src/main/java/com/example/backend/model/History.java
    - backend/src/main/java/com/example/backend/model/Module.java
    - backend/src/main/java/com/example/backend/repository/HistoryRepository.java
    - backend/src/main/java/com/example/backend/repository/ModuleRepository.java
    - backend/src/main/java/com/example/backend/service/ModuleService.java

## 2. 初始化时检测到的 API 路径

  Detected API annotations:
    - "/api/modules"
    - "/health"
    - "/{id}"
    - "/{id}/history"

## 3. 初始化备注

本快照由 `scripts/init-context.sh` 自动生成，记录项目上下文治理机制建立时的初始状态。
后续请使用 `scripts/refresh-context.sh` 在每次功能变更后更新上下文。
