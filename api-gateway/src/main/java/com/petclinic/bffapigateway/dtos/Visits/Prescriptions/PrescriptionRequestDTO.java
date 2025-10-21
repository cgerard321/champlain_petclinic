package com.petclinic.bffapigateway.dtos.Visits.Prescriptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionRequestDTO {
    private String medication;
    private String dosage;
    private String instructions;
    private Integer quantity;
    private Integer refills;
    private String prescribedBy;
    private String petId;
    private LocalDate issueDate;
    private LocalDate expiresAt;
}