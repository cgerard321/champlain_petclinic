package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDTO {

    private String customerId;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String province;
    private String telephone;
    private FileResponseDTO photo;
}