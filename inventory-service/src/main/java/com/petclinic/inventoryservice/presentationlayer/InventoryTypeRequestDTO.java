package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryTypeRequestDTO {

    private String type;
}
