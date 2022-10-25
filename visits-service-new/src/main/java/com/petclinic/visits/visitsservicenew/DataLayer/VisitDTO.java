package com.petclinic.visits.visitsservicenew.DataLayer;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
public class VisitDTO {

    private String visitId;

    private int year;

    private int month;

    private int day;
    private String description;
    private String petId;
    private int practitionerId;
    private boolean status;

    public VisitDTO(String visitId, int year, int month, int day, String description, String petId, int practitionerId, boolean status) {
        this.visitId = visitId;
        this.year = year;
        this.month = month;
        this.day = day;
        this.description = description;
        this.petId = petId;
        this.practitionerId = practitionerId;
        this.status = status;
    }
}
