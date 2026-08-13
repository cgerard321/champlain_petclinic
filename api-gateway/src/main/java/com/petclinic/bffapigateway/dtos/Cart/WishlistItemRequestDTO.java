package com.petclinic.bffapigateway.dtos.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemRequestDTO {
    private String productId;
    private Integer quantity;

    public int resolveQuantity() {
        return (quantity == null || quantity <= 0) ? 1 : quantity;
    }
}
