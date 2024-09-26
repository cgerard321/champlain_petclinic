package com.petclinic.cartsservice.dataaccesslayer.cartproduct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProduct {
    private String productId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Integer quantityInCart;
}
