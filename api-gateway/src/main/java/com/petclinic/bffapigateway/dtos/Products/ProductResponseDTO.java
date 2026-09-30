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
    private Double productSalePrice;
    private Double averageRating;
    private Integer requestCount;
    private Integer productQuantity;
    private Boolean isUnlisted;
    private ProductType productType;
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
            ProductType productType,
            ProductStatus productStatus,
            DeliveryType deliveryType) {
        this(productId, imageId, productName, productDescription,
                productSalePrice, averageRating, requestCount, productQuantity,
                isUnlisted, productType, productStatus, deliveryType, null);
    }
}
