package com.petclinic.bffapigateway.dtos.Vets;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Getter
@Builder
@NoArgsConstructor
@Setter
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
