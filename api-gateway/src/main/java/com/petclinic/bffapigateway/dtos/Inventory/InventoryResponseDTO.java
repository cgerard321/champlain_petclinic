package com.petclinic.bffapigateway.dtos.Inventory;

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
<<<<<<< HEAD
    private String inventoryType;
<<<<<<< HEAD
   private String  inventoryName;
=======
>>>>>>> 97ecf498 (MoveCodeToNewBranch)
=======
   private String  inventoryName;
    private InventoryType inventoryType;
>>>>>>> 36cc1a93 (Feat/invt cpc 757 new/part1/create inventory (#417))
    private String inventoryDescription;
}
