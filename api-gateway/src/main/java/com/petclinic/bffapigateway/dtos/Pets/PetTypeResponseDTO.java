package com.petclinic.bffapigateway.dtos.Pets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetTypeResponseDTO {

    private String petTypeId;
    private String name;
    private String petTypeDescription;
}
