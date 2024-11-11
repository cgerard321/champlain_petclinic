package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.products.DeliveryType;
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
    private Double averageRating;
    private Integer requestCount;
    private Integer productQuantity;
    private Boolean isUnlisted;
    private ProductType productType;
    private ProductStatus productStatus;
    private DeliveryType deliveryType;
}
