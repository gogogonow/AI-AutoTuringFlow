FROM nginx:1.25-alpine
# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf
# Generate nginx config inline to avoid COPY issues with varying build contexts
RUN printf '%s\n' \
  'server {' \
  '    listen 80;' \
  '    server_name _;' \
  '' \
  '    root /usr/share/nginx/html;' \
  '    index index.html;' \
  '' \
  '    location /api/ {' \
  '        proxy_pass ${BACKEND_URL}/api/;' \
  '        proxy_set_header Host $host;' \
  '        proxy_set_header X-Real-IP $remote_addr;' \
  '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' \
  '        proxy_set_header X-Forwarded-Proto $scheme;' \
  '        proxy_connect_timeout 30s;' \
  '        proxy_read_timeout 60s;' \
  '    }' \
  '' \
  '    location = /health {' \
  '        access_log off;' \
  '        add_header Content-Type application/json;' \
  '        return 200 '"'"'{"status":"UP"}'"'"';' \
  '    }' \
  '' \
  '    location / {' \
  '        try_files $uri $uri/ /index.html;' \
  '    }' \
  '}' > /etc/nginx/conf.d/app.conf.template
# Generate entrypoint script inline to avoid COPY issues with varying build contexts
RUN { \
      echo '#!/bin/sh'; \
      echo 'set -e'; \
      echo 'BACKEND_URL="${BACKEND_URL:-http://backend:8080}"'; \
      echo 'export BACKEND_URL'; \
      echo 'envsubst '"'"'${BACKEND_URL}'"'"' < /etc/nginx/conf.d/app.conf.template > /etc/nginx/conf.d/app.conf'; \
      echo 'exec "$@"'; \
    } > /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh
# Copy static frontend files
COPY . /usr/share/nginx/html/
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
