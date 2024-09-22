package com.petclinic.inventoryservice.datalayer.Inventory;

import com.petclinic.inventoryservice.datalayer.Supply.Supply;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Document(collection = "inventories")
public class Inventory {

    @Id
    private String id;
    private String inventoryId;
    private String inventoryName;
    private String inventoryType;
    private String inventoryDescription;
    private List<Supply> supplies = new ArrayList<>();


    public void addSupply(Supply supply) {
        if (this.supplies == null) {
            this.supplies = new ArrayList<>();
        }
        this.supplies.add(supply);
    }
}