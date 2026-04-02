FROM nginx:1.25-alpine
# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf
# Copy nginx config as a template; env vars are substituted at startup via entrypoint
COPY nginx.conf /etc/nginx/conf.d/app.conf.template
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
