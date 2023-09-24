package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ProductResponseDTO {
    private String id;
    private String productId;
    private String inventoryId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productQuantity;
}
