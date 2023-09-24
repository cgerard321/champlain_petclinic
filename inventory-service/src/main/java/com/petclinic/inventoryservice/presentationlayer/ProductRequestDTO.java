package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ProductRequestDTO {
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productQuantity;
}
