package com.petclinic.visits.visitsservicenew.DataLayer;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor

public class VisitDTO {

    private String visitId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;

    public VisitDTO(String visitId, Date date, String description, int petId, int practitionerId, boolean status) {
        this.visitId = visitId;
        this.date = date;
        this.description = description;
        this.petId = petId;
        this.practitionerId = practitionerId;
        this.status = status;
    }
}
