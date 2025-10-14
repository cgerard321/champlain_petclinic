package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
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
    private String province;
    private String telephone;
    private List<PetResponseDTO> pets;
    private String photoId;
    private FileResponseDTO photo;
}