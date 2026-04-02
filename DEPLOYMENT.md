# Deployment Setup Guide

This guide explains how to configure the full CI/CD pipeline that automatically builds and deploys the application whenever code is merged into `main`.

## Architecture

```
GitHub push → Actions: build JAR → build Docker images → push to ghcr.io
                                  → SSH to server → docker compose up
Server: [Nginx frontend :80] → [Spring Boot backend :8080] → [MySQL :3306]
```

## Prerequisites

| Requirement | Notes |
|---|---|
| Linux cloud server (Ubuntu 22.04 recommended) | ECS / CVM / DigitalOcean Droplet, etc. |
| Docker Engine ≥ 24 + Docker Compose v2 | Installed on the server |
| A domain name | DNS A record pointing to the server IP |
| Nginx + Certbot (optional, for HTTPS) | Installed on the server for SSL termination |

---

## Step 1 – Server Setup

### Install Docker and Docker Compose

```bash
# Ubuntu 22.04
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER   # allow running docker without sudo
newgrp docker
docker compose version          # verify v2+
```

### Create deployment directory

```bash
sudo mkdir -p /opt/optical-modules
sudo chown $USER:$USER /opt/optical-modules
```

### Open firewall ports

Ensure ports **80** (HTTP) and optionally **443** (HTTPS) are open in your cloud provider's security group / firewall.

---

## Step 2 – GitHub Secrets

Go to **Settings → Secrets and variables → Actions** in the repository and add the following secrets:

| Secret name | Value |
|---|---|
| `SSH_HOST` | Server IP address or hostname |
| `SSH_USER` | Linux username used for deployment (e.g. `ubuntu`) |
| `SSH_PRIVATE_KEY` | Private SSH key whose public key is in `~/.ssh/authorized_keys` on the server |
| `DB_ROOT_PASSWORD` | MySQL root password (choose a strong password) |
| `DB_USERNAME` | Application database username (e.g. `appuser`) |
| `DB_PASSWORD` | Application database password |

> **GitHub Environment**: The deploy job is scoped to the `production` environment.  
> Create it at **Settings → Environments → New environment → production** for additional protection rules (e.g. required reviewers).

---

## Step 3 – Domain & HTTPS (Optional but Recommended)

After the first successful deploy, install Nginx and Certbot on the server to add HTTPS:

```bash
sudo apt install -y nginx certbot python3-certbot-nginx

# Create a reverse-proxy config pointing to the frontend container
sudo tee /etc/nginx/sites-available/optical-modules <<'EOF'
server {
    listen 80;
    server_name app.yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

sudo ln -s /etc/nginx/sites-available/optical-modules /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# Obtain SSL certificate
sudo certbot --nginx -d app.yourdomain.com
```

> Replace `app.yourdomain.com` with your actual domain.  
> After this the frontend container should be moved to a non-conflicting port (e.g. `8081`) in `docker-compose.yml` and the Nginx proxy updated accordingly.

---

## Step 4 – Trigger a Deployment

Merge any pull request into `main` (or push directly).  
The **Build and Deploy** Actions workflow will:

1. Build the backend JAR with Maven
2. Build and push Docker images to `ghcr.io/<org>/<repo>/backend` and `/frontend`
3. SSH into the server, pull the new images, and restart all containers
4. MySQL is started automatically with `schema.sql` and `data.sql` on first boot

Monitor the deployment in **Actions → Build and Deploy**.

---

## Local Development

You can run the full stack locally with:

```bash
# Build images locally and start all services
docker compose up --build

# Frontend: http://localhost
# Backend API: http://localhost:8080/api
# MySQL: localhost:3306 (user: appuser / password from .env)
```

Create a `.env` file at the repository root for local overrides:

```dotenv
DB_ROOT_PASSWORD=localrootpass
DB_USERNAME=appuser
DB_PASSWORD=localpass
```

---

## Troubleshooting

| Issue | Action |
|---|---|
| Backend container keeps restarting | Check MySQL healthcheck: `docker compose logs mysql` |
| Images not found on server | Ensure `GITHUB_TOKEN` has `packages: read` and GHCR visibility is set to public or the server is authenticated |
| Port 80 already in use | Stop the host Nginx before starting containers, or change the frontend port mapping |
| Database not initialised | The `initdb` scripts run only on first container start with an empty volume. Delete the volume with `docker compose down -v` and restart to re-run them |
