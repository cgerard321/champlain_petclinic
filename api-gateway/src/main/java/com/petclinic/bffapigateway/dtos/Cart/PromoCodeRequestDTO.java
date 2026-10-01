package com.petclinic.bffapigateway.dtos.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequestDTO {
    private String Name;

    private String code;

    private  double discount;

    private String expirationDate;

    // active goes in request so we can toggle a promo
    private Boolean active;
}
