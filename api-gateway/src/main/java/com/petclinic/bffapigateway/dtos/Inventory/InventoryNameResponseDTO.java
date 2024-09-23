package com.petclinic.bffapigateway.dtos.Inventory;

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


