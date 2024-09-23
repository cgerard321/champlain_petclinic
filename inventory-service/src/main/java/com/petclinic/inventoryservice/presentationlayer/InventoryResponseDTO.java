package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Supply.Supply;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryResponseDTO {
    private String inventoryId;
    private String  inventoryName;
    private String inventoryType;
    private String inventoryDescription;
    private List<SupplyResponseDTO> supplies;

}
