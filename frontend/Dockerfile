FROM nginx:1.25-alpine

# Remove the default site shipped with the base image
RUN rm /etc/nginx/conf.d/default.conf

# Create the envsubst template inline (avoids COPY build-context issues on Railway).
# The base image's entrypoint automatically runs envsubst on every file in
# /etc/nginx/templates/ and writes the result to /etc/nginx/conf.d/.
RUN mkdir -p /etc/nginx/templates && cat > /etc/nginx/templates/default.conf.template <<'NGINX_CONF'
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # Proxy API requests to the backend service
    location /api/ {
        proxy_pass ${BACKEND_URL}/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 60s;
    }

    # Health-check endpoint (used by Railway)
    location = /health {
        access_log off;
        add_header Content-Type application/json;
        return 200 '{"status":"UP"}';
    }

    # SPA fallback: serve index.html for any unmatched route
    location / {
        try_files $uri $uri/ /index.html;
    }
}
NGINX_CONF

# Copy static frontend files
COPY . /usr/share/nginx/html/

# Default backend URL (docker-compose service name).
# Override with BACKEND_URL env var in Railway / production.
ENV BACKEND_URL=http://backend:8080

# Only substitute BACKEND_URL; leave nginx's own $variables untouched.
ENV NGINX_ENVSUBST_FILTER=BACKEND_URL

EXPOSE 80
