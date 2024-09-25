package com.petclinic.products.presentationlayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseModel {

    private String productId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private String productType;
    private Integer requestCount;
}
