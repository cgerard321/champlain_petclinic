package com.petclinic.inventoryservice.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponseDTO {
    private String inventoryId;
    private InventoryType inventoryType;

   private String  inventoryName;


    private String inventoryDescription;
}
