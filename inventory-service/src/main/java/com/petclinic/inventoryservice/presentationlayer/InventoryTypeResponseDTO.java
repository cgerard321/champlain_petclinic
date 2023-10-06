package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryTypeResponseDTO {

    private String typeId;
    private String type;
}
