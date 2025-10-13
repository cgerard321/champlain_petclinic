package com.petclinic.bffapigateway.dtos.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponseDTO {
    private String inventoryId;
    private String inventoryCode;
    private String inventoryType;
    private String inventoryName;
    private String inventoryDescription;
    private String inventoryImage;
    private String inventoryBackupImage;
    private byte[] imageUploaded;
    private Boolean important;
    private List<ProductResponseDTO> products;
    private String recentUpdateMessage;
}
