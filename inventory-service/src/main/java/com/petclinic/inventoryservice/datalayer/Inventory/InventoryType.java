package com.petclinic.inventoryservice.datalayer.Inventory;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryType {
        //Might change to allow the ability to insert new types i.e adding a type entity
        //internal,
        //sales

    @Id
    private String id;
    private String typeId;
    private String type;

}

