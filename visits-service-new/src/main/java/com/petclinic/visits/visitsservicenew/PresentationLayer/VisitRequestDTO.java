package com.petclinic.visits.visitsservicenew.PresentationLayer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class VisitRequestDTO {
    private int year;
    private int month;
    private int day;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;
}
