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
    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Integer quantity;
    private Integer productStock;
    private double averageRating;
    private Integer productQuantity;



    public ProductResponseModel(String productId, String imageId, String productName, String productDescription, Double productSalePrice) {
        this.productId = productId;
        this.imageId = imageId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productSalePrice = productSalePrice;
    }
}
