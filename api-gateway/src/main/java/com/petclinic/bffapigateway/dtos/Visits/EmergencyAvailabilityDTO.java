package com.petclinic.bffapigateway.dtos.Visits;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmergencyAvailabilityDTO {
    private VetDTO veterinarian;
    private List<TimeSlotDTO> availableSlots;

}
