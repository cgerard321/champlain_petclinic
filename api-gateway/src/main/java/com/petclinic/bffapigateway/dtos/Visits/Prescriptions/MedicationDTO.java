package com.petclinic.bffapigateway.dtos.Visits.Prescriptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationDTO {
    private String name;
    private String strength;
    private String dosage;
    private String frequency;
    private Integer quantity;
}
