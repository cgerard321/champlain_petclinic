package com.petclinic.bffapigateway.dtos.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Since there will be only one cart by customer when the client
// first creates his account, the cart will be empty
public class CartRequestDTO {
    private String customerId;
}