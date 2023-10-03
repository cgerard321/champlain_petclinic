package com.petclinic.bffapigateway.dtos.Inventory;

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
