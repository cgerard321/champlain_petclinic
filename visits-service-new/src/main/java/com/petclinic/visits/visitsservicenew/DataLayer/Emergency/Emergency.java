package com.petclinic.visits.visitsservicenew.DataLayer.Emergency;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Emergency {

    @Id
    private String id;

    private String visitEmergencyId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime visitDate;

    private String description;

    private String petName;

    private UrgencyLevel urgencyLevel;


    private String emergencyType;
}
