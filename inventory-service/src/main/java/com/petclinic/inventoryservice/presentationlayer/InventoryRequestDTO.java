package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
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
}
