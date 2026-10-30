package com.petclinic.bffapigateway.dtos.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {
    private String productId;
    private String inventoryId;
    private String productName;
    private String supplierName;
    private String productDescription;
    private Map<String, String> name;
    private Map<String, String> description;
    private Double productPrice;
    private Integer productQuantity;
    private Double productSalePrice;
    private Status status;
    private LocalDateTime lastUpdatedAt;
    private byte[] photoData;
    private String photoType;

    private LocalDate expirationDate;

    public ProductResponseDTO(String productId, String inventoryId, String productName,
                              String supplierName, String productDescription, Double productPrice,
                              Integer productQuantity, Double productSalePrice, Status status,
                              LocalDateTime lastUpdatedAt, byte[] photoData, String photoType,
                              LocalDate expirationDate) {
        this(productId, inventoryId, productName, supplierName, productDescription, null, null,
                productPrice, productQuantity, productSalePrice, status, lastUpdatedAt,
                photoData, photoType, expirationDate);
    }

}
