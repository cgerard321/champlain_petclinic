package com.petclinic.bffapigateway.dtos.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponseDTO {
    private String inventoryId;
    private String inventoryType;
   private String  inventoryName;
    private String inventoryDescription;
    private List<SupplyResponseDTO> supplies;
}
