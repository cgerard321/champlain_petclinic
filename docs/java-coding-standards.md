# Backend Standards (Java Spring Boot 3.x)

Back to [Main page](../README.md)

<!-- TOC -->

<!-- TOC -->

- [Backend Standards (Java Spring Boot 3.x)](#backend-standards-java-spring-boot-3x)
  - [API Gateway Controller Standards](#api-gateway-controller-standards)
    - [API Versioning](#api-versioning)
    - [Security Annotations](#security-annotations)
    - [Response Patterns](#response-patterns)
  - [Service Client Standards](#service-client-standards)
- [API Design Guidelines](#api-design-guidelines)
  - [Endpoint Patterns](#endpoint-patterns)
  - [Request/Response Standards](#requestresponse-standards)
- [Scheduled Tasks in Spring Boot](#scheduled-tasks-in-spring-boot)
  <!-- TOC -->

## API Gateway Controller Standards

### API Versioning

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

### Security Annotations

- Use `@SecuredEndpoint` for role-based access control
- Use `@IsUserSpecific` for user-specific resource access
- Always specify allowed roles explicitly

### Response Patterns

- Use `Mono<ResponseEntity<T>>` for single item responses
- Use `Flux<T>` for streaming responses
- Include proper HTTP status codes
- Implement consistent error handling

## Service Client Standards

- Use WebClient for reactive HTTP calls
- Implement proper error handling with status code checking
- Use meaningful error messages and custom exceptions
- Include circuit breaker patterns for resilience

# API Design Guidelines

## Endpoint Patterns

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

## Request/Response Standards

- Use consistent DTO naming: `*RequestDTO`, `*ResponseDTO`
- Include validation annotations on request DTOs
- Use proper HTTP status codes (200, 201, 400, 404), we want to avoid 500 errors, that means we did not handle something properly
- Include meaningful error messages in responses

# Scheduled Tasks in Spring Boot

See [Spring Scheduled Tasks](./spring-scheduled-tasks.md) for best practices and examples on implementing scheduled jobs in Java using Spring Boot.
