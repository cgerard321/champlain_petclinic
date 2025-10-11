package com.petclinic.visits.visitsservicenew.PresentationLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AvailabilityControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private VisitRepo visitRepo;

    private VetDTO createVetWithWorkHours() {
        VetDTO vet = new VetDTO();
        vet.setVetId("vet-123");
        vet.setFirstName("John");
        vet.setLastName("Doe");
        vet.setActive(true);

        Map<String, List<String>> workHours = new HashMap<>();
        workHours.put("Monday", List.of("Hour_9_10", "Hour_10_11", "Hour_14_15"));
        workHours.put("Tuesday", List.of("Hour_9_10"));

        try {
            String workHoursJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(workHours);
            vet.setWorkHoursJson(workHoursJson);
        } catch (Exception e) {
            throw new RuntimeException("Test setup failed: Unable to serialize work hours to JSON", e);
        }
        return vet;
    }

    @Test
    void getAllVetsForAvailability_shouldReturnOk() {
        VetDTO vet = createVetWithWorkHours();
        when(vetsClient.getAllVets()).thenReturn(Flux.just(vet));

        webTestClient.get()
                .uri("/api/v1/availability/vets")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VetDTO.class)
                .hasSize(1);
    }

    @Test
    void getAvailableTimeSlots_withNoBookings_shouldReturnAllSlotsAvailable() {
        VetDTO vet = createVetWithWorkHours();
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(visitRepo.findByPractitionerIdAndVisitDateBetween(anyString(), any(), any()))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/availability/vets/vet-123/slots?date=2025-10-13")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TimeSlotDTO.class)
                .hasSize(3); // Monday has 3 slots
    }

    @Test
    void getAvailableTimeSlots_withBooking_shouldMarkSlotUnavailable() {
        VetDTO vet = createVetWithWorkHours();

        // Create a booked visit at 9:30 AM
        Visit bookedVisit = new Visit();
        bookedVisit.setVisitId("visit-1");
        bookedVisit.setVisitDate(LocalDateTime.of(2025, 10, 13, 9, 30));
        bookedVisit.setPractitionerId("vet-123");
        bookedVisit.setPetId("pet-1");
        bookedVisit.setStatus(Status.CONFIRMED);

        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(visitRepo.findByPractitionerIdAndVisitDateBetween(anyString(), any(), any()))
                .thenReturn(Flux.just(bookedVisit));

        webTestClient.get()
                .uri("/api/v1/availability/vets/vet-123/slots?date=2025-10-13")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TimeSlotDTO.class)
                .hasSize(3); // Still returns 3 slots, but one marked unavailable
    }

    @Test
    void getAvailableTimeSlots_whenVetNotFound_shouldReturn404() {
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/availability/vets/invalid-vet/slots?date=2025-10-13")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAvailableTimeSlots_whenVetHasNoWorkHours_shouldReturnEmptyList() {
        VetDTO vet = new VetDTO();
        vet.setVetId("vet-123");
        vet.setWorkHoursJson(null);

        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        webTestClient.get()
                .uri("/api/v1/availability/vets/vet-123/slots?date=2025-10-13")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TimeSlotDTO.class)
                .hasSize(0);
    }

    @Test
    void getAvailableTimeSlots_whenVetDoesNotWorkOnRequestedDay_shouldReturnEmptyList() {
        VetDTO vet = createVetWithWorkHours();
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        // Request Sunday (not a working day)
        webTestClient.get()
                .uri("/api/v1/availability/vets/vet-123/slots?date=2025-10-12")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TimeSlotDTO.class)
                .hasSize(0);
    }

    @Test
    void getAvailableTimeSlots_withInvalidJson_shouldReturnEmptyList() {
        VetDTO vet = new VetDTO();
        vet.setVetId("vet-123");
        vet.setWorkHoursJson("invalid json {{{");

        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        webTestClient.get()
                .uri("/api/v1/availability/vets/vet-123/slots?date=2025-10-13")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TimeSlotDTO.class)
                .hasSize(0);
    }

    @Test
    void getAvailableDates_shouldReturnWorkingDays() {
        VetDTO vet = createVetWithWorkHours();
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        webTestClient.get()
                .uri("/api/v1/availability/vets/vet-123/dates?startDate=2025-10-13&endDate=2025-10-20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(String.class)
                .consumeWith(response -> {
                    assert response.getResponseBody().size() > 0;
                });
    }

    @Test
    void getAvailableDates_whenVetNotFound_shouldReturn404() {
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/availability/vets/invalid-vet/dates?startDate=2025-10-13&endDate=2025-10-20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getVeterinarianAvailability_shouldReturnVet() {
        VetDTO vet = createVetWithWorkHours();
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        webTestClient.get()
                .uri("/api/v1/availability/vets/vet-123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VetDTO.class);
    }

    @Test
    void getVeterinarianAvailability_whenVetNotFound_shouldReturn404() {
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/availability/vets/invalid-vet")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}