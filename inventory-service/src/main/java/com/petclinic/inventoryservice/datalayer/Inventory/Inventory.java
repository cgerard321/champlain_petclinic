package com.petclinic.inventoryservice.datalayer.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inventory {
    @Id
    private String id;
    private String inventoryId;
    private String inventoryName;
    private InventoryType inventoryType;
    private String inventoryDescription;
}
