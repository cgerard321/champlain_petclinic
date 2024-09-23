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
    private Double supplyPrice;
    private Integer supplyQuantity;
    private Double supplySalePrice;
}
