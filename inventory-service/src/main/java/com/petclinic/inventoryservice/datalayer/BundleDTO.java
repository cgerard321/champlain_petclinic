package com.petclinic.inventoryservice.datalayer;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleDTO {

    private String bundleUUID;
    private String item;
    private int quantity;
    private Date expiryDate;

    public BundleDTO(String item,int quantity,Date expiryDate) {
        this.item = item;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }
}
