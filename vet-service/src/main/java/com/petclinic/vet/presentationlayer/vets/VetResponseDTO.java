package com.petclinic.vet.presentationlayer.vets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.petclinic.vet.dataaccesslayer.vets.Workday;
import com.petclinic.vet.presentationlayer.files.FileResponseDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VetResponseDTO {
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
    private FileResponseDTO photo;
}
