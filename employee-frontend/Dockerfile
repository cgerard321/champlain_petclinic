FROM node:26.1-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

ARG API_URL
ARG CUSTOMER_PORTAL_URL
RUN if [ -n "$API_URL" ]; then \
      sed -i "s|apiUrl: '.*'|apiUrl: '${API_URL}'|" src/environments/environment.ts ; \
    fi
RUN if [ -n "$CUSTOMER_PORTAL_URL" ]; then \
      sed -i "s|customerPortal: '.*'|customerPortal: '${CUSTOMER_PORTAL_URL}'|" src/environments/environment.ts ; \
    fi

ARG BUILD_MODE=production
RUN npm run build -- --configuration=${BUILD_MODE}
RUN ls -R /app/dist

FROM nginx:alpine

RUN chown -R nginx:nginx /var/cache/nginx /var/run /var/log/nginx
RUN chown -R nginx:0 /usr/share/nginx/html && \
  chmod -R g+r+rwX /usr/share/nginx/html

COPY --from=build /app/dist/employee-frontend/browser/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 4200

CMD ["nginx", "-g", "daemon off;"]
