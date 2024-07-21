# Stage 1 - Build the React application
FROM node:18-alpine as react-build
WORKDIR /petclinic-frontend/
COPY public /petclinic-frontend/public/
COPY src/ /petclinic-frontend/src
COPY package.json /frontend/
COPY package.json .
COPY package-lock.json .
RUN npm install
RUN npm run build

# Stage 2 - Serve the React application using NGINX
FROM nginx:1.19.0
WORKDIR /usr/share/nginx/html

# Copy the built React application from the first stage
COPY --from=react-build /petclinic-frontend/build/ /usr/share/nginx/html

# Optionally, if you have a custom NGINX configuration (nginx.conf), uncomment and modify this line:
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# Configure NGINX to serve the application
ENTRYPOINT ["nginx", "-g", "daemon off;"]
