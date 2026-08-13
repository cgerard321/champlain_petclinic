package com.petclinic.cartsservice.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequestModel {

    private String productId;
    private Integer quantity;

    public int resolveQuantity(int defaultQuantity) {
        return (quantity == null || quantity <= 0) ? defaultQuantity : quantity;
    }

    public int resolveQuantity() {
        return resolveQuantity(1);
    }
}
