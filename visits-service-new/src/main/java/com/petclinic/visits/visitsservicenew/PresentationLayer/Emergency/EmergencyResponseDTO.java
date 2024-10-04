package com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.UrgencyLevel;
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


    private String petName;


    private UrgencyLevel urgencyLevel;


    private String emergencyType;
}
