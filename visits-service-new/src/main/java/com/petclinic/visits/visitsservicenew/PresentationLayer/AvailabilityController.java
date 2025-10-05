package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.Availability.AvailabilityService;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Slf4j
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    
    /**
     * Get ALL vets
     * GET /api/v1/availability/vets
     */
    @GetMapping(value = "/vets", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<VetDTO> getAllVetsForAvailability() {
        return availabilityService.getAllVets();
    }

    /**
     * Get vet's weekly availability schedule
     * GET /api/v1/availability/vets/{vetId}
     */
    @GetMapping(value = "/vets/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<VetDTO> getVeterinarianAvailability(@PathVariable String vetId) {
        return availabilityService.getVeterinarianAvailability(vetId);
    }

    /**
     * Get available time slots for a specific vet on a specific date
     * GET /api/v1/availability/vets/{vetId}/slots?date=2025-10-08
     */
    @GetMapping(value = "/vets/{vetId}/slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<TimeSlotDTO> getAvailableTimeSlots(
            @PathVariable String vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date) {
        LocalDate localDate = LocalDate.parse(date);
        return availabilityService.getAvailableTimeSlotsForDate(vetId, localDate);
    }

    /**
     * Get available dates for a vet within a date range
     * GET /api/v1/availability/vets/{vetId}/dates?startDate=2025-10-01&endDate=2025-10-31
     */
    @GetMapping(value = "/vets/{vetId}/dates", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<LocalDate> getAvailableDates(
            @PathVariable String vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return availabilityService.getAvailableDatesForVet(vetId, start, end);
    }
}