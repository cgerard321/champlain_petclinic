package com.petclinic.bffapigateway.dtos.Vets;

import lombok.*;

import java.util.Set;

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
    private String resume;
<<<<<<< HEAD
    private Set<Workday> workday;
=======
    private String workday;
>>>>>>> ceef8eff (VetDTO split in apigateway)
    private boolean active;
    private Set<SpecialtyDTO> specialties;
}
