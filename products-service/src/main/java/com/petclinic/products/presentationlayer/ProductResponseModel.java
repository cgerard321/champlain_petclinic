package com.petclinic.products.presentationlayer;

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
    private Integer requestCount;
}
