package com.petclinic.bffapigateway.dtos.Products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private String productId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private Integer quantityInCart;
}
