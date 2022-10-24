package com.petclinic.inventoryservice.datalayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.UUID;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bundle {

    @Id
    private String id;
    private String bundleUUID;
    private String item;
    private int quantity;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date expiryDate;
}
