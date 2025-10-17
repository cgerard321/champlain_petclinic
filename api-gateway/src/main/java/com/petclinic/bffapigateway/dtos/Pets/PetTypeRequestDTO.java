package com.petclinic.bffapigateway.dtos.Pets;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetTypeRequestDTO {

    private String name;
    private String petTypeDescription;
}
