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
// DO: Use shared axios instance with environment configuration
import axiosInstance from '@/shared/api/axiosInstance';

const response = await axiosInstance.get('/vets');
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

const response = await axiosInstance.get<VetResponseModel>(`/vets/${vetId}`);
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
// DO: Use consistent API versioning (prefer v2)
export const updateVetEducation = async (vetId: string, educationId: string, education: EducationRequestModel) => {
  return await axiosInstance.put(`/vets/${vetId}/educations/${educationId}`, education);
};

export const getEducation = async (vetId: string) => {
  return await axiosInstance.get(`/vets/${vetId}/education`);
};
```

**GOOD - Version Flag Pattern:**
```typescript
// DO: Use version flag for transitional periods
import { createVersionedInstance, shouldUseV2Api } from '@/shared/api/axiosInstance';

export const updateVetEducation = async (vetId: string, educationId: string, education: EducationRequestModel) => {
  const useV2 = shouldUseV2Api();
  const axiosInstance = createVersionedInstance(useV2);
  return await axiosInstance.put(`/vets/${vetId}/educations/${educationId}`, education);
};

export const getEducation = async (vetId: string) => {
  const useV2 = shouldUseV2Api();
  const axiosInstance = createVersionedInstance(useV2);
  const endpoint = useV2 ? `/vets/${vetId}/education` : `/vet/${vetId}/education`;
  return await axiosInstance.get(endpoint);
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
- Avoid `any` types - use `unknown` if necessary

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

The project uses a centralized axios configuration at `/src/shared/api/axiosInstance.ts`:

```typescript
import axios, { AxiosInstance } from 'axios';
import axiosErrorResponseHandler from '@/shared/api/axiosErrorResponseHandler.ts';

axios.defaults.withCredentials = true;

// Helper to determine API version from environment
export const shouldUseV2Api = (): boolean => {
  return import.meta.env.VITE_USE_V2_API !== 'false'; // Default to v2
};

// Base URLs for different API versions
const getBaseURL = (useV2: boolean): string => {
  if (useV2) {
    return import.meta.env.VITE_BACKEND_URL; // V2 from environment
  } else {
    return 'http://localhost:8080/api/gateway/'; // V1 hardcoded
  }
};

const createAxiosInstance = (useV2: boolean = shouldUseV2Api()): AxiosInstance => {
  const instance = axios.create({
    baseURL: getBaseURL(useV2),
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Global error handling interceptor
  instance.interceptors.response.use(
    response => response,
    error => {
      handleAxiosError(error);
      return Promise.reject(error);
    }
  );

  return instance;
};

// Default instance using environment flag
const axiosInstance = createAxiosInstance();

// Factory function to create instances with specific version
export const createVersionedInstance = (useV2: boolean): AxiosInstance => {
  return createAxiosInstance(useV2);
};

// Pre-configured instances for convenience
export const v1Instance = createAxiosInstance(false);
export const v2Instance = createAxiosInstance(true);

export default axiosInstance;
```

### API Version Management

For transitional periods where you need to support both v1 and v2 APIs, the main axios instance supports version switching through environment variables. The system is built into `/src/shared/api/axiosInstance.ts` and provides:

- **Environment-based version control**: Uses `VITE_USE_V2_API` flag
- **Factory function**: `createVersionedInstance(useV2: boolean)` 
- **Pre-configured instances**: `v1Instance` and `v2Instance`
- **Backward compatibility**: Default instance respects environment setting

**Environment Variables for Version Control:**
```bash
# .env.development
VITE_ENV=dev
VITE_BACKEND_URL="http://localhost:8080/api/v2/gateway/"
VITE_USE_V2_API="true"  # Flag to control API version

# .env.production  
VITE_ENV=prod
VITE_BACKEND_URL="https://your-production-domain.com/api/v2/gateway/"
VITE_USE_V2_API="true"  # Use v2 in production
```

## Backend Standards (Java Spring Boot 3.x)

### API Gateway Controller Standards

#### API Versioning
- **v1 API**: Legacy endpoints under `/api/gateway/*` (avoid adding new endpoints)
- **v2 API**: New endpoints under `/api/v2/gateway/*` (preferred for all new development)

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
- [ ] Consistent API versioning (prefers v2) or uses version flag pattern
- [ ] Proper TypeScript types and interfaces
- [ ] Includes error handling with try-catch
- [ ] Follows feature-based folder structure
- [ ] Uses proper React hooks and patterns
- [ ] No manual URL concatenation with baseURL

#### Practical Usage Examples for Version Flag Pattern

**Example 1: Simple API call with version flag**
```typescript
// src/features/veterinarians/api/getAllVets.ts
import { shouldUseV2Api, createVersionedInstance } from '@/shared/api/axiosInstance';
import { VetResponseModel } from '../models/VetResponseModel';

export async function getAllVets(): Promise<VetResponseModel[]> {
  const useV2 = shouldUseV2Api();
  const axiosInstance = createVersionedInstance(useV2);
  
  try {
    const response = await axiosInstance.get<VetResponseModel[]>('/vets');
    return response.data;
  } catch (error) {
    console.error('Error fetching vets:', error);
    throw error;
  }
}
```

**Example 2: API call with different endpoints per version**
```typescript
// src/features/veterinarians/api/getVetEducation.ts
import { shouldUseV2Api, v1Instance, v2Instance } from '@/shared/api/axiosInstance';
import { EducationResponseModel } from '../models/EducationResponseModel';

export async function getVetEducation(vetId: string): Promise<EducationResponseModel> {
  const useV2 = shouldUseV2Api();
  
  if (useV2) {
    // V2 API: /api/v2/gateway/vets/{vetId}/education
    const response = await v2Instance.get<EducationResponseModel>(`/vets/${vetId}/education`);
    return response.data;
  } else {
    // V1 API: /api/gateway/vet/{vetId}/education (note: different path structure)
    const response = await v1Instance.get<EducationResponseModel>(`/vet/${vetId}/education`);
    return response.data;
  }
}
```

**Example 3: Gradual migration strategy**
```typescript
// src/features/products/api/getProducts.ts
import { shouldUseV2Api, v1Instance, v2Instance } from '@/shared/api/axiosInstance';
import { ProductModel } from '../models/ProductModel';

export async function getProducts(): Promise<ProductModel[]> {
  const useV2 = shouldUseV2Api();
  
  try {
    if (useV2) {
      // Use new v2 endpoint with enhanced features
      const response = await v2Instance.get<ProductModel[]>('/products');
      return response.data;
    } else {
      // Fallback to v1 endpoint
      const response = await v1Instance.get<ProductModel[]>('/products');
      return response.data;
    }
  } catch (error) {
    // If v2 fails and we're in development, try v1 as fallback
    if (useV2 && import.meta.env.VITE_ENV === 'dev') {
      console.warn('V2 API failed, falling back to V1:', error);
      const response = await v1Instance.get<ProductModel[]>('/products');
      return response.data;
    }
    throw error;
  }
}
```

### Backend Checklist
- [ ] Uses v2 API endpoints for new features
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

**Remember**: These standards ensure consistency, maintainability, and reliability across the entire codebase. Following them helps everyone on the team and makes the application more robust.
