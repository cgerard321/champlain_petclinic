package com.petclinic.bffapigateway.dtos.Cart;

import com.petclinic.bffapigateway.dtos.Files.FileDetails;
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
    public PromoCodeRequestDTO(String name, String code, double discount, String expirationDate, Boolean active) {
        this.Name = name;
        this.code = code;
        this.discount = discount;
        this.expirationDate = expirationDate;
        this.active = active;
    }

    private FileDetails image;
    private boolean removeImage;

    private String scope;
    private java.util.List<String> productIds;
    private java.util.List<String> productTypes;

}
