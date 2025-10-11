package com.petclinic.visits.visitsservicenew.BusinessLayer.Availability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.TimeSlotDTO;
import com.petclinic.visits.visitsservicenew.Utils.WorkHourParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final VetsClient vetsClient;
    private final VisitRepo visitRepo;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<VetDTO> getAllVets() {
        return vetsClient.getAllVets();
    }

    @Override
    public Mono<VetDTO> getVeterinarianAvailability(String vetId) {

        return vetsClient.getVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No veterinarian found with vetId: " + vetId)));
    }

    @Override
    public Flux<TimeSlotDTO> getAvailableTimeSlotsForDate(String vetId, LocalDate date) {

        return vetsClient.getVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No veterinarian found with vetId: " + vetId)))
                .flatMapMany(vet -> {
                    // Parse workHoursJson to get the map of days to hour slots
                    Map<String, List<String>> workHoursMap = parseWorkHoursJson(vet.getWorkHoursJson());

                    if (workHoursMap == null || workHoursMap.isEmpty()) {
                        return Flux.empty();
                    }

                    // Get the day of week
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                    // Get work hours for this specific day
                    List<String> workHoursList = workHoursMap.get(dayName);

                    if (workHoursList == null || workHoursList.isEmpty()) {
                        return Flux.empty();
                    }

                    // Convert work hour enums to time slots
                    return Flux.fromIterable(workHoursList)
                            .map(workHourEnum -> {
                                LocalTime startTime = WorkHourParser.getStartTime(workHourEnum);
                                LocalTime endTime = WorkHourParser.getEndTime(workHourEnum);

                                return new TimeSlotDTO(
                                        LocalDateTime.of(date, startTime),
                                        LocalDateTime.of(date, endTime),
                                        true
                                );
                            })
                            .collectList()
                            .flatMapMany(slots -> markBookedSlots(slots, vetId, date));
                });
    }

    @Override
    public Flux<LocalDate> getAvailableDatesForVet(String vetId, LocalDate startDate, LocalDate endDate) {

        return vetsClient.getVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No veterinarian found with vetId: " + vetId)))
                .flatMapMany(vet -> {
                    // Parse workHoursJson
                    Map<String, List<String>> workHoursMap = parseWorkHoursJson(vet.getWorkHoursJson());

                    if (workHoursMap == null || workHoursMap.isEmpty()) {
                        return Flux.empty();
                    }

                    // Get all days the vet works (keys in the workHours map)
                    List<String> workingDayNames = List.copyOf(workHoursMap.keySet());

                    // Convert day names to DayOfWeek enums
                    List<DayOfWeek> workingDays = workingDayNames.stream()
                            .map(dayName -> DayOfWeek.valueOf(dayName.toUpperCase()))
                            .toList();

                    // Generate dates that match working days
                    return Flux.fromStream(
                            startDate.datesUntil(endDate.plusDays(1))
                                    .filter(date -> workingDays.contains(date.getDayOfWeek()))
                    );
                });
    }


    private Map<String, List<String>> parseWorkHoursJson(String workHoursJson) {
        if (workHoursJson == null || workHoursJson.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(
                    workHoursJson,
                    new TypeReference<Map<String, List<String>>>() {}
            );
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse workHoursJson. Returning empty schedule. Invalid JSON: '{}'. Error: {}",
                    workHoursJson, e.getMessage());
            return Map.of();
        }
    }


    private Flux<TimeSlotDTO> markBookedSlots(List<TimeSlotDTO> slots, String vetId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return visitRepo.findByPractitionerIdAndVisitDateBetween(vetId, startOfDay, endOfDay)
                .collectList()
                .flatMapMany(existingVisits -> {
                    // Mark slots as unavailable if they conflict with existing visits
                    for (TimeSlotDTO slot : slots) {
                        for (Visit visit : existingVisits) {
                            if (isSlotBooked(slot, visit)) {
                                slot.setAvailable(false);
                                break;
                            }
                        }
                    }
                    return Flux.fromIterable(slots);
                });
    }


    private boolean isSlotBooked(TimeSlotDTO slot, Visit visit) {
        LocalDateTime visitTime = visit.getVisitDate();

        // Check if visit time falls within this slot
        return !visitTime.isBefore(slot.getStartTime()) &&
                visitTime.isBefore(slot.getEndTime());
    }
}
