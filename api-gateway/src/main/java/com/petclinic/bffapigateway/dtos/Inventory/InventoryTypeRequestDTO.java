package com.petclinic.bffapigateway.dtos.Inventory;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryTypeRequestDTO {

    private String type;
}
