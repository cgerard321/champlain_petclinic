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
    private UUID visitId;
    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;
}
