package com.petclinic.visits.visitsservicenew.PresentationLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VisitsControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private VisitRepo visitRepo;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;

    @MockBean
    private EntityDtoUtil entityDtoUtil;

    String uuidVisit1 = UUID.randomUUID().toString();
    String uuidVisit2 = UUID.randomUUID().toString();
    String uuidCancelledVisit1 = UUID.randomUUID().toString();
    String uuidCancelledVisit2 = UUID.randomUUID().toString();
    String uuidVet = UUID.randomUUID().toString();
    String uuidPet = UUID.randomUUID().toString();
    String uuidPhoto = UUID.randomUUID().toString();
    String uuidOwner = UUID.randomUUID().toString();

    private final String STATUS = "CONFIRMED";
    private final int dbSize = 6;

    Set<SpecialtyDTO> set = new HashSet<>();
    Set<Workday> workdaySet = new HashSet<>();

    VetDTO vet = VetDTO.builder()
            .vetId(uuidVet)
            .vetBillId("1")
            .firstName("James")
            .lastName("Carter")
            .email("carter.james@email.com")
            .phoneNumber("(514)-634-8276 #2384")
            .imageId("1")
            .resume("Practicing since 3 years")
            .workday(workdaySet)
            .active(true)
            .specialties(set)
            .build();

    Date currentDate = new Date();
    PetResponseDTO petResponseDTO = PetResponseDTO.builder()
            .petTypeId(uuidPet)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuidPhoto)
            .ownerId(uuidOwner)
            .build();
    VisitResponseDTO archivedVisitResponseDTO = VisitResponseDTO.builder()
            .visitId("visitId4")
            .visitDate(LocalDateTime.parse("2022-12-24T13:00:00"))
            .visitEndDate(LocalDateTime.parse("2022-12-24T14:00:00"))
            .description("Dog Needs Physio-Therapy")
            .petId("0e4d8481-b611-4e52-baed-af16caa8bf8a")
            .petName("Leo")
            .visitDate(LocalDateTime.parse("2022-12-24T13:00:00"))
            .practitionerId("69f85d2e-625b-11ee-8c99-0242ac120002")
            .vetFirstName("Rafael")
            .vetLastName("Ortega")
            .vetEmail("ortegarafael@email.com")
            .vetPhoneNumber("(514)-634-8276 #2387")
            .status(Status.ARCHIVED)
            .build();

    Visit visit1 = buildVisit(uuidVisit1, "this is a dummy description", vet.getVetId());
    Visit visit2 = buildVisit(uuidVisit2, "this is a dummy description", vet.getVetId());
    Visit cancelledVisit1 = buildCancelledVisit(uuidCancelledVisit1, "this is a dummy description", vet.getVetId());
    Visit cancelledVisit2 = buildCancelledVisit(uuidCancelledVisit2, "this is a dummy description", vet.getVetId());
    Visit archivedVisit1 = buildVisitArchivedVisit("visitId1", "this is a dummy description", vet.getVetId());
    Visit archivedVisit2 = buildVisitArchivedVisit("visitId2", "this is a dummy description for archive2", vet.getVetId());
    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto(visit1.getVisitId(), vet.getVetId());
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDto(vet.getVetId());

    @BeforeEach
    void dbSetUp() {
        visitRepo.deleteAll()
                .thenMany(Flux.just(visit1, visit2, cancelledVisit1, cancelledVisit2, archivedVisit1, archivedVisit2)
                        .flatMap(visitRepo::save))
                .blockLast();  // Wait for all operations to complete
    }


    private Visit buildVisit(String uuid, String description, String vetId) {
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }

    private Visit buildCancelledVisit(String uuid, String description, String vetId) {
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.CANCELLED)
                .build();
    }

    private Visit buildVisitArchivedVisit(String uuid, String description, String vetId) {
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2022-12-24T13:00:00"))
                .visitEndDate(LocalDateTime.parse("2022-12-24T14:00:00"))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.ARCHIVED)
                .build();
    }

    private VisitResponseDTO buildVisitResponseDto(String visitId, String vetId) {
        return VisitResponseDTO.builder()
                .visitId(visitId)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }

    private VisitRequestDTO buildVisitRequestDto(String vetId) {
        return VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }

    @Test
    void getAllVisits() {
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        webTestClient
                .get()
                .uri("/visits")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list) -> assertEquals(list.size(), dbSize));
    }

    @Test
    void getVisitByVisitId() {
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        webTestClient
                .get()
                .uri("/visits/" + visit1.getVisitId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit1.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit1.getPetId())
                .jsonPath("$.description").isEqualTo(visit1.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 13:45")
                .jsonPath("$.status").isEqualTo("UPCOMING");
    }

    @Test
    void getVisitByPractitionerId() {
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));

        webTestClient
                .get()
                .uri("/visits/practitioner/" + vet.getVetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(list.size(), dbSize);
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getStatus(), visit1.getStatus());
                });
    }

    @Test
    void getVisitsForPet() {
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        webTestClient
                .get()
                .uri("/visits/pets/" + visit1.getPetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(dbSize, list.size());
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getStatus(), visit1.getStatus());
                });
    }

    @Test
    void getVisitsForStatus() {
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        visit1.setStatus(Status.CONFIRMED);

        visitRepo.save(visit1).block(); //block is telling the test to wait for the response to complete

        webTestClient
                .get()
                .uri("/visits/status/" + STATUS)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getStatus().toString(), "UPCOMING");
                });
    }

    @Test
    void updateVisit() {
        when(entityDtoUtil.toVisitEntity(any(VisitRequestDTO.class))).thenReturn(visit1);
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        webTestClient
                .put()
                .uri("/visits/" + visit1.getVisitId())
                .body(Mono.just(visitResponseDTO), VisitResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit1.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit1.getPetId())
                .jsonPath("$.description").isEqualTo(visit1.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 13:45")
                .jsonPath("$.status").isEqualTo("UPCOMING");
    }

    @Test
    void updateStatusForVisitByVisitId() {
        when(entityDtoUtil.toVisitEntity(any(VisitRequestDTO.class))).thenReturn(visit1);
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        String status = "CANCELLED";
        webTestClient
                .put()
                .uri("/visits/" + visit1.getVisitId() + "/status/" + status)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit1.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit1.getPetId())
                .jsonPath("$.description").isEqualTo(visit1.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 13:45")
                .jsonPath("$.status").isEqualTo("UPCOMING");
    }

    @Test
    void deleteVisit() {
        webTestClient
                .delete()
                .uri("/visits/" + visit1.getVisitId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteAllCanceledVisits() {
        webTestClient
                .delete()
                .uri("/visits/cancelled")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        // Verify that canceled visits were deleted
        String cancelledStatus = "CANCELLED";
        StepVerifier.create(visitRepo.findAllByStatus(cancelledStatus))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getAllArchivedVisits_returnsNotFoundWhenNoArchivedVisits() {
        visitRepo.deleteAll().block();  // Clear the repository

        webTestClient
                .get()
                .uri("/visits/archived")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody();

        StepVerifier
                .create(visitRepo.findAllByStatus(Status.ARCHIVED.toString()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getAllArchivedVisits_returnsAllArchivedVisitsFluxDTO() {
        VisitResponseDTO completedVisit = VisitResponseDTO.builder()
                .visitId("visitId4")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .visitEndDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Dog Needs Physio-Therapy")
                .petId("0e4d8481-b611-4e52-baed-af16caa8bf8a")
                .practitionerId("69f85d2e-625b-11ee-8c99-0242ac120002")
                .status(Status.ARCHIVED)
                .build();

        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(completedVisit));

        webTestClient
                .get()
                .uri("/visits/archived")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()  // Verify status is OK
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(VisitResponseDTO.class)
                .value(Assertions::assertNotNull);
    }

    @Test
    void archiveCompletedVisit_archivesVisitAndReturnsVisitResponseDTO() {
        String validCompletedVisitId = "visitId";
        Visit completedVisit = Visit.builder()
                .visitId(validCompletedVisitId)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Dog Needs Physio-Therapy")
                .petId("0e4d8481-b611-4e52-baed-af16caa8bf8a")
                .practitionerId(vet.getVetId())
                .status(Status.COMPLETED)
                .build();
        visitRepo.save(completedVisit).block();

        Visit archivedVisit = Visit.builder()
                .visitId(validCompletedVisitId)
                .visitDate(completedVisit.getVisitDate())
                .description(completedVisit.getDescription())
                .petId(completedVisit.getPetId())
                .practitionerId(completedVisit.getPractitionerId())
                .status(Status.ARCHIVED)
                .visitEndDate(completedVisit.getVisitDate().plusHours(1))
                .build();

        VisitResponseDTO archivedVisitResponseDTO = VisitResponseDTO.builder()
                .visitId(validCompletedVisitId)
                .visitDate(archivedVisit.getVisitDate())
                .visitEndDate(archivedVisit.getVisitEndDate())
                .description(archivedVisit.getDescription())
                .petId(archivedVisit.getPetId())
                .practitionerId(archivedVisit.getPractitionerId())
                .status(Status.ARCHIVED)
                .build();

        // Step 4: Mock the EntityDtoUtil to return the expected DTO
        when(entityDtoUtil.toVisitResponseDTO(any(Visit.class))).thenReturn(Mono.just(archivedVisitResponseDTO));

        webTestClient
                .put()
                .uri("/visits/completed/" + validCompletedVisitId + "/archive")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(visitRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VisitResponseDTO.class)
                .value(response -> {
                    assertEquals(validCompletedVisitId, response.getVisitId());
                    assertEquals("Dog Needs Physio-Therapy", response.getDescription());
                    assertEquals("0e4d8481-b611-4e52-baed-af16caa8bf8a", response.getPetId());
                    assertEquals(Status.ARCHIVED, response.getStatus());
                });

        StepVerifier.create(visitRepo.findByVisitId(validCompletedVisitId))
                .assertNext(updatedVisit -> {
                    assertNotNull(updatedVisit, "Archived visit should exist in the repository");
                    assertEquals(Status.ARCHIVED, updatedVisit.getStatus(), "Visit status should be ARCHIVED");
                })
                .verifyComplete();
    }
//    @Test
//    void archiveCompletedVisit_withInvalidVisitId_returnsNotFound() {
//        String invalidVisitId = "invalidId";
//        VisitRequestDTO visitRequestDTO = buildVisitRequestDTO(UUID.randomUUID().toString());
//
//        when(visitService.archiveCompletedVisit(eq(invalidVisitId), any(Mono.class)))
//                .thenReturn(Mono.error(new NotFoundException("No visit was found with visitId: " + invalidVisitId)));
//
//        webTestClient
//                .put()
//                .uri("/visits/completed/" + invalidVisitId + "/archive")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
//                .exchange()
//                .expectStatus().isNotFound();
//
//        verify(visitService, times(1)).archiveCompletedVisit(eq(invalidVisitId), any(Mono.class));
//    }

//    @Test
//    void archiveCompletedVisit_withInvalidVisitId_returnsBadRequest() {
//        String invalidVisitId = "invalidId";
//        VisitRequestDTO visitRequestDTO = buildVisitRequestDTO(UUID.randomUUID().toString());
//
//        when(visitService.archiveCompletedVisit(eq(invalidVisitId), any(Mono.class)))
//                .thenReturn(Mono.error(new NotFoundException("No visit was found with visitId: " + invalidVisitId)));
//
//        webTestClient
//                .put()
//                .uri("/visits/completed/" + invalidVisitId + "/archive")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
//                .exchange()
//                .expectStatus().isNotFound();
//
//        verify(visitService, times(1)).archiveCompletedVisit(eq(invalidVisitId), any(Mono.class));
//    }
//
//    @Test
//    void archiveCompletedVisit_withEmptyVisitId_returnsBadRequest() {
//        VisitRequestDTO visitRequestDTO = buildVisitRequestDTO(UUID.randomUUID().toString());
//
//        webTestClient
//                .put()
//                .uri("/visits/completed//archive")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
//                .exchange()
//                .expectStatus().isBadRequest();
//
//        verify(visitService, never()).archiveCompletedVisit(anyString(), any(Mono.class));
//    }

//                .value((visit) -> {
//                    assertNotNull(visit);
//                    assertEquals(1, visit.size());
//                    assertEquals(visit.get(0).getVisitId(), completedVisit.getVisitId());
//                    assertEquals(visit.get(0).getPractitionerId(), completedVisit.getPractitionerId());
//                    assertEquals(visit.get(0).getPetId(), completedVisit.getPetId());
//                    assertEquals(visit.get(0).getDescription(), completedVisit.getDescription());
//                    assertEquals(visit.get(0).getVisitDate(), completedVisit.getVisitDate());
//                    assertEquals(visit.get(0).getVisitEndDate(), completedVisit.getVisitEndDate());
//                    assertEquals(visit.get(0).getStatus(), Status.ARCHIVED);
//                });


    @Test
    void updateVisitStatus_ShouldSucceed_WhenStatusUpdatedToCancelled() {
        String visitId = "visitId9";
        String status = "CANCELLED";

        VisitResponseDTO cancelledVisitResponse = VisitResponseDTO.builder()
                .visitId(visitId)
                .visitDate(LocalDateTime.now())
                .description("Test visit with cancelled status")
                .petId("3")
                .practitionerId(vet.getVetId())  // Ensure vet.getVetId() is not null
                .status(Status.CANCELLED)         // Expected status after update
                .build();

        Visit cancelledVisit = Visit.builder()
                .visitId(visitId)
                .visitDate(LocalDateTime.now())
                .description("Test visit with cancelled status")
                .petId("3")
                .practitionerId(vet.getVetId())  // Ensure vet.getVetId() is not null
                .status(Status.UPCOMING)         // Initial status
                .build();

        // Save the initial visit with status UPCOMING
        visitRepo.save(cancelledVisit).block();

        // Verify initial status is UPCOMING
        StepVerifier.create(visitRepo.findByVisitId(visitId))
                .expectNextMatches(visit -> visit.getStatus() == Status.UPCOMING)
                .verifyComplete();

        // Mock entityDtoUtil to return the expected VisitResponseDTO when converting to response format
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(cancelledVisitResponse));

        // Perform the status update via WebTestClient
        webTestClient.patch()
                .uri("/visits/{visitId}/{status}", visitId, status)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(VisitResponseDTO.class)
                .value(Assertions::assertNotNull)   // Ensure the response body is not null
                .value(response -> assertEquals(Status.CANCELLED, response.getStatus()));  // Assert the status is updated to CANCELLED

        // Verify that the status has been updated to CANCELLED in the repository
        StepVerifier.create(visitRepo.findByVisitId(visitId))
                .expectNextMatches(visit -> visit.getStatus() == Status.CANCELLED)
                .verifyComplete();
    }


    // Test for the NOT_FOUND scenario (when visit does not exist)
    @Test
    void updateVisitStatus_ShouldReturnNotFound_WhenVisitDoesNotExist() {
        String visitId = "nonExistentVisitId";
        String status = "CANCELLED";

        // Verify that no visit exists with the provided visitId
        StepVerifier
                .create(visitRepo.findByVisitId(visitId))
                .expectNextCount(0) // No visit should exist with this ID
                .verifyComplete();

        // Perform the status update via WebTestClient, expecting a 404 Not Found response
        webTestClient.patch()
                .uri("/visits/{visitId}/{status}", visitId, status)
                .exchange()
                .expectStatus().isNotFound();

        // Re-check that no new visits have been created with this ID
        StepVerifier
                .create(visitRepo.findByVisitId(visitId))
                .expectNextCount(0) // Still no visit should exist with this ID
                .verifyComplete();
    }

}