package com.petclinic.inventoryservice.datalayer.Inventory;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryName {

    @Id
    private String id;
    private String nameId;
    private String name;

}

