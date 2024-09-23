package com.petclinic.bffapigateway.dtos.Inventory;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplyResponseDTO {
    private String id;
    private String supplyId;
    private String inventoryId;
    private String supplyName;
    private String supplyDescription;
    private Double supplyPrice;
    private Integer supplyQuantity;
    private Double supplySalePrice;
    private Status status;

}

