package com.petclinic.visits.datalayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisitDTO {
    private String visitId;
    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;

    public VisitDTO(Date date, String description, int petId, int practitionerId, boolean status){
        this.date = date;
        this.description = description;
        this.petId = petId;
        this.practitionerId = practitionerId;
        this.status = status;
    }
}
