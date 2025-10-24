# Mailer Service Usage Standards

Back to [Main page](../README.md)

<!-- TOC -->
* [General Rules](#general-rules)
* [Backend Usage](#backend-usage)
    * [Backend Setup](#backend-setup)
        * [1. Add Required Dependencies](#1-add-required-dependencies)
        * [2. Add the Mail DTO](#2-add-the-mail-dto)
        * [3. Create the Mailer Service Client](#3-create-the-mailer-service-client)
        * [4. Add Service Configurations](#4-add-service-configurations-to-applicationyml)
        * [5. Add Rethrower Utility](#5-add-rethrower-utility)
        * [6. Create the Auth Service Client](#6-create-the-auth-service-client)
        * [7. Add GenericHttpException](#7-add-generichttpexception)
        * [8. Add Role and UserDetails DTOs](#8-add-role-and-userdetails-dtos)
    * [Send Email](#send-email)
    * [Understanding Rethrower and Role Components](#understanding-rethrower-and-role-components)
        * [Rethrower Utility](#rethrower-utility)
        * [Role DTO](#role-dto)
        * [Simplified Implementation](#simplified-implementation-alternative)
* [API Documentation](#api-documentation)
* [Environment Variables](#environment-variables-required)
<!-- TOC -->

## General Rules
1. Never communicate with the Mailer Service directly through the API Gateway.
    - All communication with the Mailer Service must go through your own domain service (e.g., `billing-service`, `customer-service`, etc.).
    - The API Gateway must never call the Mailer Service directly.
    - Do not register or reference a `MailServiceClient` or `MailController` inside the API Gateway.

2. Always retrieve user information from the Auth Service before sending emails.
    - Use the JWT token to authenticate the request
    - Call `authServiceClient.getUserById(jwtToken, userId)` to get the user details
    - Extract the email address from the returned `UserDetails` object

3. Create private helper methods for email generation.
    - Encapsulate email creation logic in private methods within your service implementation
    - These methods should accept user details and other necessary parameters
    - Return a `Mail` object configured with all required fields

4. Handle errors appropriately.
    - Log any errors that occur during email sending
    - Do not fail the entire business operation if email sending fails (use error handling strategies like `.onErrorResume()`)
    - Consider email sending as a supplementary action, not a critical one

## Backend Usage

### Backend Setup

These are the steps that need to be followed to integrate your service with the **Mailer Service**.

#### [↑](#backend-usage) 1. Add Required Dependencies

WebClient is included in Spring WebFlux, which should already be in your service dependencies:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

#### [↑](#backend-usage) 2. Add the Mail DTO

You will need to create the Mail DTO in your service with the following structure:

```java
package com.petclinic.yourservice.domainclientlayer.Mailing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Mail {
    @JsonProperty("EmailSendTo")
    private String emailSendTo;
    
    @JsonProperty("EmailTitle")
    private String emailTitle;
    
    @JsonProperty("TemplateName")
    private String templateName;
    
    @JsonProperty("Header")
    private String header;
    
    @JsonProperty("Body")
    private String body;
    
    @JsonProperty("Footer")
    private String footer;
    
    @JsonProperty("CorrespondantName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String correspondantName;
    
    @JsonProperty("SenderName")
    private String senderName;
}
```

**Note:** The `@JsonProperty` annotations are required because the Go mailer service expects PascalCase field names.

**Field Descriptions:**
- `emailSendTo` (required): The recipient's email address
- `emailTitle` (required): The subject line of the email
- `templateName`: Template identifier (if using templates)
- `header`: Email header text
- `body`: Main email content (supports HTML)
- `footer`: Email footer text
- `correspondantName`: Name of the correspondent
- `senderName`: Display name for the sender

#### [↑](#backend-usage) 3. Create the Mailer Service Client

Create a WebClient-based client following the same pattern as AuthServiceClient:

```java
package com.petclinic.yourservice.domainclientlayer.Mailing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MailServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String mailerServiceUrl;

    public MailServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.mailer-service.host}") String mailerServiceHost,
            @Value("${app.mailer-service.port}") String mailerServicePort) {
        this.webClientBuilder = webClientBuilder;
        this.mailerServiceUrl = "http://" + mailerServiceHost + ":" + mailerServicePort;
    }

    public Mono<String> sendMail(Mail mail) {
        return webClientBuilder.build()
                .post()
                .uri(mailerServiceUrl + "/mail")
                .bodyValue(mail)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Email sent successfully to: {}", mail.getEmailSendTo()))
                .doOnError(error -> log.error("Failed to send email to {}: {}", 
                                              mail.getEmailSendTo(), error.getMessage()));
    }
}
```

#### [↑](#backend-usage) 4. Add Service Configurations to application.yml

Add both the auth service and mailer service configurations in your application.yml file. This ensures all necessary service endpoints are properly configured in one place.

For default profile:
```yaml
app:
  auth-service:
    host: auth-service
    port: 8080
  mailer-service:
    host: mailer-service
    port: 8080
```

For docker profile:
```yaml
---
spring:
  config:
    activate:
      on-profile: docker

app:
  auth-service:
    host: auth-service
    port: 8080
  mailer-service:
    host: mailer-service
    port: 8080
```

#### [↑](#backend-usage) 5. Add Rethrower Utility

Create a Rethrower utility class to handle error responses from the Auth Service:

```java
// Rethrower.java
package com.petclinic.yourservice.domainclientlayer.Auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class Rethrower {
    private final ObjectMapper objectMapper;
    
    public Mono<? extends Throwable> rethrow(ClientResponse clientResponse, 
                                           Function<Map, ? extends Throwable> exceptionProvider) {
        return clientResponse.createException().flatMap(n -> {
            try {
                final Map map = objectMapper.readValue(n.getResponseBodyAsString(), Map.class);
                return Mono.error(exceptionProvider.apply(map));
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }
        });
    }
}
```

#### [↑](#backend-usage) 6. Create the Auth Service Client

You need the AuthServiceClient to retrieve user details (including email) before sending emails. This implementation includes proper error handling with the Rethrower:

```java
package com.petclinic.yourservice.domainclientlayer.Auth;

import com.petclinic.yourservice.exceptions.GenericHttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Component
public class AuthServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String authServiceUrl;
    
    @Autowired
    private Rethrower rethrower;

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort) {
        this.webClientBuilder = webClientBuilder;
        this.authServiceUrl = "http://" + authServiceHost + ":" + authServicePort;
    }

    public Mono<UserDetails> getUserById(String jwtToken, String userId) {
        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/users/{userId}", userId)
                .cookie("Bearer", jwtToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> rethrower.rethrow(response,
                                x -> new GenericHttpException(x.get("message").toString(), NOT_FOUND))
                )
                .bodyToMono(UserDetails.class)
                .doOnError(error -> log.error("Error fetching user {}: {}", userId, error.getMessage()));
    }
}
```

#### [↑](#backend-usage) 7. Add GenericHttpException

Create a custom exception class for handling HTTP errors:

```java
// GenericHttpException.java
package com.petclinic.yourservice.exceptions;

import org.springframework.http.HttpStatus;

public class GenericHttpException extends RuntimeException {
    private final HttpStatus status;

    public GenericHttpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
```

#### 6. Add Role and UserDetails DTOs

You will need both the Role and UserDetails DTOs from the Auth Service:

```java
// Role.java
package com.petclinic.yourservice.domainclientlayer.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Role {
    private int id;
    private String name;
}

// UserDetails.java
package com.petclinic.yourservice.domainclientlayer.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserDetails {
    private String userId;
    private String username;
    private String email;
    private Set<Role> roles;
}
```

#### 7. Update Service Implementation

Add the following dependencies to your service implementation:

```java
private final AuthServiceClient authClient;
private final MailServiceClient mailServiceClient;
```

### Send Email

The process of sending an email involves:
1. Getting the JWT token from the request
2. Fetching user details using the Auth Service
3. Generating the email content
4. Sending the email via the Mailer Service

#### Example Implementation from Billing Service

Here's a complete example of how the billing service sends a confirmation email after payment:

```java
@Override
public Mono<BillResponseDTO> processPayment(String customerId, String billId, 
                                           PaymentRequestDTO paymentRequestDTO, String jwtToken) {
    return authClient.getUserById(jwtToken, customerId)
            .onErrorResume(e -> {
                log.error("Failed to authenticate or fetch user for customerId: {}. Error: {}", 
                         customerId, e.getMessage(), e);
                if (e instanceof ResponseStatusException) {
                    return Mono.error(e);
                }
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                                 "Authentication failed or user not found"));
            })
            .flatMap(user -> {
                // Validate payment details
                if (paymentRequestDTO.getCardNumber() == null || 
                    paymentRequestDTO.getCardNumber().length() != 16 ||
                    paymentRequestDTO.getCvv() == null || 
                    paymentRequestDTO.getCvv().length() != 3 ||
                    paymentRequestDTO.getExpirationDate() == null || 
                    paymentRequestDTO.getExpirationDate().length() != 5) {
                    return Mono.error(new InvalidPaymentException("Invalid payment details"));
                }

                // Find and process the bill
                return billRepository.findByCustomerIdAndBillId(customerId, billId)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(
                                      HttpStatus.NOT_FOUND, "Bill not found")))
                        .flatMap(bill -> {
                            // Update bill status
                            BigDecimal interestAtPayment = InterestCalculationUtil.calculateInterest(bill);
                            bill.setInterest(interestAtPayment);
                            bill.setBillStatus(BillStatus.PAID);

                            // Generate and send confirmation email (fire and forget)
                            mailServiceClient.sendMail(generateConfirmationEmail(user))
                                    .subscribe();

                            // Save the updated bill
                            return billRepository.save(bill);
                        })
                        .map(EntityDtoUtil::toBillResponseDto);
            });
}

private Mail generateConfirmationEmail(UserDetails user) {
    return new Mail(
            user.getEmail(), 
            "Pet Clinic - Payment Confirmation", 
            "default", 
            "Pet Clinic confirmation email",
            "Dear, " + user.getUsername() + "\n" +
            "Your bill has been successfully paid",
            "Thank you for choosing Pet Clinic.", 
            user.getUsername(), 
            "ChamplainPetClinic@gmail.com"
    );
}
```

#### Key Points:

1. **Authenticate First**: Always call `authClient.getUserById(jwtToken, userId)` to get user details
2. **Extract Email**: Get the email from `user.getEmail()` method
3. **Private Helper Method**: Create a private method (e.g., `generateConfirmationEmail`) to build the Mail object
4. **Error Handling**: Use appropriate error handling for authentication failures
5. **Non-Blocking**: Email sending should not block the main business logic

## Understanding Rethrower and Role Components

Jump to: [Back to Top](#mailer-service-usage-standards)

### [↑](#backend-usage) Rethrower Utility
- The `Rethrower` utility is included for consistency with the billing service's implementation.
- It's primarily used by `AuthServiceClient` to handle and transform error responses from the Auth Service.
- While the billing service uses this pattern, you can simplify your implementation if you don't need the same level of error handling.

#### [↑](#rethrower-utility) When to Use the Full Implementation
Consider using the full implementation with `Rethrower` if you:
1. Need to parse error responses in a specific format
2. Want to maintain consistency with the billing service's error handling
3. Plan to add more complex error handling in the future

### [↑](#backend-usage) Role DTO
- The `Role` class is included for completeness as part of the `UserDetails` DTO.
- The billing service doesn't use the `roles` field in its current implementation.
- You can safely omit the `roles` field if your service doesn't need role-based functionality.

### [↑](#backend-usage) Simplified Implementation (Alternative)
If you don't need the full error handling capabilities, you can simplify the `AuthServiceClient`:

```java
public Mono<UserDetails> getUserById(String jwtToken, String userId) {
    return webClientBuilder.build()
            .get()
            .uri(authServiceUrl + "/users/{userId}", userId)
            .cookie("Bearer", jwtToken)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                response -> response.bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new GenericHttpException(error, NOT_FOUND)))
            )
            .bodyToMono(UserDetails.class)
            .doOnError(error -> log.error("Error fetching user {}: {}", userId, error.getMessage()));
}
```

### When to Simplify
You can use the simplified version if you:
1. Only need basic error handling
2. Don't require parsing of error response bodies
3. Want to reduce dependencies and complexity
6. **Logging**: Log errors but don't fail the entire operation if email sending fails

#### Email Content Best Practices:

- **Subject Line**: Make it clear and descriptive (e.g., "Payment Confirmation", "Order Receipt")
- **Header**: Brief introductory text
- **Body**: Main message content, can include HTML formatting
- **Footer**: Closing message or contact information
- **Sender Name**: Use a recognizable name (e.g., "ChamplainPetClinic@gmail.com")
- **Correspondent Name**: Personalize with the user's name

#### Error Handling Strategy:

```java
// Option 1: Fire and forget (recommended for non-critical emails)
mailServiceClient.sendMail(generateEmail(user))
    .subscribe(); // Email sending happens asynchronously

// Option 2: Fire and forget with explicit error handling
mailServiceClient.sendMail(generateEmail(user))
    .doOnError(e -> log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage()))
    .onErrorResume(e -> Mono.empty()) // Suppress error to not affect main flow
    .subscribe();

// Option 3: Wait for email to send (blocks the reactive chain)
mailServiceClient.sendMail(generateEmail(user))
    .flatMap(response -> {
        log.info("Email sent: {}", response);
        return billRepository.save(bill);
    })
    .onErrorResume(e -> {
        log.error("Email failed but continuing: {}", e.getMessage());
        return billRepository.save(bill); // Continue even if email fails
    });

// Option 4: Parallel execution (don't wait for email)
Mono<Bill> saveBill = billRepository.save(bill);
Mono<String> sendEmail = mailServiceClient.sendMail(generateEmail(user))
    .onErrorResume(e -> Mono.empty());

return Mono.when(sendEmail).thenReturn(saveBill).flatMap(m -> m);
```

## Mailer Service Technical Details

### Endpoint
- **URL**: `POST /mail`
- **Port**: `8888` (default)
- **Content-Type**: `application/json`

### Request Body Structure
```json
{
  "EmailSendTo": "user@example.com",
  "EmailTitle": "Email Subject",
  "TemplateName": "default",
  "Header": "Email Header",
  "Body": "Email body content",
  "Footer": "Email footer",
  "CorrespondantName": "John Doe",
  "SenderName": "Pet Clinic"
}
```

### Response
- **Success**: `200 OK` with message `"Message sent to {email}"`
- **Bad Request**: `400 Bad Request` if mail object is invalid
- **Internal Server Error**: `500 Internal Server Error` if sending fails

### Service Architecture
The Mailer Service is built with Go and uses:
- **Framework**: Gin
- **SMTP Library**: gopkg.in/mail.v2
- **Port**: 8888
- **Health Check**: Available via `/metrics` endpoint
- **Documentation**: Available via `/swagger/*any` endpoint

### [↑](#mailer-service-usage-standards) Environment Variables Required
The Mailer Service requires the following environment variables:
- `SMTP_SERVER`: SMTP server hostname
- `SMTP_USER`: SMTP username
- `SMTP_PASS`: SMTP password
