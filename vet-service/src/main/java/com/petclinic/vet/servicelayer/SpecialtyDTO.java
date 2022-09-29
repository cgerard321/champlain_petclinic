package com.petclinic.vet.servicelayer;


import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class SpecialtyDTO {
    private Integer specialtyId;
    private String name;
}
