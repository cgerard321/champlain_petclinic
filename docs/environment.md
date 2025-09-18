# Environment Configuration

Back to [Main page](../README.md)

<!-- TOC -->
* [Environment Configuration](#environment-configuration)
  * [Environment Variables Setup for Frontend Application](#environment-variables-setup-for-frontend-application)
<!-- TOC -->

## Environment Variables Setup for Frontend Application

**Required Environment Variables:**

```bash
# For Vite (React) Frontend
# Create a file in the root of the petclinic-frontend folder: .env
# For development
VITE_ENV=dev
VITE_BACKEND_URL="http://localhost:8080/api/v2/gateway/"

# For production, this is simply for education purposes
# since you won't be deploying this project to production (already done here : https://petclinic.benmusicgeek.synology.me/home)
VITE_ENV=prod
VITE_BACKEND_URL="https://your-production-domain.com/api/v2/gateway/"
```
