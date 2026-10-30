package com.petclinic.bffapigateway.dtos.Inventory;

import java.time.LocalDate;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequestDTO {
    private String productName;
    private String supplierName;
    private String productDescription;
    private Map<String, String> name;
    private Map<String, String> description;
    private Double productPrice;
    private Integer productQuantity;
    private Double productSalePrice;
    private String recentUpdateMessage;
    private byte[] photoData;
    private String photoType;

    private LocalDate expirationDate;

    public ProductRequestDTO(String productName, String supplierName, String productDescription,
                             Double productPrice, Integer productQuantity, Double productSalePrice,
                             String recentUpdateMessage, byte[] photoData, String photoType,
                             LocalDate expirationDate) {
        this(productName, supplierName, productDescription, null, null, productPrice,
                productQuantity, productSalePrice, recentUpdateMessage, photoData, photoType, expirationDate);
    }


}
