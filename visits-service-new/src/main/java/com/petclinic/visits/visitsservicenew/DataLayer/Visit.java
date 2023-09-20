package com.petclinic.visits.visitsservicenew.DataLayer;

import lombok.*;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    private String id;

    private String visitId;

    LocalDateTime visitDate;

    private String description;

    private int petId;

    private int practitionerId;

    private boolean status;

}
