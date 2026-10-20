package com.petclinic.cartsservice.presentationlayer;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BillingRequestModel {
    private String customerName;
    private String email;
    private String address;
    private String city;
    private String province;
    private String postalCode;

}
