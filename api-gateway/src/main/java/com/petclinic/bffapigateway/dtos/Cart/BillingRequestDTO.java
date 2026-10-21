package com.petclinic.bffapigateway.dtos.Cart;


import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequestDTO {
    private String customerName;
    private String email;
    private String address;
    private String city;
    private String province;
    private String postalCode;
}
