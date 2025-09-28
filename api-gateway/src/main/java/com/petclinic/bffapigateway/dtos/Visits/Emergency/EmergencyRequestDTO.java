package com.petclinic.bffapigateway.dtos.Visits.Emergency;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyRequestDTO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime visitDate;

    private String description;

    private String petId;
    private String practitionerId;

    private UrgencyLevel urgencyLevel;


    private String emergencyType;
}
