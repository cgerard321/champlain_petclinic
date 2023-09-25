package com.petclinic.inventoryservice.datalayer.Inventory;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Inventory {
    @Id
    private String id;
    private String inventoryId;
    private String inventoryName;
    private InventoryType inventoryType;
    private String inventoryDescription;
}
