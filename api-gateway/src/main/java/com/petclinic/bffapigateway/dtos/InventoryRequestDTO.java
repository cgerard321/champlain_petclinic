package com.petclinic.bffapigateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequestDTO {
    private String name;
    private InventoryType inventoryType;
    private String inventoryDescription;
}
