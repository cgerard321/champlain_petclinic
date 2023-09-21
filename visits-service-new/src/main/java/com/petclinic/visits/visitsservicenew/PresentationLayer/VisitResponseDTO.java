package com.petclinic.visits.visitsservicenew.PresentationLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
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
    private int practitionerId;
    private Status status;
}