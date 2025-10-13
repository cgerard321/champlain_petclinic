package com.petclinic.visits.visitsservicenew.DataLayer;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
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

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime visitDate;

    private String description;

    private String petId;

    private String practitionerId;

    private Status status;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime visitEndDate;


}
