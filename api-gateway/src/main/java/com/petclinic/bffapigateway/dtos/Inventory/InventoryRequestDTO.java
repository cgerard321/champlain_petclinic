package com.petclinic.bffapigateway.dtos.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequestDTO {

    private String inventoryName;
    private String inventoryType;
    private String inventoryDescription;
    private Map<String, String> name;
    private Map<String, String> description;
    private String inventoryImage;
    private String inventoryBackupImage;
    private byte[] imageUploaded;
    private Boolean important;

    public InventoryRequestDTO(String inventoryName, String inventoryType, String inventoryDescription,
                               String inventoryImage, String inventoryBackupImage, byte[] imageUploaded,
                               Boolean important) {
        this(inventoryName, inventoryType, inventoryDescription, null, null,
                inventoryImage, inventoryBackupImage, imageUploaded, important);
    }
}
