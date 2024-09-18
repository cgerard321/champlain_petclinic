package com.petclinic.bffapigateway.dtos.Vets;


import lombok.*;


@Data
@Getter
@Builder
@NoArgsConstructor
public class SpecialtyDTO {
    private String specialtyId;
    private String name;

    public SpecialtyDTO(String specialtyId, String name) {
        this.specialtyId = specialtyId;
        this.name = name;
    }
}
