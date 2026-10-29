package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class InventoryResponseDTO {
    private String inventoryId;
    private String inventoryCode;
    private String  inventoryName;
    private String inventoryType;
    private String inventoryDescription;
    private Map<String, String> name;
    private Map<String, String> description;
    private String inventoryImage;
    private String inventoryBackupImage;
    private byte[] imageUploaded;
    private Boolean important;
    private List<ProductResponseDTO> products;
    private String recentUpdateMessage;

}
