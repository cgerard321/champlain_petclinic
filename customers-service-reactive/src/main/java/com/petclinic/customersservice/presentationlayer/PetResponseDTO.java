package com.petclinic.customersservice.presentationlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PetResponseDTO {

    private String ownerId;
    private String name;
    private Date birthDate;
    private String petTypeId;
    private String photoId;

}
