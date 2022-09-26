package com.petclinic.visits.visitsservicenew.DataLayer;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
public class VisitDTO {

    private String visitId;

    private int year;

    private int month;

    private int day;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;

    public VisitDTO(String visitId, int year, int month, int day, String description, int petId, int practitionerId, boolean status) {
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
