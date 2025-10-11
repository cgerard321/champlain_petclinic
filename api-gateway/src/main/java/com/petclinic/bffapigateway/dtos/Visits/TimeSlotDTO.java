package com.petclinic.bffapigateway.dtos.Visits;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;
}

