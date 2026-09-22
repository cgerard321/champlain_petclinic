package com.petclinic.billing.domainclientlayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerResponseDTO {

    private String ownerId;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
//    private String photoId;
//    private Photo photo;
//    private List<PetDTO> pets;
}