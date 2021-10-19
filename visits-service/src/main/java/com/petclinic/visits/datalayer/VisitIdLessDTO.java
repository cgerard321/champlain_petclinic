package com.petclinic.visits.datalayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisitIdLessDTO {
    private Date date;
    private String description;
    private int petId;
    private int practitionerId;
    private boolean status;
}
