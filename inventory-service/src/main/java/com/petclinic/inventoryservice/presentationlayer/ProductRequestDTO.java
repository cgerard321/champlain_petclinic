package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Product.Status;
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
    private Double productSalePrice;
    private Status status;
    private String recentUpdateMessage;
}