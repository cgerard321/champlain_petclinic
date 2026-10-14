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
public class ProductResponseDTO {

    private String productId;
    private String imageId;
    private String productName;
    private String productDescription;
    private String productNameFr;
    private String productDescriptionFr;
    private Double productSalePrice;
    private Double averageRating;
    private Integer requestCount;
    private Integer productQuantity;
    private Boolean isUnlisted;
    private String productTypeId;
    private String productType;
    private LocalDate releaseDate;
    private ProductStatus productStatus;
    private DeliveryType deliveryType;
    private FileDetails image;

    public ProductResponseDTO(
            String productId,
            String imageId,
            String productName,
            String productDescription,
            Double productSalePrice,
            Double averageRating,
            Integer requestCount,
            Integer productQuantity,
            Boolean isUnlisted,
            String productTypeId,
            String productType,
            LocalDate releaseDate,
            ProductStatus productStatus,
            DeliveryType deliveryType) {
        this(productId, imageId, productName, productDescription, null, null,
                productSalePrice, averageRating, requestCount, productQuantity,
                isUnlisted, productTypeId, productType,releaseDate, productStatus, deliveryType, null);
    }
}
