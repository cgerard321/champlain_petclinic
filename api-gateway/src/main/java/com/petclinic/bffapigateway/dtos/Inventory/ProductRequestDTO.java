package com.petclinic.bffapigateway.dtos.Inventory;

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
    private String productDescription;
    private Double productPrice;
    private Integer productQuantity;
    private Double productSalePrice;
    private String recentUpdateMessage;


}
