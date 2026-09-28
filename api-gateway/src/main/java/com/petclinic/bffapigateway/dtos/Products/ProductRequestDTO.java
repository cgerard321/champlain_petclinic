package com.petclinic.bffapigateway.dtos.Products;

import com.petclinic.bffapigateway.dtos.Files.FileDetails;
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


    private String productName;
    private String productDescription;
    private Double productSalePrice;
    private Double averageRating;
    private Integer productQuantity;
    private Boolean isUnlisted;
    private ProductType productType;
    private LocalDate releaseDate;
    private ProductStatus productStatus;
    private DeliveryType deliveryType;
    private FileDetails image;

    /**
     * Keeps callers using the previous imageId-based constructor source-compatible.
     * New requests should provide image content through {@link #image}.
     */
    public ProductRequestDTO(
            String imageId,
            String productName,
            String productDescription,
            Double productSalePrice,
            Double averageRating,
            Integer productQuantity,
            Boolean isUnlisted,
            ProductType productType,
            LocalDate releaseDate,
            ProductStatus productStatus,
            DeliveryType deliveryType) {
        this(productName, productDescription, productSalePrice, averageRating,
                productQuantity, isUnlisted, productType, releaseDate,
                productStatus, deliveryType, null);
    }
}
