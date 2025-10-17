# Environment Configuration

Back to [Main page](../README.md)

<!-- TOC -->
* [Environment Configuration](#environment-configuration)
  * [Environment Variables Setup for Frontend Application](#environment-variables-setup-for-frontend-application)
<!-- TOC -->

## Environment Variables Setup for Frontend Application

**Required Environment Variables:**

### React Frontend (petclinic-frontend)
```bash
# Create a file in the root of the petclinic-frontend folder: .env
# For development
VITE_ENV=dev
VITE_BACKEND_URL="http://localhost:8080/api/v2/gateway/"

# For production, this is simply for education purposes
# since you won't be deploying this project to production (already done here : https://petclinic.benmusicgeek.synology.me/home)
VITE_ENV=prod
VITE_BACKEND_URL="https://your-production-domain.com/api/v2/gateway/"
```

### Angular Frontend (angular-frontend)

No environment variables required

The Angular frontend uses relative URLs for API calls (`/api/gateway/...`) which are handled by:
- Development: Vite proxy (configured in `vite.config.js`) proxies `/api` to `http://localhost:8080`
- Production: Nginx proxy (configured in `nginx.conf`) proxies `/api` to `http://api-gateway:8080`


