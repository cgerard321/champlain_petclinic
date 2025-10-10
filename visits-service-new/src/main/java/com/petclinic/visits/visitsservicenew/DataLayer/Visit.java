package com.petclinic.visits.visitsservicenew.DataLayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

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
