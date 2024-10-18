package com.petclinic.products.presentationlayer.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestModel {

    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private String productType;
    private Integer productQuantity;
    private Boolean isUnlisted;
}
