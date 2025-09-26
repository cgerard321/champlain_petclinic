# Frontend Standards (React TypeScript)

Back to [Main page](../README.md)

<!-- TOC -->
* [Frontend Standards (React TypeScript)](#frontend-standards-react-typescript)
  * [Critical Issues - These Will Not Be Tolerated](#critical-issues---these-will-not-be-tolerated)
    * [1. Hardcoded URLs Instead of Environment Variables](#1-hardcoded-urls-instead-of-environment-variables)
    * [2. Using fetch() or Creating New Axios Instances](#2-using-fetch-or-creating-new-axios-instances)
    * [3. Manual URL Concatenation with baseURL](#3-manual-url-concatenation-with-baseurl)
    * [4. Inconsistent API Versioning](#4-inconsistent-api-versioning)
  * [Required Standards](#required-standards)
    * [API Layer Structure](#api-layer-structure)
    * [Error Handling](#error-handling)
    * [TypeScript Best Practices](#typescript-best-practices)
    * [React Component Standards](#react-component-standards)
  * [Shared Axios Instance Configuration](#shared-axios-instance-configuration)
<!-- TOC -->

## Critical Issues - These Will Not Be Tolerated

### 1. Hardcoded URLs Instead of Environment Variables

**BAD:**

```typescript
// DON'T: Hardcoded URLs
const response = await fetch('http://localhost:8080/api/v2/gateway/vets');

// DON'T: Hardcoded base URL in class
export class PromoApi {
  private static BASE_URL = 'http://localhost:8080/api/v2/gateway/promos';
}
```

**GOOD:**

```typescript
// DO: Be explicit about version choice - prefer v1 unless breaking changes needed
import axiosInstance from '@/shared/api/axiosInstance';

// Preferred: Use v1 API explicitly for most cases
const response = await axiosInstance.get('/vets', { useV2: false });

// Only use v2 when you need breaking changes that would affect Angular frontend
const response = await axiosInstance.get('/vets', { useV2: true });
```

### 2. Using fetch() or Creating New Axios Instances

**BAD:**

```typescript
// DON'T: Use fetch() directly
const response = await fetch(`http://localhost:8080/api/v2/gateway/vets/${vetId}`, {
  headers: { Accept: 'application/json' },
  credentials: 'include',
});

// DON'T: Create new axios instances
const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v2/gateway',
  headers: { 'Content-Type': 'application/json' },
});
```

**GOOD:**

```typescript
// DO: Use the shared axios instance
import axiosInstance from '@/shared/api/axiosInstance';

const response = await axiosInstance.get<VetResponseModel>(`/vets/${vetId}`, {useV2: false});
return response.data;
```

### 3. Manual URL Concatenation with baseURL

**BAD:**

```typescript
// DON'T: Manually concatenate with baseURL
const response = await axiosInstance.get<ProductModel[]>(
  axiosInstance.defaults.baseURL + `inventories/${inventoryId}/products`
);

// DON'T: Mix different concatenation styles
const response = await axiosInstance.get<InventoryType[]>(
  axiosInstance.defaults.baseURL + 'inventories/types'
);
```

**GOOD:**

```typescript
// DO: Let axios handle URL construction
const response = await axiosInstance.get<ProductModel[]>(
  `/inventories/${inventoryId}/products`
);

const response = await axiosInstance.get<InventoryType[]>('/inventories/types');
```

### 4. Inconsistent API Versioning

**BAD:**

```typescript
// DON'T: Mix different API versions in the same file
export const updateVetEducation = async (vetId: string, educationId: string, education: EducationRequestModel) => {
  return await axiosInstance.put(`/vets/${vetId}/educations/${educationId}`, education);
};

export const getEducation = async (vetId: string) => {
  return await axios.get(`http://localhost:8080/api/gateway/vet/${vetId}/education`);
};
```

**GOOD - Consistent Versioning:**

```typescript
// DO: Be explicit about API versioning and prefer v1 unless breaking changes needed
export const updateVetEducation = async (vetId: string, educationId: string, education: EducationRequestModel) => {
  // Use v1 API explicitly (preferred for backwards compatibility)
  return await axiosInstance.put(`/vets/${vetId}/educations/${educationId}`, education, { useV2: false });
};

export const getEducation = async (vetId: string) => {
  // Use v1 API explicitly 
  return await axiosInstance.get(`/vets/${vetId}/education`, { useV2: false });
};
```

**GOOD - Default v1 Pattern:**

```typescript
// DO: Prefer v1 API for new features unless breaking changes are required
import axiosInstance from '@/shared/api/axiosInstance';

export const updateVetEducation = async (vetId: string, educationId: string, education: EducationRequestModel) => {
  // Use v1 API explicitly (preferred approach)
  return await axiosInstance.put(`/vets/${vetId}/educations/${educationId}`, education, { useV2: false });
};

export const getEducation = async (vetId: string) => {
  // Only use v2 if you need breaking changes that would affect Angular frontend
  return await axiosInstance.get(`/vet/${vetId}/education`, { useV2: true });
};
```



## Required Standards

### API Layer Structure

- **File naming**: Use camelCase for API files (e.g., `getAllVets.ts`, `addInventory.ts`)
- **Function naming**: Use descriptive verbs (get, add, update, delete, search)
- **Return types**: Always specify return types and use proper TypeScript generics

**GOOD Example:**

```typescript
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';
import axiosInstance from '@/shared/api/axiosInstance';

export async function getAllVets(): Promise<VetResponseModel[]> {
  try {
    const response = await axiosInstance.get<VetResponseModel[]>('/vets', {
      responseType: 'stream'
    });
    
    return response.data
      .split('data:')
      .map((payload: string) => {
        try {
          if (payload === '') return null;
          return JSON.parse(payload);
        } catch (err) {
          console.error("Can't parse JSON:", err);
        }
      })
      .filter((data?: JSON) => data !== null);
  } catch (error) {
    console.error('Error fetching vets:', error);
    throw error;
  }
}
```

### Error Handling

- Always include try-catch blocks for async operations
- Use consistent error messages and logging
- Let the axios interceptor handle global error responses
- Throw errors to allow component-level handling

### TypeScript Best Practices

- Use strict type checking
- Prefer interfaces over types for object shapes
- Use proper generic constraints
- Avoid `any` types - use `unknown` if **necessary**

### React Component Standards

- Use functional components with hooks
- Implement proper cleanup in useEffect
- Use TypeScript for all props and state
- Follow the feature-based folder structure

## Shared Axios Instance Configuration

The project uses a centralized axios configuration at `/src/shared/api/axiosInstance.ts` with a flexible per-request versioning system:

```typescript
import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import axiosErrorResponseHandler from '@/shared/api/axiosErrorResponseHandler.ts';

axios.defaults.withCredentials = true;

// Extend AxiosRequestConfig to include our custom useV2 property
declare module 'axios' {
  export interface AxiosRequestConfig {
    useV2?: boolean;
  }
}

// Extend InternalAxiosRequestConfig to include our custom useV2 property
interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  useV2?: boolean;
}

const createAxiosInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL, // Base URL without version
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request interceptor to modify URL based on useV2 flag
  instance.interceptors.request.use(
    (config: CustomAxiosRequestConfig) => {
      // Default to v2 if useV2 is not specified
      const useV2 = config.useV2 !== undefined ? config.useV2 : true;
      const versionPath = useV2 ? '/v2/gateway' : '/gateway';

      // Modify the URL to include the version path
      if (
        config.url &&
        !config.url.startsWith('http://') &&
        !config.url.startsWith('https://')
      ) {
        config.url = versionPath + config.url;
      }

      // Remove the custom property from config
      delete config.useV2;

      return config;
    },
    error => {
      return Promise.reject(error);
    }
  );

  // Response interceptor to handle errors globally
  instance.interceptors.response.use(
    response => response,
    error => {
      handleAxiosError(error);
      return Promise.reject(error);
    }
  );

  return instance;
};

const axiosInstance = createAxiosInstance();

export default axiosInstance;
```
