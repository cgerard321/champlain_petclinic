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
    private final int dbSize = 4;

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

    Visit visit1 = buildVisit(uuidVisit1, "this is a dummy description", vet.getVetId());
    Visit visit2 = buildVisit(uuidVisit2, "this is a dummy description", vet.getVetId());
    Visit cancelledVisit1 = buildCancelledVisit(uuidCancelledVisit1, "this is a dummy description", vet.getVetId());
    Visit cancelledVisit2 = buildCancelledVisit(uuidCancelledVisit2, "this is a dummy description", vet.getVetId());

    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto(visit1.getVisitId(), vet.getVetId());
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDto(vet.getVetId());

    @BeforeEach
    void dbSetUp() {
        Publisher<Visit> visitPublisher = visitRepo.deleteAll()
                .thenMany(visitRepo.save(visit1)
                        .thenMany(visitRepo.save(visit2)
                                .thenMany(visitRepo.save(cancelledVisit1)
                                        .thenMany(visitRepo.save(cancelledVisit2)))));

        StepVerifier.create(visitPublisher).expectNextCount(1).verifyComplete();
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
    void deleteCompletedVisitByValidVisitId_Return_NoContent() {
        // Arrange: Create and save a COMPLETED visit
        String validVisitId = "visitId3";
        Visit completedVisit = Visit.builder()
                .visitId(validVisitId)
                .visitDate(LocalDateTime.now())
                .description("Completed visit for deletion test")
                .petId("3")
                .practitionerId(vet.getVetId())
                .status(Status.COMPLETED)
                .build();

        visitRepo.save(completedVisit).block();

        // Verify the visit exists and has a status of COMPLETED
        StepVerifier
                .create(visitRepo.findByVisitId(validVisitId))
                .expectNextMatches(visit -> visit.getStatus().equals(Status.COMPLETED))
                .verifyComplete();

        // Act: Delete the completed visit
        webTestClient
                .delete()
                .uri("/visits/completed/" + validVisitId)
                .exchange()
                .expectStatus().isNoContent();

        // Assert: Verify the visit is deleted
        StepVerifier
                .create(visitRepo.findByVisitId(validVisitId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void deleteCompletedVisitByInvalidVisitId_Return_NotFound() {
        String visitId = "InvalidId";
        webTestClient
                .delete()
                .uri("/visits/completed/" + visitId)
                .exchange()
                .expectStatus().isNotFound();

        StepVerifier
                .create(visitRepo.findByVisitId(visitId))
                .expectNextCount(0) //confirms that visit does not exist in the database
                .verifyComplete();
    }

    @Test
    void deleteCompletedVisitByValidVisitId_Where_StatusIsNotCompleted_Return_NotFound() {
        String validId = "ValidId";
        Visit CancelledVisit = Visit.builder()
                .visitId(validId)
                .visitDate(LocalDateTime.now())
                .description("Completed visit for deletion test")
                .petId("3")
                .practitionerId(vet.getVetId())
                .status(Status.CANCELLED)
                .build();

        visitRepo.save(CancelledVisit).block();

        StepVerifier
                .create(visitRepo.findByVisitId(validId))
                .expectNextMatches(visit -> visit.getStatus() == Status.CANCELLED)
                .verifyComplete();

        webTestClient
                .delete()
                .uri("/visits/completed/" + CancelledVisit.getVisitId())
                .exchange()
                .expectStatus().isNotFound();

        StepVerifier
                .create(visitRepo.findByVisitId(visit1.getVisitId()))
                .expectNextCount(1)
                .verifyComplete();
    }

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