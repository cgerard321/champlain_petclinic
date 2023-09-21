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
    private String inventoryType;
<<<<<<< HEAD
   private String  inventoryName;
=======
>>>>>>> 97ecf498 (MoveCodeToNewBranch)
    private String inventoryDescription;
}
