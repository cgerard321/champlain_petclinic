package com.petclinic.visits.visitsservicenew.DataLayer;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.UUID;

@Data
@ToString
public class Visit {

    @Id
    private String id;

    private String visitId;

    private Date date = new Date();

    private String description;

    private int petId;

    private int practitionerId;

    private boolean status;

}
