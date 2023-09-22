package com.petclinic.visits.visitsservicenew.PresentationLayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitResponseDTO {
    private String visitId;
    private LocalDateTime visitDate;
    private String description;
    private int petId;
    private String practitionerId;
    private boolean status;
}