package com.petclinic.visits.visitsservicenew.DataLayer;


import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
public class VisitIdLessDTO {

    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;

    public VisitIdLessDTO(Date date, String description, int petId, int practitionerId, boolean status) {
        this.date = date;
        this.description = description;
        this.petId = petId;
        this.practitionerId = practitionerId;
        this.status = status;
    }
}
