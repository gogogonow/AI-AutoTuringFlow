FROM nginx:1.25-alpine

# Remove the default site shipped with the base image
RUN rm /etc/nginx/conf.d/default.conf

# Install the envsubst template.
# The base image's entrypoint automatically runs envsubst on every file in
# /etc/nginx/templates/ and writes the result to /etc/nginx/conf.d/.
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Copy static frontend files
COPY . /usr/share/nginx/html/

# Default backend URL (docker-compose service name).
# Override with BACKEND_URL env var in Railway / production.
ENV BACKEND_URL=http://backend:8080

# Only substitute BACKEND_URL; leave nginx's own $variables untouched.
ENV NGINX_ENVSUBST_FILTER=BACKEND_URL

EXPOSE 80
