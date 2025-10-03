package com.petclinic.cartsservice.dataaccesslayer.cartproduct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList; // Unused import for Qodana test
import java.util.HashMap; 
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProduct {
    private String productId;
    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private Integer quantityInCart;
    private Integer productQuantity;
}
