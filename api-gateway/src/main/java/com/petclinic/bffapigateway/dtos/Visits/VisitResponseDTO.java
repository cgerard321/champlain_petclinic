package com.petclinic.bffapigateway.dtos.Visits;

import com.fasterxml.jackson.annotation.JsonFormat;
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
//    private String visitId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
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
    private String visitId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime visitEndDate;
}