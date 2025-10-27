package com.petclinic.bffapigateway.dtos.Vets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
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
    private String workHoursJson;
    private boolean active;
    private Set<SpecialtyDTO> specialties;
}
