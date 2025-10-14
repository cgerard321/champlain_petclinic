package com.petclinic.bffapigateway.dtos.CustomerDTOs;

import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String province;
    private String telephone;
    private List<PetResponseDTO> pets;
    private String photoId;
    private FileResponseDTO photo;
}