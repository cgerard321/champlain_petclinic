package com.petclinic.bffapigateway.dtos.Cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponseDTO {
    private String id;
    private String Name;

    private String code;

    private  double discount;

    private LocalDateTime expirationDate;

    private  boolean isActive;
}
