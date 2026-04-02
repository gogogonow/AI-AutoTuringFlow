FROM nginx:1.25-alpine
# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf
# Copy nginx config as a template; env vars are substituted at startup via docker-entrypoint.sh
COPY nginx.conf /etc/nginx/conf.d/app.conf.template
# Copy entrypoint script
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
# Copy static frontend files
COPY . /usr/share/nginx/html/
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
