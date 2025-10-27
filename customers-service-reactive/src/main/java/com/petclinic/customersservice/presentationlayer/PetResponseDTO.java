package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
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
    private String isActive;
    private String weight;
    private FileResponseDTO photo;
}
