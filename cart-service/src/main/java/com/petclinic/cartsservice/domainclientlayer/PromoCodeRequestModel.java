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
public class PromoCodeRequestModel {
    private String Name;

    private String code;

    private  double discount;

    private String expirationDate;

    private Boolean active;

    private FileRequestDTO image;
    private boolean removeImage;

    private String scope;
    private java.util.List<String> productIds;
    private java.util.List<String> productTypes;

}
