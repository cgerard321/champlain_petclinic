package com.petclinic.billing.datalayer;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SpecialtyDTO {
    private String specialtyId;
    private String name;
}
