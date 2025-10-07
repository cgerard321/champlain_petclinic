# API Version Management

Back to [Main page](../README.md)

<!-- TOC -->
* [API Version Management](#api-version-management)
      * [Practical Usage Examples for Version Flag Pattern](#practical-usage-examples-for-version-flag-pattern)
        * [Example 1: Simple API call with version flag](#example-1-simple-api-call-with-version-flag)
        * [Example 2: API call with different endpoints per version](#example-2-api-call-with-different-endpoints-per-version)
        * [Example 3: Gradual migration strategy with fallback](#example-3-gradual-migration-strategy-with-fallback)
        * [Example 4: POST/PUT requests with versioning](#example-4-postput-requests-with-versioning)
<!-- TOC -->


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
      // v1 API: Preferred approach for backwards compatibility
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
      console.warn('v1 API failed, trying V2 for development testing:', error);
      try {
        const response = await axiosInstance.get<ProductModel[]>('/products', { useV2: true });
        return response.data;
      } catch (v2Error) {
        console.error('Both v1 and V2 APIs failed:', v2Error);
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
