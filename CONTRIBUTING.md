# Contribution Guide & Coding Standards

## Overview

This document outlines the coding standards and best practices for contributing to the Champlain Pet Clinic project. The project consists of a React TypeScript frontend and Java Spring Boot 3.x microservices backend with an API Gateway following the Backend for Frontend (BFF) pattern.

## Table of Contents

- [Frontend Standards (React TypeScript)](#frontend-standards-react-typescript)
- [Backend Standards (Java Spring Boot 3.x)](#backend-standards-java-spring-boot-3x)
- [API Design Guidelines](#api-design-guidelines)
- [Environment Configuration](#environment-configuration)
- [Code Review Checklist](#code-review-checklist)

## Frontend Standards (React TypeScript)

### Critical Issues - These Will Not Be Tolerated

#### 1. Hardcoded URLs Instead of Environment Variables

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

#### 2. Using fetch() or Creating New Axios Instances

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

#### 3. Manual URL Concatenation with baseURL

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

#### 4. Inconsistent API Versioning

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

### Required Standards

#### API Layer Structure

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

#### Error Handling

- Always include try-catch blocks for async operations
- Use consistent error messages and logging
- Let the axios interceptor handle global error responses
- Throw errors to allow component-level handling

#### TypeScript Best Practices

- Use strict type checking
- Prefer interfaces over types for object shapes
- Use proper generic constraints
- Avoid `any` types - use `unknown` if **necessary**

#### React Component Standards

- Use functional components with hooks
- Implement proper cleanup in useEffect
- Use TypeScript for all props and state
- Follow the feature-based folder structure

## Environment Configuration

### Environment Variables Setup

**Required Environment Variables:**

```bash
# .env.development
VITE_ENV=dev
VITE_BACKEND_URL="http://localhost:8080/api/v2/gateway/"

# .env.production  
VITE_ENV=prod
VITE_BACKEND_URL="https://your-production-domain.com/api/v2/gateway/"
```

### Shared Axios Instance Configuration

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

### API Version Management

The project uses a flexible per-request versioning system that allows you to specify which API version to use for each individual request. The versioning is handled automatically by the axios instance using a `useV2` flag.

**Version Selection Policy:**

- **v1 API (Preferred)**: Use for all new development unless breaking changes are required
- **v2 API (Breaking Changes Only)**: Only use when creating non-backwards compatible changes that would break the Angular frontend
- **Be Explicit**: Always specify the version flag (`{ useV2: true }` or `{ useV2: false }`) to be declarative
- **Default Fallback**: While the system defaults to v2 when no flag is specified, you should always be explicit about your choice

**Key Features:**

- **Per-request control**: Use `{ useV2: false }` for v1 API or `{ useV2: true }` for v2 API
- **Automatic URL modification**: The interceptor prepends `/gateway` (v1) or `/v2/gateway` (v2) based on the flag
- **External URL protection**: HTTP/HTTPS URLs are not modified by the versioning system
- **Declarative approach**: Always specify the version flag for clarity and maintainability

**Basic Usage Examples:**

```typescript
// Preferred: Use v1 API explicitly (recommended for most cases)
const response = await axiosInstance.get('/vets', { useV2: false });

// Use v2 API explicitly (only when breaking changes are needed)
const response = await axiosInstance.get('/vets', { useV2: true });

// POST request with v1 API (preferred)
const response = await axiosInstance.post('/customers', customerData, { useV2: false });

// POST request with v2 API (only for breaking changes)
const response = await axiosInstance.post('/customers', customerData, { useV2: true });
```

**Environment Variables:**

```bash
# .env.development
VITE_ENV=dev
VITE_BACKEND_URL="http://localhost:8080/api"

# .env.production  
VITE_ENV=prod
VITE_BACKEND_URL="https://your-production-domain.com/api"
```

## Backend Standards (Java Spring Boot 3.x)

### API Gateway Controller Standards

#### API Versioning

- **v1 API**: Standard endpoints under `/api/gateway/*` (preferred for all new development)
- **v2 API**: Breaking change endpoints under `/api/v2/gateway/*` (only when non-backwards compatible changes are required)

**GOOD - V2 Controller Structure:**

```java
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/vets")
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:80"})
public class VetController {

    private final VetsServiceClient vetsServiceClient;
    
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VetResponseDTO> getVets() {
        return vetsServiceClient.getVets();
    }
}
```

#### Security Annotations

- Use `@SecuredEndpoint` for role-based access control
- Use `@IsUserSpecific` for user-specific resource access
- Always specify allowed roles explicitly

#### Response Patterns

- Use `Mono<ResponseEntity<T>>` for single item responses
- Use `Flux<T>` for streaming responses
- Include proper HTTP status codes
- Implement consistent error handling

### Service Client Standards

- Use WebClient for reactive HTTP calls
- Implement proper error handling with status code checking
- Use meaningful error messages and custom exceptions
- Include circuit breaker patterns for resilience

## API Design Guidelines

### Endpoint Patterns

**RESTful Resource Naming:**
```
GET    /api/v2/gateway/vets              # Get all vets
GET    /api/v2/gateway/vets/{vetId}      # Get specific vet
POST   /api/v2/gateway/vets              # Create new vet
PUT    /api/v2/gateway/vets/{vetId}      # Update existing vet
DELETE /api/v2/gateway/vets/{vetId}      # Delete vet
```

**Nested Resources:**

```
GET    /api/v2/gateway/vets/{vetId}/educations           # Get vet's education
POST   /api/v2/gateway/vets/{vetId}/educations           # Add education
PUT    /api/v2/gateway/vets/{vetId}/educations/{eduId}   # Update education
DELETE /api/v2/gateway/vets/{vetId}/educations/{eduId}   # Delete education
```

### Request/Response Standards

- Use consistent DTO naming: `*RequestDTO`, `*ResponseDTO`
- Include validation annotations on request DTOs
- Use proper HTTP status codes (200, 201, 400, 404, 500)
- Include meaningful error messages in responses

## Code Review Checklist

### Frontend Checklist

- [ ] Uses shared axios instance instead of fetch() or new axios instances
- [ ] No hardcoded URLs - uses environment variables
- [ ] Always explicitly specifies API version using `useV2` flag for clarity
- [ ] Prefers v1 API (`{ useV2: false }`) unless breaking changes require v2
- [ ] Only uses v2 API (`{ useV2: true }`) when non-backwards compatible changes are needed
- [ ] Proper TypeScript types and interfaces
- [ ] Includes error handling with try-catch
- [ ] Follows feature-based folder structure
- [ ] Uses proper React hooks and patterns
- [ ] No manual URL concatenation with baseURL
- [ ] External URLs (http://, https://) are not affected by versioning system

#### Practical Usage Examples for Version Flag Pattern

##### Example 1: Simple API call with version flag

```typescript
// src/features/veterinarians/api/getAllVets.ts
import axiosInstance from '@/shared/api/axiosInstance';
import { VetResponseModel } from '../models/VetResponseModel';

export async function getAllVets(): Promise<VetResponseModel[]> {
  try {
    // Use v1 API explicitly (preferred approach)
    const response = await axiosInstance.get<VetResponseModel[]>('/vets', { useV2: false });
    return response.data;
  } catch (error) {
    console.error('Error fetching vets:', error);
    throw error;
  }
}

export async function getAllVetsV2(): Promise<VetResponseModel[]> {
  try {
    // Only use v2 if breaking changes are needed that would affect Angular frontend
    const response = await axiosInstance.get<VetResponseModel[]>('/vets', { useV2: true });
    return response.data;
  } catch (error) {
    console.error('Error fetching vets:', error);
    throw error;
  }
}
```

##### Example 2: API call with different endpoints per version

```typescript
// src/features/veterinarians/api/getVetEducation.ts
import axiosInstance from '@/shared/api/axiosInstance';
import { EducationResponseModel } from '../models/EducationResponseModel';

export async function getVetEducation(vetId: string, useV2: boolean = false): Promise<EducationResponseModel> {
  try {
    if (useV2) {
      // V2 API: Only use when breaking changes are needed
      const response = await axiosInstance.get<EducationResponseModel>(
        `/vets/${vetId}/education`, 
        { useV2: true }
      );
      return response.data;
    } else {
      // V1 API: Preferred approach for backwards compatibility
      const response = await axiosInstance.get<EducationResponseModel>(
        `/vet/${vetId}/education`, 
        { useV2: false }
      );
      return response.data;
    }
  } catch (error) {
    console.error('Error fetching vet education:', error);
    throw error;
  }
}
```

##### Example 3: Gradual migration strategy with fallback

```typescript
// src/features/products/api/getProducts.ts
import axiosInstance from '@/shared/api/axiosInstance';
import { ProductModel } from '../models/ProductModel';

export async function getProducts(): Promise<ProductModel[]> {
  try {
    // Start with v1 API (preferred approach)
    const response = await axiosInstance.get<ProductModel[]>('/products', { useV2: false });
    return response.data;
  } catch (error) {
    // Only fallback to v2 if specifically needed for breaking changes
    if (import.meta.env.VITE_ENV === 'dev') {
      console.warn('V1 API failed, trying V2 for development testing:', error);
      try {
        const response = await axiosInstance.get<ProductModel[]>('/products', { useV2: true });
        return response.data;
      } catch (v2Error) {
        console.error('Both V1 and V2 APIs failed:', v2Error);
        throw v2Error;
      }
    }
    throw error;
  }
}
```

##### Example 4: POST/PUT requests with versioning

```typescript
// src/features/customers/api/createCustomer.ts
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerRequestModel, CustomerResponseModel } from '../models/CustomerModel';

export async function createCustomer(customer: CustomerRequestModel): Promise<CustomerResponseModel> {
  try {
    // Use v1 API explicitly (preferred approach for backwards compatibility)
    const response = await axiosInstance.post<CustomerResponseModel>('/customers', customer, { useV2: false });
    return response.data;
  } catch (error) {
    console.error('Error creating customer:', error);
    throw error;
  }
}

export async function createCustomerV2(customer: CustomerRequestModel): Promise<CustomerResponseModel> {
  try {
    // Only use v2 if breaking changes are needed that would affect Angular frontend
    const response = await axiosInstance.post<CustomerResponseModel>(
      '/customers', 
      customer, 
      { useV2: true }
    );
    return response.data;
  } catch (error) {
    console.error('Error creating customer with v2:', error);
    throw error;
  }
}
```

### Backend Checklist

- [ ] Uses v1 API endpoints for new features unless breaking changes are required
- [ ] Only uses v2 API endpoints when non-backwards compatible changes are needed
- [ ] Implements proper security annotations
- [ ] Uses reactive patterns (Mono/Flux) consistently
- [ ] Includes proper error handling and custom exceptions
- [ ] Follows RESTful naming conventions
- [ ] Uses WebClient for service-to-service communication
- [ ] Includes proper validation on request DTOs
- [ ] Uses consistent response patterns

### General Checklist

- [ ] Code follows established patterns in the codebase
- [ ] Includes appropriate logging
- [ ] Has proper unit/integration tests
- [ ] Documentation is updated if needed
- [ ] No breaking changes to existing APIs
- [ ] Performance considerations addressed
- [ ] Security implications reviewed

## Getting Started

1. **Setup Environment**: Copy the appropriate environment file and configure your variables
2. **Review Existing Code**: Study the patterns in `/src/shared/api/axiosInstance.ts` and similar files
3. **Follow the Standards**: Use the examples in this guide as templates
4. **Test Thoroughly**: Ensure your changes work with the existing infrastructure
5. **Submit for Review**: Include a checklist in your PR description

## Questions or Issues?

If you have questions about these standards or need clarification on any patterns, please:

1. Check existing code examples in the repository
2. Review this contributing guide
3. Ask questions in pull request discussions
4. Reach out to the team leads for architectural decisions

---

**Remember**: These standards ensure consistency, maintainability, and reliability across the entire codebase. Following them helps everyone and makes the application more robust.
