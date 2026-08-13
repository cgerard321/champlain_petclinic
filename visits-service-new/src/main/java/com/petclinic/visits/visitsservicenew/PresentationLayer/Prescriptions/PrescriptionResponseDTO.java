package com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionResponseDTO {
    private String prescriptionId;
    private LocalDate date;
    private String vetFirstName;
    private String vetLastName;
    private String ownerFirstName;
    private String ownerLastName;
    private String petName;
    private String directions;
    private List<MedicationDTO> medications;
}