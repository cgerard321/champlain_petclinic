package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryResponseDTO {
    private String inventoryId;
    private String  inventoryName;
    private String inventoryType;
    private String inventoryDescription;
    private List<SupplyResponseDTO> supplies;
}
