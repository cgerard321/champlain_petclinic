package com.petclinic.cartsservice.presentationlayer;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//since there will be only one cart by customer when the client
// will first create his account, the cart will be empty
public class CartRequestModel {
    private String customerId;
}
