package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestModel {

    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Integer productQuantity;
    private Boolean isUnlisted;
    private ProductType productType;
    private LocalDate releaseDate;
    private ProductStatus productStatus;
}
