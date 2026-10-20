package com.petclinic.cartsservice.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequestModel {
    private String fullName;
    private String email;
    private String address;
    private String city;
    private String province;
    private String postalCode;

}
