package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Product.Status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ProductResponseDTO {
    private String productId;
    private String inventoryId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productQuantity;
    private Double productSalePrice;
    private Double productProfit;
    private Status status;
    private LocalDateTime lastUpdatedAt;
}
