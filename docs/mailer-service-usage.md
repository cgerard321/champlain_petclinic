# Mailer Service Usage Standards

Back to [Main page](../README.md)

<!-- TOC -->
* [General Rules](#general-rules)
* [Backend Usage](#backend-usage)
    * [Backend Setup](#backend-setup)
    * [Send Email](#send-email)
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

#### 1. Add Required Dependencies

WebClient is included in Spring WebFlux, which should already be in your service dependencies:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

#### 2. Add the Mail DTO

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

#### 3. Create the Mailer Service Client

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

#### 4. Add Host and Port to application.yml

Add the mailer service configuration in the application.yml of your service:

```yaml
app:
  mailer-service:
    host: mailer-service
    port: 8888
```

For docker profile:
```yaml
---
spring:
  config:
    activate:
      on-profile: docker

app:
  mailer-service:
    host: mailer-service
    port: 8888
```

#### 5. Create the Auth Service Client

You need the AuthServiceClient to retrieve user details (including email) before sending emails:

```java
package com.petclinic.yourservice.domainclientlayer.Auth;

import lombok.extern.slf4j.Slf4j;
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

    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.auth-service.host}") String authServiceHost,
            @Value("${app.auth-service.port}") String authServicePort) {
        this.webClientBuilder = webClientBuilder;
        authServiceUrl = "http://" + authServiceHost + ":" + authServicePort;
    }

    public Mono<UserDetails> getUserById(String jwtToken, String userId) {
        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/users/{userId}", userId)
                .cookie("Bearer", jwtToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Auth service error: {}", errorBody);
                                    return Mono.error(new RuntimeException("User not found: " + userId));
                                })
                )
                .bodyToMono(UserDetails.class);
    }
}
```

#### 6. Add UserDetails DTO

You will need the UserDetails DTO from the Auth Service:

```java
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

#### 7. Add Auth Service Configuration to application.yml

Add the auth service configuration in the application.yml:

```yaml
app:
  auth-service:
    host: auth-service
    port: 7005
  mailer-service:
    host: mailer-service
    port: 8888
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
    port: 7005
  mailer-service:
    host: mailer-service
    port: 8888
```

#### 8. Update Service Implementation

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

### Environment Variables Required
The Mailer Service requires the following environment variables:
- `SMTP_SERVER`: SMTP server hostname
- `SMTP_USER`: SMTP username
- `SMTP_PASS`: SMTP password
