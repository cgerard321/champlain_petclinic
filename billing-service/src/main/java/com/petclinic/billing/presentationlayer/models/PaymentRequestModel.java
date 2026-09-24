package com.petclinic.billing.presentationlayer.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class PaymentRequestModel {
    private String cardNumber;
    private String cvv;
    private String expirationDate;

}