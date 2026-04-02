# 部署指南

本指南提供两种部署方式：

- **[方案 B — Railway（推荐）](#方案-b-railway-部署推荐)** – Railway 云平台一键部署，零服务器管理，免费额度即可起步
- **[方案 A — 自有服务器](#方案-a自有服务器--ssh--docker-compose)** – 自有服务器 + SSH + Docker Compose，完全掌控

---

## 架构说明

```
用户浏览器
    │  HTTP/HTTPS
    ▼
前端服务（Nginx + 静态文件，端口 80）
    │  /api/* 反向代理
    ▼
后端服务（Spring Boot，端口 8080）
    │  JDBC
    ▼
MySQL 数据库（端口 3306）
```

- **前端**：纯静态 HTML/CSS/JS，由 Nginx 托管；`/api/` 路径反向代理到后端，后端地址通过 `BACKEND_URL` 环境变量配置。
- **后端**：Spring Boot 3 + JDK 21，打包为可执行 JAR，通过 Docker 运行。数据库连接通过 `MYSQLHOST` / `MYSQLPORT` / `MYSQLDATABASE` / `MYSQLUSER` / `MYSQLPASSWORD` 环境变量配置。
- **数据库**：MySQL 8.0，初次启动时自动执行 `schema.sql` 和 `data.sql`。

---

## 方案 B：Railway 部署（推荐）

Railway 是一个支持 Docker 的云平台，连接 GitHub 仓库后每次推送即自动重新部署，内置 MySQL 插件，无需管理服务器。

### 前置条件

| 条件 | 说明 |
|---|---|
| GitHub 账号 | 仓库需托管在 GitHub |
| Railway 账号 | 访问 [railway.app](https://railway.app)，用 GitHub 账号登录 |

Railway 每月提供 **$5 免费额度**，足够运行三个服务（前端、后端、MySQL）进行开发和演示。

---

### 配置文件说明

本仓库已预先配置好 Railway 所需的所有文件，无需手动修改代码：

| 文件 | 作用 |
|---|---|
| `backend/railway.json` | 后端服务配置，指定 Dockerfile 构建方式 |
| `frontend/railway.json` | 前端服务配置，指定 `frontend/Dockerfile` |
| `frontend/nginx.conf` | Nginx 配置，`BACKEND_URL` 变量在容器启动时由 `envsubst` 替换 |
| `frontend/docker-entrypoint.sh` | 容器入口脚本，负责替换 nginx.conf 中的环境变量后启动 Nginx |
| `backend/src/main/resources/application.properties` | 已配置为读取 Railway MySQL 插件注入的 `MYSQL*` 环境变量 |

---

### 步骤 1：创建 Railway 项目

1. 登录 [railway.app](https://railway.app)
2. 点击右上角 **New Project（新建项目）**
3. 选择 **Deploy from GitHub repo（从 GitHub 仓库部署）**
4. 首次使用需授权 Railway 访问你的 GitHub，完成后选择本仓库

选择仓库后 Railway 会创建一个服务，你需要在 **Settings** 中将 **Root Directory** 设为 `backend`，Railway 会自动读取 `backend/railway.json` 并开始构建**后端服务**。

---

### 步骤 2：添加 MySQL 数据库插件

1. 在项目 Canvas（画布）界面，点击 **+ Add a Service（添加服务）**
2. 选择 **Database → MySQL**
3. Railway 会自动创建 MySQL 实例，并生成以下连接变量：

| 变量名 | 说明 |
|---|---|
| `MYSQLHOST` | MySQL 主机地址（Railway 内网） |
| `MYSQLPORT` | MySQL 端口（默认 3306） |
| `MYSQLDATABASE` | 数据库名 |
| `MYSQLUSER` | 数据库用户名 |
| `MYSQLPASSWORD` | 数据库密码 |

4. 点击后端服务 → **Variables（环境变量）** → **Add a Variable Reference（添加变量引用）**，将上述 5 个 MySQL 变量引用到后端服务

> Railway 有时会自动完成变量引用，请确认后端服务的 Variables 面板中包含 `MYSQLHOST` 等 5 个变量。

---

### 步骤 3：确认后端服务配置

1. 点击项目中的后端服务（名称通常与仓库名一致）
2. 进入 **Settings（设置）** 选项卡，确认以下设置：

   | 设置项 | 预期值 |
   |---|---|
   | Source Repo | 本仓库 |
   | Root Directory | `backend` |
   | Builder | `DOCKERFILE` |
   | Dockerfile Path | `Dockerfile` |

3. 进入 **Variables** 选项卡，确认 5 个 `MYSQL*` 变量已存在
4. 等待构建完成，状态变为绿色 ✅

---

### 步骤 4：（可选）为后端生成公网域名

如需从浏览器直接访问后端 API，或用于测试：

1. 点击后端服务 → **Settings → Networking（网络）**
2. 点击 **Generate Domain（生成域名）**
3. 获得类似 `https://xxx.up.railway.app` 的公网 URL

> 前端通过 Railway **私有内网**访问后端，不需要公网域名。但如果你想把 `BACKEND_URL` 设为公网地址，这里的 URL 也可以使用。

---

### 步骤 5：添加前端服务

1. 在项目 Canvas，点击 **+ Add a Service → GitHub Repo**
2. 选择同一个仓库
3. 点击新创建的前端服务 → **Settings**，配置以下内容：

   | 设置项 | 填写内容 |
   |---|---|
   | **Root Directory（根目录）** | `frontend` |
   | **Builder** | `DOCKERFILE`（会自动读取 `frontend/railway.json`） |
   | **Dockerfile Path** | `Dockerfile`（相对于 `frontend/` 目录） |

4. 点击 **Save** 后 Railway 会自动触发前端构建

---

### 步骤 6：配置前端环境变量 `BACKEND_URL`

前端 Nginx 在容器启动时通过 `BACKEND_URL` 环境变量确定后端地址。Railway 同一项目内的服务通过**私有内网**通信，地址格式为：

```
http://<服务名>.railway.internal:<端口>
```

**操作步骤**：

1. 点击前端服务 → **Variables（环境变量）**
2. 点击 **New Variable（新增变量）**，添加：

   | 变量名 | 值 |
   |---|---|
   | `BACKEND_URL` | `http://backend.railway.internal:8080` |

   > 其中 `backend` 是后端服务在 Railway 中显示的**服务名**（可在服务设置中查看）。若名称不同，请对应修改。

3. 保存后 Railway 会自动重新部署前端

> **备选方案**：如果不确定内网地址，也可以使用后端的公网域名（步骤 4 中生成的 URL，格式为 `https://xxx.up.railway.app`，**不要**带末尾斜杠）。

---

### 步骤 7：为前端配置公网域名

1. 点击前端服务 → **Settings → Networking**
2. 点击 **Generate Domain（生成域名）**
3. 获得类似 `https://frontend-xxx.up.railway.app` 的访问地址
4. 在浏览器中打开该地址，验证：
   - 前端页面正常加载
   - 点击任意功能，API 请求（`/api/...`）正常返回数据

---

### 步骤 8：初始化数据库（首次部署必须）

Railway MySQL 插件是空数据库，需手动执行初始化 SQL。

**方法一：使用 Railway 内置数据库控制台**

1. 点击项目中的 MySQL 服务
2. 进入 **Data（数据）** 选项卡，打开内置 SQL 编辑器
3. 依次粘贴并执行 `backend/database/schema.sql` 和 `backend/database/data.sql` 的内容

**方法二：使用本地 MySQL 客户端**

在 MySQL 服务的 **Connect** 选项卡中获取连接信息，然后执行：

```bash
# 替换以下变量为 Railway 控制台显示的实际值
mysql -h $MYSQLHOST -P $MYSQLPORT -u $MYSQLUSER -p"$MYSQLPASSWORD" $MYSQLDATABASE \
  < backend/database/schema.sql

mysql -h $MYSQLHOST -P $MYSQLPORT -u $MYSQLUSER -p"$MYSQLPASSWORD" $MYSQLDATABASE \
  < backend/database/data.sql
```

---

### 环境变量汇总

#### 后端服务（由 MySQL 插件自动注入，无需手动填写）

| 变量名 | 说明 |
|---|---|
| `MYSQLHOST` | MySQL 主机（Railway 内网地址） |
| `MYSQLPORT` | MySQL 端口 |
| `MYSQLDATABASE` | 数据库名 |
| `MYSQLUSER` | 数据库用户名 |
| `MYSQLPASSWORD` | 数据库密码 |

#### 前端服务（手动配置）

| 变量名 | 示例值 | 说明 |
|---|---|---|
| `BACKEND_URL` | `http://backend.railway.internal:8080` | 后端地址，Nginx 反向代理 `/api/` 时使用 |

---

### 自动部署（CI/CD）

Railway 与 GitHub 直接集成，**每次推送到 `main` 分支后自动触发重新构建和滚动部署**，无需额外配置 GitHub Actions。

---

### Railway 部署架构总览

```
GitHub main 分支推送
        │
        ├──▶ Railway 后端服务
        │      rootDir: backend/
        │      Dockerfile: Dockerfile
        │      env: MYSQLHOST / MYSQLPORT / MYSQLDATABASE / MYSQLUSER / MYSQLPASSWORD
        │             （由 MySQL 插件自动注入）
        │      healthcheck: /actuator/health
        │
        ├──▶ Railway 前端服务
        │      rootDir: frontend/
        │      Dockerfile: frontend/Dockerfile
        │      env: BACKEND_URL=http://backend.railway.internal:8080
        │
        └──▶ Railway MySQL 插件
               自动注入连接变量 → 后端服务
```

---

## 方案 A：自有服务器 + SSH + Docker Compose

### 前置条件

| 要求 | 说明 |
|---|---|
| Linux 云服务器（Ubuntu 22.04+） | 阿里云 ECS / 腾讯云 CVM / DigitalOcean Droplet 等 |
| Docker Engine ≥ 24 + Docker Compose v2 | 需安装在服务器上 |
| 域名（可选） | DNS A 记录指向服务器 IP |

---

### 步骤 A-1：准备服务器

```bash
# 安装 Docker 和 Docker Compose v2（Ubuntu 22.04）
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker
docker compose version   # 确认 v2+

# 创建部署目录
sudo mkdir -p /opt/optical-modules
sudo chown $USER:$USER /opt/optical-modules
```

在云服务商控制台开放防火墙端口：**22**（SSH）、**80**（HTTP）、可选 **443**（HTTPS）。

---

### 步骤 A-2：生成 SSH 密钥对

在**本地机器**上执行：

```bash
ssh-keygen -t ed25519 -C "github-deploy" -f ~/.ssh/deploy_key
# 两次回车，不设置密码

# 将公钥安装到服务器
ssh-copy-id -i ~/.ssh/deploy_key.pub YOUR_USER@YOUR_SERVER_IP

# 验证连通性
ssh -i ~/.ssh/deploy_key YOUR_USER@YOUR_SERVER_IP "echo connected"
```

---

### 步骤 A-3：配置 GitHub Secrets

进入 **仓库 → Settings → Secrets and variables → Actions → New repository secret**，添加以下 6 个密钥：

| Secret 名 | 说明 | 示例 |
|---|---|---|
| `SSH_HOST` | 服务器 IP 或域名 | `1.2.3.4` |
| `SSH_USER` | 服务器 Linux 用户名 | `ubuntu` |
| `SSH_PRIVATE_KEY` | 私钥全文（以 `-----BEGIN OPENSSH PRIVATE KEY-----` 开头） | *(粘贴私钥)* |
| `DB_ROOT_PASSWORD` | MySQL root 密码 | `MyR00tP@ss!` |
| `DB_USERNAME` | 应用数据库用户名 | `appuser` |
| `DB_PASSWORD` | 应用数据库密码 | `MyApp@ss!` |

---

### 步骤 A-4：创建 `production` 环境

部署 Job 依赖 `environment: production`，该环境必须提前创建：

1. 进入 **仓库 → Settings → Environments → New environment**
2. 命名为 `production`
3. （可选）设置 **Required reviewers** 要求人工审批后再部署

---

### 步骤 A-5：开启 GHCR 写权限

1. 进入 **仓库 → Settings → Actions → General**
2. **Workflow permissions** 选择 **Read and write permissions**
3. 点击 **Save**

---

### 步骤 A-6：触发首次部署

向 `main` 分支合并 PR 或直接推送，**Build and Deploy** Workflow 会：

1. 用 Maven + JDK 21 构建后端 JAR
2. 构建并推送 Docker 镜像到 `ghcr.io/<repo>/backend:<sha>` 和 `ghcr.io/<repo>/frontend:<sha>`
3. SSH 登录服务器，上传 `docker-compose.yml` 和 SQL 文件
4. 写入 `.env`，执行 `docker compose pull && docker compose up -d`
5. MySQL 首次启动时自动执行 `schema.sql` 和 `data.sql`
6. 循环健康检查后端（最多 150 秒）

---

### 步骤 A-7：配置域名 + HTTPS（可选）

首次部署成功后，可在服务器配置 Nginx + Let's Encrypt：

先修改 `docker-compose.yml` 将前端端口从 `"80:80"` 改为 `"8081:80"`，提交并重新部署，再执行：

```bash
sudo apt install -y nginx certbot python3-certbot-nginx

sudo tee /etc/nginx/sites-available/optical-modules <<'NGINXEOF'
server {
    listen 80;
    server_name app.yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINXEOF

sudo ln -s /etc/nginx/sites-available/optical-modules /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d app.yourdomain.com
```

---

## 本地开发

```bash
# 创建本地 .env 文件（仓库根目录）
cat > .env <<'EOF'
DB_ROOT_PASSWORD=localrootpass
DB_USERNAME=appuser
DB_PASSWORD=localpass
EOF

# 构建镜像并启动全部服务
docker compose up --build

# 访问地址：
#   前端：    http://localhost
#   后端 API：http://localhost:8080/api
#   MySQL：   localhost:3306 (用户: appuser)
```

> 重置数据库：`docker compose down -v && docker compose up --build`

---

## 故障排查

| 问题 | 排查思路 | 解决方法 |
|---|---|---|
| Railway 启动报 `RuntimeError: GitHub client is not initialised` 或出现 `/app/main.py` 错误 | **Root Directory 未设置**。Railway 从仓库根目录部署时，会检测到根目录的 `requirements.txt` 和 `main.py`，将其当作 Python 项目运行（`/app/main.py` 是 CI 专用的 AI 编排脚本，不是后端服务） | 在 Railway 控制台点击后端服务 → **Settings** → 将 **Root Directory** 设为 `backend`，保存后重新部署。Railway 将读取 `backend/railway.json` 使用 Dockerfile 构建 Spring Boot |
| 后端容器反复重启 | MySQL 未就绪或凭据错误 | `docker compose logs mysql`；检查 `MYSQL*` 变量是否与 MySQL 容器配置一致 |
| 前端 `/api/` 返回 502 | `BACKEND_URL` 配置错误 | 确认 Railway 前端服务的 `BACKEND_URL` 变量值；检查后端服务名是否与内网地址匹配 |
| Railway 数据库连接失败 | MySQL 变量未引用到后端服务 | 在后端服务 Variables 面板添加对 MySQL 插件变量的引用 |
| Railway 部署一直停在 Building | Dockerfile 构建失败 | 查看 Railway 构建日志，通常是依赖下载超时或代码编译错误 |
| `docker compose pull` 报 401 | GHCR 镜像是私有的 | 将 GHCR 包设为 **Public**，或创建含 `read:packages` 权限的 PAT 存为 `GHCR_PAT` 密钥 |
| 数据库表不存在 | SQL 初始化脚本未执行 | Railway：手动执行 `schema.sql`；本地：`docker compose down -v` 后重启 |
| 后端健康检查 150s 超时 | JVM 冷启动慢 | 调大 `docker-compose.yml` 中 backend 的 `start_period` |
