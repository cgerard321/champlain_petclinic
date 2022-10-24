
package com.petclinic.bffapigateway.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class BundleDetails {
    private String bundleUUID;
    private String item;
    private int quantity;
    private Date expiryDate;
}
