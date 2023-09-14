package com.petclinic.inventoryservice.presentationlayer;

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
    private String sku;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productQuantity;
}
