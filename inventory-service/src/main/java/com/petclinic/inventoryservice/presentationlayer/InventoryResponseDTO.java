package com.petclinic.inventoryservice.presentationlayer;

import lombok.*;

import java.util.List;

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
    private String inventoryImage;
    private String inventoryBackupImage;
    private byte[] imageUploaded;
    private Boolean important;
    private List<ProductResponseDTO> products;
    private String recentUpdateMessage;

}
