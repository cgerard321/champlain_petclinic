package com.petclinic.customersservice.presentationlayer;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetTypeRequestDTO {

    private String name;
    private String petTypeDescription;
}
