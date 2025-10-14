package com.petclinic.visits.visitsservicenew.BusinessLayer.Availability;

import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.TimeSlotDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface AvailabilityService {
    Flux<VetDTO> getAllVets();
    Mono<VetDTO> getVeterinarianAvailability(String vetId);
    Flux<TimeSlotDTO> getAvailableTimeSlotsForDate(String vetId, LocalDate date);
    Flux<LocalDate> getAvailableDatesForVet(String vetId, LocalDate startDate, LocalDate endDate);
}
