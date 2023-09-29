package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import lombok.*;

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
}
