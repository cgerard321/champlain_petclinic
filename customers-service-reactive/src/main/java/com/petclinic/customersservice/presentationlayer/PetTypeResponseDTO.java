package com.petclinic.customersservice.presentationlayer;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetTypeResponseDTO {

    private String petTypeId;
    private String name;
    private String petTypeDescription;
}
