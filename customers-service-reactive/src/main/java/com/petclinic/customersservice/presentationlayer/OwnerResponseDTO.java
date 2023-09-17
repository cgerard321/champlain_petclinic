package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.PetDTO;
import com.petclinic.customersservice.data.Photo;
import lombok.*;

import java.util.List;

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
    private String photoId;
    private Photo photo;
    private List<PetDTO> pets;
}