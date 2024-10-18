package com.petclinic.bffapigateway.dtos.Visits.Emergency;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyResponseDTO {

    private String visitEmergencyId;

    LocalDateTime visitDate;

    private String description;


    private String petId;
    private String petName;
    private Date petBirthDate;
    private String practitionerId;
    private String vetFirstName;
    private String vetLastName;
    private String vetEmail;
    private String vetPhoneNumber;


    private UrgencyLevel urgencyLevel;


    private String emergencyType;
}
