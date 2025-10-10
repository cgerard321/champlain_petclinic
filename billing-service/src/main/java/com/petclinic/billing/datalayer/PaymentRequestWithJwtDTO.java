package com.petclinic.billing.datalayer;

import lombok.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentRequestWithJwtDTO extends PaymentRequestDTO {
    private String jwtToken;
    public PaymentRequestWithJwtDTO(String cardNumber, String cvv, String expirationDate, String jwtToken) {
        super(cardNumber, cvv, expirationDate);
        this.jwtToken = jwtToken;
    }
}
