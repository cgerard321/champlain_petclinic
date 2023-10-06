package com.petclinic.billing.datalayer;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetRequestDTO {
    private String vetId;
    private String vetBillId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String resume;
    //private Set<Workday> workday;
    private boolean active;
    private Set<SpecialtyDTO> specialties;
}
