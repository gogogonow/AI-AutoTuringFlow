FROM nginx:1.25-alpine

# Remove the default site shipped with the base image
RUN rm /etc/nginx/conf.d/default.conf

# Install nginx config template.  The base image entrypoint automatically
# runs envsubst on /etc/nginx/templates/*.template at container start and
# writes results to /etc/nginx/conf.d/.  Only environment variables that
# actually exist are substituted, so nginx's own lowercase variables
# ($host, $uri, $scheme …) are never touched.
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Copy static frontend files
COPY . /usr/share/nginx/html/

# Default backend URL (docker-compose service name).
# Override with BACKEND_URL env var in Railway / production.
ENV BACKEND_URL=http://backend:8080

# Railway injects PORT at runtime; default to 80 for local docker-compose.
ENV PORT=80

EXPOSE 80
