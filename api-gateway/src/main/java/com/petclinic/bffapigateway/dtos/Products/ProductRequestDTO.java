package com.petclinic.bffapigateway.dtos.Products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String imageId;
    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private Integer productQuantity;
    private ProductType productType;
    private LocalDate releaseDate;
    private ProductStatus productStatus;
}
