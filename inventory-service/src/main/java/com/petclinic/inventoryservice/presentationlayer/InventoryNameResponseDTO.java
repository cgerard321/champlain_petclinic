package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryNameResponseDTO {

    private String nameId;
    private String name;
}
