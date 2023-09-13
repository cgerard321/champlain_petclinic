package com.petclinic.bffapigateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {
    private String id;
    private String inventoryId;
    private Integer sku;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productQuantity;
}
