package com.petclinic.bffapigateway.dtos.Vets;


import lombok.*;


@Data
@Builder
@NoArgsConstructor
@Getter
public class SpecialtyDTO {
    private String specialtyId;
    private String name;

    public SpecialtyDTO(String specialtyId, String name) {
        this.specialtyId = specialtyId;
        this.name = name;
    }

}
