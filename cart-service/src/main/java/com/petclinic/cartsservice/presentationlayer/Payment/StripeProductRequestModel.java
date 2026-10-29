package com.petclinic.cartsservice.presentationlayer.Payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeProductRequestModel {

    private String cartId;
    private String frontendTypeUrl;

}
