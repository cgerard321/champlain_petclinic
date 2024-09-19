package com.petclinic.bffapigateway.dtos.Vets;


import lombok.*;



@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Data

public class SpecialtyDTO {
    private String specialtyId;
    private String name;

    public SpecialtyDTO(String specialtyId, String name) {
        this.specialtyId = specialtyId;
        this.name = name;
    }
}
