package com.petclinic.customersservice.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerRequestDTO {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private String email;
    //private List<PetResponseDTO> pets;
}
