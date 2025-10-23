package com.petclinic.visits.visitsservicenew.PresentationLayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileRequestDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitResponseDTO {
    private String visitId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime visitDate;
    private String description;
    private String petId;
    private String petName;
    private Date petBirthDate;
    private String practitionerId;
    private String vetFirstName;
    private String vetLastName;
    private String vetEmail;
    private String vetPhoneNumber;
    private Status status;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime visitEndDate;
    @JsonProperty("isEmergency")
    private Boolean isEmergency;
    private FileResponseDTO prescription;

}