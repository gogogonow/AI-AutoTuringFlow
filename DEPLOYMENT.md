# Deployment Setup Guide

This guide covers two approaches for deploying the application:

- **[Method A](#method-a-self-hosted-server--ssh--docker-compose-recommended)** – Self-hosted server via SSH + Docker Compose (current default, most control)
- **[Method B](#method-b-cloud-platform-github-app-zero-server-management)** – Cloud platform GitHub Apps (Railway / Render / Fly.io, zero server management)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Developer merges PR → main                                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │ triggers
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  GitHub Actions: Build and Deploy workflow                      │
│                                                                 │
│  Job 1: build-and-push                                          │
│    1. Set up JDK 21 (Temurin)                                   │
│    2. mvn package → optical-modules-backend-1.0.0.jar           │
│    3. docker build + push → ghcr.io/<repo>/backend:<sha>        │
│    4. docker build + push → ghcr.io/<repo>/frontend:<sha>       │
│                                                                 │
│  Job 2: deploy  (needs job 1, environment: production)          │
│    1. scp docker-compose.yml + backend/database/ to server      │
│    2. SSH → write .env → docker compose pull → compose up -d    │
│    3. Health-check loop (30 × 5 s) on backend container         │
└──────────────────────────┬──────────────────────────────────────┘
                           │ SSH
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Linux Server  /opt/optical-modules                             │
│                                                                 │
│  [frontend :80]  →  [backend :8080]  →  [mysql :3306]          │
│   Nginx/Alpine      Spring Boot JRE21    MySQL 8.0              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Method A: Self-Hosted Server + SSH + Docker Compose (Recommended)

### Prerequisites

| Requirement | Notes |
|---|---|
| Linux cloud server (Ubuntu 22.04+) | Alibaba ECS / Tencent CVM / DigitalOcean Droplet / etc. |
| Docker Engine ≥ 24 + Docker Compose v2 | Installed on the server |
| Domain name (optional, for HTTPS) | DNS A record pointing to the server IP |

---

### Step A-1 – Prepare the Server

#### Install Docker and Docker Compose v2

```bash
# Ubuntu 22.04
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER   # run docker without sudo
newgrp docker
docker compose version          # must show v2+
```

#### Create the deployment directory

```bash
sudo mkdir -p /opt/optical-modules
sudo chown $USER:$USER /opt/optical-modules
```

#### Open firewall ports

Open ports **22** (SSH), **80** (HTTP), and optionally **443** (HTTPS) in your cloud provider's security group / firewall rules.

---

### Step A-2 – Generate an SSH Key Pair

Run this on your **local machine** (not the server):

```bash
ssh-keygen -t ed25519 -C "github-deploy" -f ~/.ssh/deploy_key
# Press Enter twice to leave the passphrase empty

# Install the public key on the server
ssh-copy-id -i ~/.ssh/deploy_key.pub YOUR_USER@YOUR_SERVER_IP
# Or manually on the server:
#   cat ~/.ssh/deploy_key.pub >> ~/.ssh/authorized_keys
#   chmod 600 ~/.ssh/authorized_keys

# Verify the key works
ssh -i ~/.ssh/deploy_key YOUR_USER@YOUR_SERVER_IP "echo connected"
```

Keep the **private key** (`~/.ssh/deploy_key`) – you will paste it into GitHub Secrets next.

---

### Step A-3 – Configure GitHub Secrets

Go to **Repository → Settings → Secrets and variables → Actions → New repository secret** and add all six secrets below:

| Secret name | How to get it | Example |
|---|---|---|
| `SSH_HOST` | Server IP address or hostname | `1.2.3.4` or `myserver.com` |
| `SSH_USER` | Linux username on the server | `ubuntu` |
| `SSH_PRIVATE_KEY` | Full content of `~/.ssh/deploy_key` (the **private** key, starts with `-----BEGIN OPENSSH PRIVATE KEY-----`) | *(paste entire file)* |
| `DB_ROOT_PASSWORD` | Choose a strong password for MySQL root | `MyR00tP@ss2024!` |
| `DB_USERNAME` | Application DB username | `appuser` |
| `DB_PASSWORD` | Application DB password | `MyApp@ss2024!` |

> **⚠️ GHCR Login on the Server**  
> The workflow passes `GITHUB_TOKEN` to the server to pull images from `ghcr.io`. This works during the workflow run, but if you want to pull images manually later (e.g. rollback), you need a Personal Access Token.  
>
> **Optional but recommended:** Create a [GitHub PAT](https://github.com/settings/tokens) with `read:packages` scope, store it as secret `GHCR_PAT`, and in `deploy.yml` replace:
> ```yaml
> echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
> ```
> with:
> ```yaml
> echo "${{ secrets.GHCR_PAT }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
> ```
>
> **Alternative (simplest):** Make the GHCR packages **public** — go to each package page → Package settings → Change visibility → Public. Then no login is needed on the server at all.

---

### Step A-4 – Create the GitHub `production` Environment

The deploy job is gated by `environment: production`. This environment must exist or the job will be skipped.

1. Go to **Repository → Settings → Environments → New environment**
2. Name it exactly `production`
3. *(Optional)* Add **Required reviewers** so every deployment needs manual approval
4. *(Optional)* Add **Deployment branches** rule to restrict to `main` only

All six secrets from Step A-3 can alternatively be stored at the environment level instead of the repository level for extra security.

---

### Step A-5 – Enable GHCR Write Permissions

The workflow needs to push Docker images to GitHub Container Registry:

1. Go to **Repository → Settings → Actions → General**
2. Under **Workflow permissions**, select **Read and write permissions**
3. Click **Save**

---

### Step A-6 – Domain & HTTPS (Optional but Recommended)

After the first successful deployment, configure Nginx + Certbot on the server for SSL:

```bash
sudo apt install -y nginx certbot python3-certbot-nginx

# Stop the container's port 80 first to free the port for host Nginx
# Edit docker-compose.yml: change frontend ports to "8081:80"
# Then:

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

# Issue SSL certificate (auto-configures HTTPS redirect)
sudo certbot --nginx -d app.yourdomain.com
```

> Replace `app.yourdomain.com` with your actual domain and `8081` with whatever port you chose for the frontend container.

---

### Step A-7 – Trigger the First Deployment

Merge any pull request to `main`, or push directly. The **Build and Deploy** workflow will:

1. Build the backend JAR with Maven (JDK 21)
2. Build and push Docker images to `ghcr.io/<repo>/backend:<sha>` and `ghcr.io/<repo>/frontend:<sha>`
3. SSH into the server, copy `docker-compose.yml` + SQL init files
4. Write the `.env` file with injected secrets, then run `docker compose pull && docker compose up -d`
5. MySQL initialises from `backend/database/schema.sql` + `data.sql` on first boot only
6. Health-check the backend for up to 150 seconds

Monitor progress at **Actions → Build and Deploy**.

---

## Method B: Cloud Platform GitHub App (Zero Server Management)

These platforms connect directly to your GitHub repository and deploy automatically on every push to `main`. No server, no SSH keys, no Docker daemon to manage yourself.

### Comparison

| Platform | Free tier | MySQL support | Code changes needed | Difficulty |
|---|---|---|---|---|
| **Railway** | $5 free credit/month | ✅ MySQL plugin | Minimal (add `railway.json` or use `Dockerfile`) | ⭐ Easy |
| **Render** | Free for static + web service | ⚠️ PostgreSQL only (MySQL is paid) | Medium (change DB driver to PostgreSQL) | ⭐⭐ Medium |
| **Fly.io** | Generous free tier (3 shared VMs) | ✅ via external MySQL or PlanetScale | Medium (add `fly.toml`, secrets) | ⭐⭐ Medium |
| **DigitalOcean App Platform** | $5/month minimum | ✅ Managed MySQL (paid add-on) | Minimal | ⭐ Easy |

---

### Option B-1: Railway

Railway has a GitHub App that redeploys on every push. It supports Docker and includes a MySQL plugin with zero configuration.

#### What you need to prepare

1. A [Railway](https://railway.app) account (sign in with GitHub)
2. No server, no SSH keys needed

#### Code changes required

**None for Docker-based deploy.** Railway auto-detects `Dockerfile` in `backend/` and `frontend/`.  
You only need to tell Railway where each service lives by adding a `railway.json` at the repo root:

```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "DOCKERFILE",
    "dockerfilePath": "backend/Dockerfile"
  },
  "deploy": {
    "startCommand": "java -jar app.jar",
    "healthcheckPath": "/actuator/health",
    "restartPolicyType": "ON_FAILURE"
  }
}
```

For the frontend, create a separate Railway service pointing to `frontend/Dockerfile`.

#### Environment variables on Railway

Railway injects a `DATABASE_URL` for the MySQL plugin. Your Spring Boot app reads individual DB vars (`DB_HOST`, `DB_PORT`, etc.), so set these in Railway's **Variables** panel by parsing `DATABASE_URL`, or add a custom `application.properties` referencing the Railway-provided `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` variables.

Add to `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://${MYSQLHOST:localhost}:${MYSQLPORT:3306}/${MYSQLDATABASE:optical_modules}
spring.datasource.username=${MYSQLUSER:appuser}
spring.datasource.password=${MYSQLPASSWORD:apppassword}
```

#### Setup steps

1. Go to [railway.app](https://railway.app) → **New Project → Deploy from GitHub repo**
2. Select this repository
3. Railway detects `Dockerfile` in `backend/` automatically
4. Click **+ New** → **Database** → **MySQL** to add a managed MySQL instance
5. Railway links the MySQL vars to your backend service automatically
6. Add a second service for the frontend (point to `frontend/Dockerfile`)
7. Set the frontend's public domain under **Settings → Networking → Generate Domain**

---

### Option B-2: Render

Render has a GitHub App and supports Docker-based web services. The free tier covers static sites and one web service, but **MySQL is a paid add-on**. The free database tier uses PostgreSQL.

#### Code changes required (switching to PostgreSQL)

1. Update `pom.xml` – replace the MySQL driver with PostgreSQL:

```xml
<!-- Remove: -->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- Add: -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

2. Update `application.properties` – change the datasource URL:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:optical_modules}
spring.datasource.driver-class-name=org.postgresql.Driver
```

3. Rewrite `backend/database/schema.sql` for PostgreSQL syntax (remove `ENGINE=InnoDB`, change `AUTO_INCREMENT` to `SERIAL`, etc.)

4. Add a `render.yaml` at the repo root to define services:

```yaml
services:
  - type: web
    name: optical-modules-backend
    runtime: docker
    dockerfilePath: ./backend/Dockerfile
    envVars:
      - key: DB_HOST
        fromDatabase:
          name: optical-modules-db
          property: host
      - key: DB_PORT
        fromDatabase:
          name: optical-modules-db
          property: port
      - key: DB_NAME
        fromDatabase:
          name: optical-modules-db
          property: database
      - key: DB_USERNAME
        fromDatabase:
          name: optical-modules-db
          property: user
      - key: DB_PASSWORD
        fromDatabase:
          name: optical-modules-db
          property: password

  - type: web
    name: optical-modules-frontend
    runtime: docker
    dockerfilePath: ./frontend/Dockerfile

databases:
  - name: optical-modules-db
    databaseName: optical_modules
    plan: free
```

#### Setup steps

1. Go to [render.com](https://render.com) → **New → Blueprint** → connect your GitHub repo
2. Render reads `render.yaml` and creates all services automatically
3. Free PostgreSQL database is provisioned and linked automatically

---

### Option B-3: Fly.io

Fly.io runs your Docker containers close to users on a global edge network. It has a generous free tier (3 shared-CPU VMs) and supports MySQL via an external provider like [PlanetScale](https://planetscale.com) (free MySQL-compatible serverless DB).

#### Code changes required

Add a `fly.toml` for the backend at the repo root:

```toml
app = "optical-modules-backend"
primary_region = "hkg"   # choose your nearest region

[build]
  dockerfile = "backend/Dockerfile"

[http_service]
  internal_port = 8080
  force_https = true
  auto_stop_machines = true
  auto_start_machines = true

[[vm]]
  cpu_kind = "shared"
  cpus = 1
  memory_mb = 512
```

#### Setup steps

```bash
# Install flyctl
curl -L https://fly.io/install.sh | sh

# Login and create the app
fly auth login
fly launch --no-deploy    # generates fly.toml, review and adjust

# Set environment variables (DB credentials from PlanetScale)
fly secrets set DB_HOST=... DB_USERNAME=... DB_PASSWORD=... DB_NAME=optical_modules

# Add the GitHub Actions deploy step
# In deploy.yml, replace the SSH deploy job with:
#   uses: superfly/flyctl-actions/setup-flyctl@master
#   run: fly deploy --remote-only
# and add FLY_API_TOKEN to GitHub Secrets
```

---

## Local Development

Run the full stack locally without any cloud account:

```bash
# Create a local .env file at the repository root
cat > .env <<'EOF'
DB_ROOT_PASSWORD=localrootpass
DB_USERNAME=appuser
DB_PASSWORD=localpass
EOF

# Build images and start all three services
docker compose up --build

# Access:
#   Frontend:   http://localhost
#   Backend API: http://localhost:8080/api
#   MySQL:       localhost:3306 (user: appuser)
```

> The MySQL container auto-runs `backend/database/schema.sql` and `backend/database/data.sql` on first start.  
> To reset the database: `docker compose down -v && docker compose up --build`

---

## Troubleshooting

| Issue | Diagnosis | Fix |
|---|---|---|
| Backend container keeps restarting | MySQL not ready yet or wrong credentials | `docker compose logs mysql` – check healthcheck; verify `DB_*` secrets match what is in `.env` |
| `docker compose pull` fails on server with 401 | GHCR image is private and `GITHUB_TOKEN` expired | Make GHCR packages **public**, or create a PAT with `read:packages` and store as `GHCR_PAT` secret |
| `.env` file has leading spaces on each line | heredoc indentation in YAML produces spaces | Already fixed in current `deploy.yml` (uses `cat > .env <<'EOF'` without indented content) |
| Port 80 already in use on server | Host Nginx is running on port 80 | Either stop host Nginx (`sudo systemctl stop nginx`) or change `frontend` container port to `8081:80` and update Nginx config |
| Database not initialised / tables missing | Init SQL only runs once on empty volume | `docker compose down -v` to delete the volume, then `docker compose up -d` to re-run init scripts |
| Images not found on server after manual pull | `GITHUB_TOKEN` is run-scoped and expired | Use a PAT with `read:packages` stored as `GHCR_PAT`, or make packages public |
| Backend health-check fails after 150 s | App takes too long to start (JVM cold start) | Increase `start_period` in `docker-compose.yml` (currently `60s`) or increase the retry loop in `deploy.yml` |
| Railway MySQL env vars not found | Railway uses `MYSQLHOST` etc., not `DB_HOST` | Add the env var mapping to `application.properties` as shown in Option B-1 |
