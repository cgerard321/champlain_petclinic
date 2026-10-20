package com.petclinic.cartsservice.dataaccesslayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Billing {
    private String fullName;
    private String email;
    private String address;
    private String city;
    private String province;
    private String postalCode;
}
