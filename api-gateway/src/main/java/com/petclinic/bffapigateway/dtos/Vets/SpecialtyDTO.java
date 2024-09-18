package com.petclinic.bffapigateway.dtos.Vets;


import lombok.*;


@Data
@Builder
public class SpecialtyDTO {
    private String specialtyId;
    private String name;
}
