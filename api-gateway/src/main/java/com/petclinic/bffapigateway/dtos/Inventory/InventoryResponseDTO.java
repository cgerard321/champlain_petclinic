package com.petclinic.bffapigateway.dtos.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
    private Map<String, String> name;
    private Map<String, String> description;
    private String inventoryImage;
    private String inventoryBackupImage;
    private byte[] imageUploaded;
    private Boolean important;
    private List<ProductResponseDTO> products;
    private String recentUpdateMessage;

    public InventoryResponseDTO(String inventoryId, String inventoryCode, String inventoryType,
                                String inventoryName, String inventoryDescription, String inventoryImage,
                                String inventoryBackupImage, byte[] imageUploaded, Boolean important,
                                List<ProductResponseDTO> products, String recentUpdateMessage) {
        this(inventoryId, inventoryCode, inventoryType, inventoryName, inventoryDescription, null, null,
                inventoryImage, inventoryBackupImage, imageUploaded, important, products, recentUpdateMessage);
    }
}
