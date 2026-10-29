package com.petclinic.bffapigateway.dtos.Addresses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressSuggestionDTO {
    private String address;
    private String city;
    private String province;
}