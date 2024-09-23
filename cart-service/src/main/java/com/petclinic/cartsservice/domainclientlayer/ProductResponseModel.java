package com.petclinic.cartsservice.domainclientlayer;

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
    private Integer quantity;

    public ProductResponseModel(String productId, String productName, String productDescription, Double productSalePrice) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productSalePrice = productSalePrice;
    }
}
