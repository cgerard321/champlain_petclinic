package com.petclinic.visits.visitsservicenew.DataLayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

/**
 * Visit Entity
 */
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    private String id;

    private String visitId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime visitDate;

    private String description;

    private String petId;

    private String practitionerId;

    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime visitEndDate;

    private boolean reminder;

    private String ownerEmail;
}
