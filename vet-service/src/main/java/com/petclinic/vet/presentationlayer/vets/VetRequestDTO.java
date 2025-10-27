package com.petclinic.vet.presentationlayer.vets;

import com.petclinic.vet.dataaccesslayer.vets.Workday;
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
    private Set<Workday> workday;
    private String workHoursJson;
    private boolean active;
    private Set<SpecialtyDTO> specialties;
    private boolean photoDefault;

}
