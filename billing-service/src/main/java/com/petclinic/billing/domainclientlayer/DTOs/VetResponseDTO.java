package com.petclinic.billing.domainclientlayer.DTOs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetResponseDTO {
    private String vetId;
    private String vetBillId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
//    private String resume;
//    //private Set<Workday> workday;
//    private boolean active;
//    private Set<SpecialtyDTO> specialties;
}
