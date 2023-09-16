package com.petclinic.inventoryservice.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequestDTO {
    private String inventoryType;
    private String inventoryDescription;
}
