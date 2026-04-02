#!/bin/sh
set -e

# Substitute ${BACKEND_URL} in the nginx config template at container startup.
# Default to docker-compose service name so local dev works without extra config.
BACKEND_URL="${BACKEND_URL:-http://backend:8080}"
export BACKEND_URL

envsubst '${BACKEND_URL}' \
  < /etc/nginx/conf.d/app.conf.template \
  > /etc/nginx/conf.d/app.conf

exec "$@"
