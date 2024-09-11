package com.petclinic.products.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestModel {

    private String productName;
    private String productDescription;
    private Double productSalePrice;
}
