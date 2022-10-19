package com.petclinic.visits.visitsservicenew.DataLayer;

import lombok.*;
import org.springframework.data.annotation.Id;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
