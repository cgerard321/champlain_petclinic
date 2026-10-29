package com.petclinic.inventoryservice.presentationlayer;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.petclinic.inventoryservice.datalayer.Product.Status;
import lombok.*;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ProductRequestDTO {
    private String productName;
    private String supplierName;
    private String productDescription;
    private Map<String, String> name;
    private Map<String, String> description;
    private Double productPrice;
    private Integer productQuantity;
    private Double productSalePrice;
    private Status status;
    private String recentUpdateMessage;
    private byte[] photoData;
    private String photoType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
   private LocalDate expirationDate;
}