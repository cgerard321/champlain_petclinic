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
    private boolean promoLookupFailed;
    private String productType;
    private double discountAmount;
    private String productId;
    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private Integer quantityInCart;
    private Integer productQuantity;
}
