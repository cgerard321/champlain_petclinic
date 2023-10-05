package com.petclinic.customersservice.presentationlayer;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PetResponseDTO {

    private String ownerId;
    private String petId;
    private String name;
    private Date birthDate;
    private String petTypeId;
  //  private String photoId;
    private String isActive;

}
