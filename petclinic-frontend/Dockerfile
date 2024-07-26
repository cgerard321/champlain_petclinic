# Use an official Node.js Runtime as a base image
FROM node:21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install Project dependencies
RUN npm install

# Copy the rest of the application code to the working directory
COPY . .

# audit the installed packages
RUN npm audit

# Build the React app
RUN npm run build

# Use a smaller, lightweight base image for the final image
FROM nginx:alpine

# Adjust permissions for Nginx
RUN chown -R nginx:nginx /var/cache/nginx /var/run /var/log/nginx
RUN chown -R nginx:0 /usr/share/nginx/html && \
    chmod -R g+r+rwX /usr/share/nginx/html

COPY --from=build /app/dist /usr/share/nginx/html
# Copy the default Nginx configuration file
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 3000 for the NGINX web server
EXPOSE 3000

# Command to run NGINX
CMD ["nginx", "-g", "daemon off;"]