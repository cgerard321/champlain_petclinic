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

    @GetMapping(value = "/vets", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<VetDTO> getAllVetsForAvailability() {
        return availabilityService.getAllVets();
    }

    @GetMapping(value = "/vets/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<VetDTO> getVeterinarianAvailability(@PathVariable String vetId) {
        return availabilityService.getVeterinarianAvailability(vetId);
    }

    @GetMapping(value = "/vets/{vetId}/slots", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<TimeSlotDTO> getAvailableTimeSlots(
            @PathVariable String vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date) {
        LocalDate localDate = LocalDate.parse(date);
        return availabilityService.getAvailableTimeSlotsForDate(vetId, localDate);
    }


    @GetMapping(value = "/vets/{vetId}/dates", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<LocalDate> getAvailableDates(
            @PathVariable String vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return availabilityService.getAvailableDatesForVet(vetId, startDate, endDate);
    }
}