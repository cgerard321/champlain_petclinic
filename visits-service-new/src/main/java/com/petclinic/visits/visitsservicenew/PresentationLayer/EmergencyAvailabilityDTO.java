package com.petclinic.visits.visitsservicenew.PresentationLayer;

import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
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
