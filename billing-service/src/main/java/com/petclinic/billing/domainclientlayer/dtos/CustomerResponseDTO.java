package com.petclinic.billing.domainclientlayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String telephone;
//    private String photoId;
//    private Photo photo;
//    private List<PetDTO> pets;
}