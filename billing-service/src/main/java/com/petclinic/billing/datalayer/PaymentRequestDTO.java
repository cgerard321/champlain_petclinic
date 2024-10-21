package com.petclinic.billing.datalayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class PaymentRequestDTO {
    private String cardNumber;
    private String cvv;
    private String expirationDate;

}