package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class SupplyRequestDTO {
    private String supplyName;
    private String supplyDescription;
    private Integer supplyQuantity;
    private Double supplyPrice;
    private Double supplySalePrice;
}
