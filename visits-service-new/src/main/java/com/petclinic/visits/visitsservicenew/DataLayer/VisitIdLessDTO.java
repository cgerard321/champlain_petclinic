package com.petclinic.visits.visitsservicenew.DataLayer;


import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
public class VisitIdLessDTO {

    private int year;

    private int month;

    private int day;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;

    public VisitIdLessDTO(int year, int month, int day, String description, int petId, int practitionerId, boolean status) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.description = description;
        this.petId = petId;
        this.practitionerId = practitionerId;
        this.status = status;
    }
}
