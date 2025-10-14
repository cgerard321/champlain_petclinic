# Frontend Standards (React TypeScript)

Back to [Main page](../README.md)

<!-- TOC -->
- [Frontend Standards (React TypeScript)](#frontend-standards-react-typescript)
  - [Critical Issues - These Will Not Be Tolerated](#critical-issues---these-will-not-be-tolerated)
    - [1. Hardcoded URLs Instead of Environment Variables](#1-hardcoded-urls-instead-of-environment-variables)
    - [2. Using fetch() or Creating New Axios Instances](#2-using-fetch-or-creating-new-axios-instances)
    - [3. Manual URL Concatenation with baseURL](#3-manual-url-concatenation-with-baseurl)
    - [4. Inconsistent API Versioning](#4-inconsistent-api-versioning)
  - [Required Standards](#required-standards)
    - [API Layer Structure](#api-layer-structure)
    - [Error Handling](#error-handling)
      - [Hybrid Error Handling Strategy](#hybrid-error-handling-strategy)
      - [Global Error Handling (System-Level Errors)](#global-error-handling-system-level-errors)
      - [Local Error Handling (Component-Level Errors)](#local-error-handling-component-level-errors)
      - [Implementation Guidelines](#implementation-guidelines)
      - [Error Handling Best Practices](#error-handling-best-practices)
    - [TypeScript Best Practices](#typescript-best-practices)
    - [React Component Standards](#react-component-standards)
  - [Shared Axios Instance Configuration](#shared-axios-instance-configuration)
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
export const updateVetEducation = async (vetId: string, educationId: string, education: EducationRequestModel): Promise<ApiResponse<EducationResponseModel>> => {
  try {
    const response = await axiosInstance.put(`/vets/${vetId}/educations/${educationId}`, education, { useV2: false });
    return { data: response.data, errorMessage: null };
  } catch (error) {
    return { data: null, errorMessage: 'Unable to update education. Please try again.' };
  }
};

export const getEducation = async (vetId: string): Promise<ApiResponse<EducationResponseModel>> => {
  try {
    const response = await axiosInstance.get(`/vets/${vetId}/education`, { useV2: false });
    return { data: response.data, errorMessage: null };
  } catch (error) {
    return { data: null, errorMessage: 'Unable to fetch education details. Please try again.' };
  }
};
```

**GOOD - Default v1 Pattern:**

```typescript
// DO: Prefer v1 API for new features unless breaking changes are required
import axiosInstance from '@/shared/api/axiosInstance';

export const updateVetEducation = async (vetId: string, educationId: string, education: EducationRequestModel): Promise<ApiResponse<EducationResponseModel>> => {
  try {
    const response = await axiosInstance.put(`/vets/${vetId}/educations/${educationId}`, education, { useV2: false });
    return { data: response.data, errorMessage: null };
  } catch (error) {
    return { data: null, errorMessage: 'Unable to update education. Please try again.' };
  }
};

export const getEducation = async (vetId: string): Promise<ApiResponse<EducationResponseModel>> => {
  try {
    const response = await axiosInstance.get(`/vet/${vetId}/education`, { useV2: true });
    return { data: response.data, errorMessage: null };
  } catch (error) {
    return { data: null, errorMessage: 'Unable to fetch education details. Please try again.' };
  }
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

// Example instantiation
interface ApiResponse<T> {
  data: T | null;
  errorMessage: string | null;
}

export async function getAllVets(): Promise<ApiResponse<VetResponseModel[]>> {
  try {
    const response = await axiosInstance.get<VetResponseModel[]>('/vets', { useV2: false });
    return { data: response.data, errorMessage: null };
  } catch (error) {
    return { 
      data: null, 
      errorMessage: 'Unable to fetch veterinarians. Please try again later.' 
    };
  }
}
```

### Error Handling

The project implements a hybrid error handling approach that categorizes HTTP errors into global and local handling based on their nature and impact.

#### Hybrid Error Handling Strategy

The axios instance automatically categorizes errors into two types:

#### Global Error Handling (System-Level Errors)

These errors are handled globally by the axios interceptor and trigger automatic redirects:

- `401 Unauthorized` - Authentication required, redirects to login
- `403 Forbidden` - Access denied, redirects to forbidden page  
- `500 Internal Server Error` - Server issue, redirects to error page
- `502 Bad Gateway` - Server connectivity issue
- `503 Service Unavailable` - Maintenance mode, redirects to maintenance page
- `504 Gateway Timeout` - Server timeout issue
- `0 Network Error` - No connection available

#### Local Error Handling (Component-Level Errors)

These errors propagate to components for custom handling:

- `400 Bad Request` - Invalid request data, handle in form validation
- `404 Not Found` - Resource not found, show appropriate message
- `409 Conflict` - Data conflict, handle in business logic
- `422 Unprocessable Entity` - Validation errors, show field-specific errors
- `429 Too Many Requests` - Rate limiting, show retry message

#### Implementation Guidelines

**BAD - Manual Error Handling:**

```typescript
// DON'T: Handle authentication manually in every component
export const getVets = async () => {
  try {
    // Missing explicit API versioning
    const response = await axiosInstance.get('/vets');
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      // Manual redirect - already handled globally
      window.location.href = '/login';
    }
    throw error;
  }
};
```

**GOOD - Rely on Global Handling:**

```typescript
// DO: Let global handler manage system errors automatically
export const getVets = async (): Promise<VetResponseModel[]> => {
  try {
    // Use explicit API versioning - prefer v1 unless breaking changes needed
    const response = await axiosInstance.get<VetResponseModel[]>('/vets', { useV2: false });
    return response.data;
  } catch (error) {
    // Only handle business logic errors here
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      // Handle the error locally, warn client about the not found or show via ui the client that the item is not found
    }
    throw error; // Let other errors propagate
  }
};
```

**GOOD - Component Error Handling:**

```typescript
// DO: Handle local errors in components for user feedback
const VetList: React.FC = () => {
  const [vets, setVets] = useState<VetResponseModel[]>([]);
  const [error, setError] = useState<string>('');
  
  const loadVets = async () => {
    try {
      // Use explicit API versioning - prefer v1 unless breaking changes needed
      const vets = await getVets();
      setVets(vets);
      setError('');
    } catch (error) {
      // Handle local errors (404) with user feedback
      if (axios.isAxiosError(error)) {
        switch (error.response?.status) {
          case 404:
            setError('No veterinarians found. Please try again later.');
            break;
          default:
            setError('An unexpected error occurred.');
        }
      }
      // Global errors (401, 500, etc.) are automatically handled by interceptor
    }
  };

  // Example of searching with API versioning
  const searchVetsBySpecialty = async (specialty: string) => {
    try {
      // Use v1 API explicitly for search functionality
      const response = await axiosInstance.get<VetResponseModel[]>(
        `/vets/search?specialty=${specialty}`, 
        { useV2: false }
      );
      setVets(response.data);
      setError('');
    } catch (error) {
      if (axios.isAxiosError(error)) {
        switch (error.response?.status) {
          case 400:
            setError('Invalid specialty provided. Please check your input.');
            break;
          case 404:
            setError(`No veterinarians found with specialty: ${specialty}`);
            break;
          case 422:
            setError('Invalid search criteria. Please check your filters.');
            break;
          default:
            setError('Search failed. Please try again.');
        }
      }
    }
  };
};
```

#### Error Handling Best Practices

- Always include try-catch blocks for async operations
- Return structured responses with data and error messages instead of throwing errors
- Let the axios interceptor handle global error responses
- **Important**: If you use `console.error()` for debugging purposes, remove it before merging into production
- **Error Property**: Use `errorMessage` instead of `error` to clearly indicate this is a user-friendly message intended to be displayed directly to the client. This is a local error message that components can show to users without additional processing.

**Usage in Components:**

```typescript
const { data: customers, errorMessage } = await getAllCustomers();

if (errorMessage) {
  // Display error message directly to user
  setErrorAlert(errorMessage);
} else {
  // Handle success case
  setCustomers(customers);
}
```

**Example:**

```typescript
// Here you can use a Consistent ApiResponse<T> interface to expect all api hook calls to return the data and an errorMessage if a local error occurs
interface ApiResponse<T> {
  data: T | null;
  errorMessage: string | null;
}

export async function createCustomer(customer: CustomerRequestModel): Promise<ApiResponse<CustomerResponseModel>> {
  try {
    const response = await axiosInstance.post<CustomerResponseModel>('/customers', customer, { useV2: false });
    return { data: response.data, errorMessage: null };
  } catch (error) {
    return { 
      data: null, 
      errorMessage: 'Unable to create customer. Please check your information and try again.' 
    };
  }
}
```

### TypeScript Best Practices

- Use strict type checking
- Prefer interfaces over types for object shapes
- Use proper generic constraints
- Avoid `any` types - use `unknown` if **absolutely necessary**

### React Component Standards

- Use functional components with hooks
- Implement proper cleanup in useEffect
- Use TypeScript for all props and state
- Follow the feature-based folder structure

## Shared Axios Instance Configuration

The project uses a centralized axios configuration at `/src/shared/api/axiosInstance.ts` with a flexible per-request versioning system and hybrid error handling:

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

  // Response interceptor with hybrid error handling
  instance.interceptors.response.use(
    response => response,
    error => {
      // Handle errors through a custom error handler
      const shouldStopPropagation = handleAxiosError(error);

      // If the error handler returns true, don't propagate the error further
      if (shouldStopPropagation) {
        return Promise.resolve({ data: null, status: error.response?.status });
      }

      // Otherwise, let the error propagate to try-catch blocks
      return Promise.reject(error);
    }
  );

  return instance;
};

const handleAxiosError = (error: unknown): boolean => {
  if (axios.isAxiosError(error)) {
    const statusCode = error.response?.status ?? 0;

    // Define which errors should be handled globally vs locally
    const globallyHandledCodes = {
      401: 'Authentication required - redirecting to login',
      403: 'Access forbidden - redirecting to unauthorized page',
      500: 'Internal server error - showing error page',
      502: 'Bad gateway - server issue',
      503: 'Service unavailable - showing maintenance page',
      504: 'Gateway timeout - server issue',
      0: 'Network error - no connection',
    };

    // Local handling errors (don't stop propagation)
    const locallyHandledCodes = [400, 404, 409, 422, 429];

    // log the error only for debugging
    // console.error(`HTTP ${statusCode} Error:`, error.response?.data || error.message);

    // Handle global errors
    if (statusCode in globallyHandledCodes) {
      axiosErrorResponseHandler(error, statusCode);
      return true; // Stop propagation
    }

    // Let local errors propagate to components
    if (locallyHandledCodes.includes(statusCode)) {
      return false; // Continue propagation
    }

    // For unknown status codes, let components handle
    return false;
  }
  
  return false;
};

const axiosInstance = createAxiosInstance();

export default axiosInstance;
```
