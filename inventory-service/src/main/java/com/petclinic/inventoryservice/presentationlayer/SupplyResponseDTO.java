package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplyResponseDTO {

    private String supplyId;
    private String supplyName;
    private String supplyDescription;
    private Integer supplyQuantity;
    private Double supplyPrice;
    private Double supplySalePrice;
}

