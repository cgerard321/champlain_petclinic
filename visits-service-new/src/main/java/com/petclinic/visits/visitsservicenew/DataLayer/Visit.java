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

    private int year;

    private int month;

    private int day;

    private String description;

    private int petId;

    private int practitionerId;

    private boolean status;

}
