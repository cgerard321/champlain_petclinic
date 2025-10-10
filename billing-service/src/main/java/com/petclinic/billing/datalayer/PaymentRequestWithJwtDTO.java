package com.petclinic.billing.datalayer;

import lombok.*;

/**
 * Data Transfer Object for payment requests that require JWT authentication.
 * <p>
 * This DTO extends {@link PaymentRequestDTO} by adding a JWT token field, which should
 * contain a valid JSON Web Token used to authenticate and authorize the payment request.
 * The {@code jwtToken} field is expected to be provided by the client and validated by the service.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentRequestWithJwtDTO extends PaymentRequestDTO {
    /**
     * The JSON Web Token (JWT) used for authenticating and authorizing the payment request.
     * This token should be generated and signed by a trusted authority and must be validated
     * by the billing service before processing the payment.
     */
    private String jwtToken;

    /**
     * Constructs a new {@code PaymentRequestWithJwtDTO} with the specified card details and JWT token.
     *
     * @param cardNumber      the credit card number for the payment
     * @param cvv            the card verification value
     * @param expirationDate the expiration date of the card
     * @param jwtToken       the JSON Web Token used for authentication and authorization
     */
    public PaymentRequestWithJwtDTO(String cardNumber, String cvv, String expirationDate, String jwtToken) {
        super(cardNumber, cvv, expirationDate);
        this.jwtToken = jwtToken;
    }
}
