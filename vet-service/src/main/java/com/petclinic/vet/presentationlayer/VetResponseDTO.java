package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Workday;
import com.petclinic.vet.servicelayer.SpecialtyDTO;
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
    private Set<Workday> workday;
    private boolean active;
    private Set<SpecialtyDTO> specialties;
}
