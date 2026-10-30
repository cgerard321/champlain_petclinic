package com.petclinic.cartsservice.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponseModel {

    private String id;
    private String Name;

    private String code;

    private  double discount;

    private LocalDateTime expirationDate;

    private  boolean isActive;
    private String imageId;
    private FileResponseDTO image;

    private String scope;
    private java.util.List<String> productIds;
    private java.util.List<String> productTypes;

}
