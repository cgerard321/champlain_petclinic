package com.petclinic.vet.servicelayer;


import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpecialtyDTO {
    private String specialtyId;
    private String name;
}
