package com.petclinic.bffapigateway.dtos.Visits.Emergency;


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
    LocalDateTime visitDate;

    private String description;

    private String petName;

    private UrgencyLevel urgencyLevel;


    private String emergencyType;
}
