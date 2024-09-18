package com.petclinic.bffapigateway.dtos.Vets;


import lombok.*;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyDTO {
    private String specialtyId;
    private String name;
}
