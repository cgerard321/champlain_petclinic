package com.petclinic.bffapigateway.presentationlayer.v2.visit;

import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.UrgencyLevel;
import com.petclinic.bffapigateway.dtos.Visits.Status;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.BFFApiGatewayController;
import com.petclinic.bffapigateway.presentationlayer.v2.VisitController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = VisitController.class)
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {
        VisitController.class,
        VisitsServiceClient.class
})
public class VisitControllerUnitTest {
    @Autowired
    private WebTestClient webTestClient;

    @InjectMocks
    private VisitController visitController;

    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @MockBean
    private BFFApiGatewayController bffApiGatewayController;

    private final String BASE_VISIT_URL = "/api/v2/gateway/visits";
    private final String REVIEWS_URL = BASE_VISIT_URL + "/reviews";

    private final String EMERGENCY_URL = BASE_VISIT_URL + "/emergency";

    //VisitResponseDTO Objects for testing purposes
    private final VisitResponseDTO visitResponseDTO1 = VisitResponseDTO.builder()
            .visitId("V001")
            .visitDate(LocalDate.of(2021, 5, 1).atStartOfDay())
            .description("Routine check-up")
            .petId("P001")
            .petName("Buddy")
            .petBirthDate(Date.from(LocalDate.of(2020, 5, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
            .practitionerId("PR001")
            .vetFirstName("John")
            .vetLastName("Doe")
            .vetEmail("john.doe@example.com")
            .vetPhoneNumber("555-1234")
            .status(Status.COMPLETED)
            .build();


    // ReviewResponseDTO and ReviewRequestDTO for testing purposes
    private final ReviewResponseDTO reviewResponseDTO = ReviewResponseDTO.builder()
            .reviewId("R001")
            .review("Excellent service")
            .reviewerName("John Doe")
            .rating(5)
            .dateSubmitted(LocalDateTime.now())
            .build();

    private final ReviewRequestDTO reviewRequestDTO = ReviewRequestDTO.builder()
            .review("Good service")
            .reviewerName("Jane Doe")
            .rating(4)
            .dateSubmitted(LocalDateTime.now())
            .build();


    EmergencyRequestDTO emergencyRequestDTO = EmergencyRequestDTO.builder()
            .visitDate(LocalDateTime.now())
            .description("Updated Emergency")
            .petId("Oscar")
            .practitionerId("2332222232323234hhh232")
            .urgencyLevel(UrgencyLevel.MEDIUM)
            .emergencyType("Accident")
            .build();

     EmergencyResponseDTO emergencyResponseDTO = EmergencyResponseDTO.builder()
            .visitEmergencyId(UUID.randomUUID().toString())
            .visitDate(emergencyRequestDTO.getVisitDate())
            .description(emergencyRequestDTO.getDescription())
            .petId(emergencyRequestDTO.getPetId())
             .petName("hamid")
             .petBirthDate(new Date())
             .practitionerId(emergencyRequestDTO.getPractitionerId())
             .vetFirstName("carlos")
             .vetLastName("ambock")
             .vetEmail("carlos@gmail.com")
             .vetPhoneNumber("540-233-2323")
            .urgencyLevel(emergencyRequestDTO.getUrgencyLevel())
            .emergencyType(emergencyRequestDTO.getEmergencyType())
            .build();

    @Test
    void getAllReviews_whenReviewsExist_thenReturnFluxReviewResponseDTO() {
        // Arrange
        when(visitsServiceClient.getAllReviews())
                .thenReturn(Flux.just(reviewResponseDTO));

        // Act
        webTestClient.get()
                .uri(REVIEWS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewResponseDTO.class)
                .hasSize(1);

        // Assert
        verify(visitsServiceClient, times(1)).getAllReviews();
    }

    @Test
    void getAllReviews_whenNoReviewsExist_thenReturnEmptyFlux() {
        // Arrange
        when(visitsServiceClient.getAllReviews())
                .thenReturn(Flux.empty());

        // Act
        webTestClient.get()
                .uri(REVIEWS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewResponseDTO.class)
                .hasSize(0);

        // Assert
        verify(visitsServiceClient, times(1)).getAllReviews();
    }

    @Test
    void getAllVisits_whenAllPropertiesExist_thenReturnFluxResponseDTO() {
        // Arrange
        String description = "test"; // Add a description here
        when(visitsServiceClient.getAllVisits(description))
                .thenReturn(Flux.just(visitResponseDTO1));

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(BASE_VISIT_URL)
                        .queryParam("description", description)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .hasSize(1);

        // Assert
        verify(visitsServiceClient, times(1)).getAllVisits(description);
    }

    @Test
    void whenAddVisit_asAdmin_thenReturnCreatedVisitDTO() {
        // Arrange
        VisitRequestDTO newVisitRequestDTO = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.of(2023, 10, 10, 10, 0))
                .description("Routine check-up")
                .petId("P001")
                .practitionerId("PR001")
                .status(Status.UPCOMING)
                .build();

        VisitResponseDTO createdVisitResponseDTO = VisitResponseDTO.builder()
                .visitId("V001")
                .visitDate(newVisitRequestDTO.getVisitDate())
                .description(newVisitRequestDTO.getDescription())
                .petId(newVisitRequestDTO.getPetId())
                .practitionerId(newVisitRequestDTO.getPractitionerId())
                .status(newVisitRequestDTO.getStatus())
                .build();

        // Mock the visitsServiceClient to return the expected Mono
        when(visitsServiceClient.addVisit(any(Mono.class)))
                .thenReturn(Mono.just(createdVisitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = webTestClient.post()
                .uri(BASE_VISIT_URL)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newVisitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(VisitResponseDTO.class)
                .getResponseBody()
                .single();

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(visitResponseDTO -> {
                    assertNotNull(visitResponseDTO);
                    assertNotNull(visitResponseDTO.getVisitId());
                    assertEquals(newVisitRequestDTO.getVisitDate(), visitResponseDTO.getVisitDate());
                    assertEquals(newVisitRequestDTO.getDescription(), visitResponseDTO.getDescription());
                    assertEquals(newVisitRequestDTO.getPetId(), visitResponseDTO.getPetId());
                    assertEquals(newVisitRequestDTO.getPractitionerId(), visitResponseDTO.getPractitionerId());
                    assertEquals(newVisitRequestDTO.getStatus(), visitResponseDTO.getStatus());
                    return true;
                })
                .verifyComplete();

        // Verify that addVisit was called
        verify(visitsServiceClient, times(1)).addVisit(any(Mono.class));
    }

    @Test
    void postReview_whenValidRequest_thenReturnCreatedResponse() {
        // Arrange
        when(visitsServiceClient.createReview(any(Mono.class)))
                .thenReturn(Mono.just(reviewResponseDTO));

        // Act
        webTestClient.post()
                .uri(REVIEWS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reviewRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).createReview(any(Mono.class));
    }

    @Test
    void updateReview_whenValidRequest_thenReturnOkResponse() {
        // Arrange
        when(visitsServiceClient.updateReview(eq("R001"), any(Mono.class)))
                .thenReturn(Mono.just(reviewResponseDTO));

        // Act
        webTestClient.put()
                .uri(REVIEWS_URL + "/{reviewId}", "R001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reviewRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).updateReview(eq("R001"), any(Mono.class));
    }


    @Test
    void getReviewByReviewId_whenValidReviewId_thenReturnReviewResponseDTO() {
        // Arrange
        when(visitsServiceClient.getReviewByReviewId("R001"))
                .thenReturn(Mono.just(reviewResponseDTO));

        // Act
        webTestClient.get()
                .uri(REVIEWS_URL + "/{reviewId}", "R001")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).getReviewByReviewId("R001");
    }


    @Test
    void getReviewByReviewId_whenReviewNotFound_thenReturnNotFound() {
        // Arrange
        when(visitsServiceClient.getReviewByReviewId("R999"))
                .thenReturn(Mono.empty());

        // Act
        webTestClient.get()
                .uri(REVIEWS_URL + "/{reviewId}", "R999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        // Assert
        verify(visitsServiceClient, times(1)).getReviewByReviewId("R999");
    }

    @Test
    void getAllVisits_whenNoVisitsExist_thenReturnEmptyFlux(){
        String description = null;
        // Arrange
        when(visitsServiceClient.getAllVisits(description))
                .thenReturn(Flux.empty()); // no visits should not throw an error
        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .hasSize(0);
        // Assert
        verify(visitsServiceClient, times(1)).getAllVisits(description);
    }

    @Test
    public void updateVisit_ShouldReturnUpdatedVisit() {
        String visitId = "visitId4";

        VisitRequestDTO newVisitRequestDTO = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.of(2023, 10, 10, 10, 0))
                .description("Routine check-up")
                .petId("0e4d8481-b611-4e52-baed-af16caa8bf8a")
                .practitionerId("69f85d2e-625b-11ee-8c99-0242ac120002")
                .status(Status.UPCOMING)
                .build();

        VisitResponseDTO updatedVisitResponseDTO = VisitResponseDTO.builder()
                .visitId(visitId)
                .visitDate(newVisitRequestDTO.getVisitDate())
                .description(newVisitRequestDTO.getDescription())
                .petId(newVisitRequestDTO.getPetId())
                .practitionerId(newVisitRequestDTO.getPractitionerId())
                .status(newVisitRequestDTO.getStatus())
                .build();

        // Mock the service layer to return the updated response
        when(visitsServiceClient.updateVisitByVisitId(eq(visitId), any(Mono.class)))
                .thenReturn(Mono.just(updatedVisitResponseDTO));

        // Act
        webTestClient.put()
                .uri(BASE_VISIT_URL + "/{visitId}", visitId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newVisitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(VisitResponseDTO.class)
                .getResponseBody()
                .single()
                .as(StepVerifier::create)
                .expectNextMatches(visitResponseDTO -> {
                    assertNotNull(visitResponseDTO);
                    assertNotNull(visitResponseDTO.getVisitId());
                    assertEquals(newVisitRequestDTO.getVisitDate(), visitResponseDTO.getVisitDate());
                    assertEquals(newVisitRequestDTO.getDescription(), visitResponseDTO.getDescription());
                    assertEquals(newVisitRequestDTO.getPetId(), visitResponseDTO.getPetId());
                    assertEquals(newVisitRequestDTO.getPractitionerId(), visitResponseDTO.getPractitionerId());
                    assertEquals(newVisitRequestDTO.getStatus(), visitResponseDTO.getStatus());
                    return true;
                })
                .verifyComplete();

        // Verify that updateVisitByVisitId was called
        verify(visitsServiceClient, times(1)).updateVisitByVisitId(eq(visitId), any(Mono.class));
    }


    @Test
    public void updateVisitByVisitId_ShouldReturnNotFound_WhenVisitDoesNotExist() {
        String nonExistentVisitId = "invalidVisitId";

        VisitRequestDTO visitUpdateRequest = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2022-01-15T10:30"))
                .description("Updated description for a non-existent visit.")
                .petId("1")
                .practitionerId("2")
                .status(Status.COMPLETED)
                .build();

        // Mock the service to return Mono.empty(), simulating a non-existent visit
        when(visitsServiceClient.updateVisitByVisitId(eq(nonExistentVisitId), any(Mono.class)))
                .thenReturn(Mono.empty()); // Simulate "not found" scenario

        webTestClient.put()
                .uri(BASE_VISIT_URL + "/{visitId}", nonExistentVisitId)
                .cookie("Bearer", "your-auth-token") // Assuming "Bearer" is the name of the cookie
                .body(Mono.just(visitUpdateRequest), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Validate the response
                .expectStatus().isNotFound();
    }

    @Test
    void getVisitsByOwnerId_whenOwnerExists_thenReturnFluxVisitResponseDTO() {
        // Arrange
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
        when(bffApiGatewayController.getVisitsByOwnerId(ownerId))
                .thenReturn(Flux.just(visitResponseDTO1));


        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}", ownerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .hasSize(1);

        // Assert
        verify(bffApiGatewayController, times(1)).getVisitsByOwnerId(ownerId);
    }

    //Emergency

    @Test
    void getAllEmergency_whenEmergencyExist_thenReturnFluxEmergencyResponseDTO() {
        // Arrange
        when(visitsServiceClient.getAllEmergency())
                .thenReturn(Flux.just(emergencyResponseDTO));

        // Act
        webTestClient.get()
                .uri(EMERGENCY_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmergencyResponseDTO.class)
                .hasSize(1);

        // Assert
        verify(visitsServiceClient, times(1)).getAllEmergency();
    }

    @Test
    void getAllEmergencies_whenNoEmergenciesExist_thenReturnEmptyFlux() {
        // Arrange
        when(visitsServiceClient.getAllEmergency())
                .thenReturn(Flux.empty());

        // Act
        webTestClient.get()
                .uri(EMERGENCY_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmergencyResponseDTO.class)
                .hasSize(0);

        // Assert
        verify(visitsServiceClient, times(1)).getAllEmergency();
    }

    @Test
    void postEmergency_whenValidRequest_thenReturnCreatedResponse() {
        // Arrange
        when(visitsServiceClient.createEmergency(any(Mono.class)))
                .thenReturn(Mono.just(emergencyResponseDTO));

        // Act
        webTestClient.post()
                .uri(EMERGENCY_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emergencyRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmergencyResponseDTO.class)
                .isEqualTo(emergencyResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).createEmergency(any(Mono.class));
    }

    @Test
    void getEmergencyVisitsByOwnerId_whenOwnerExists_thenReturnFluxVisitResponseDTO() {
        // Arrange
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
        when(bffApiGatewayController.getEmergencyVisitsByOwnerId(ownerId))
                .thenReturn(Flux.just(emergencyResponseDTO));


        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL + "/emergency/owners/{ownerId}", ownerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmergencyResponseDTO.class)
                .hasSize(1);

        // Assert
        verify(bffApiGatewayController, times(1)).getEmergencyVisitsByOwnerId(ownerId);
    }

    @Test
    void getEmergencyVisitsByOwnerId_whenOwnerDoesNotExist_thenReturnEmptyFlux() {
        // Arrange
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f03610";
        when(bffApiGatewayController.getEmergencyVisitsByOwnerId(ownerId))
                .thenReturn(Flux.empty());

        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL + "/emergency/owners/{ownerId}", ownerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmergencyResponseDTO.class)
                .hasSize(0);

        // Assert
        verify(bffApiGatewayController, times(1)).getEmergencyVisitsByOwnerId(ownerId);
    }

    @Test
    void getEmergencyByEmergencyId_whenValidEmergencyId_thenReturnEmergencyResponseDTO() {
        // Arrange
        when(visitsServiceClient.getEmergencyByEmergencyId(emergencyResponseDTO.getVisitEmergencyId()))
                .thenReturn(Mono.just(emergencyResponseDTO));

        // Act
        webTestClient.get()
                .uri(EMERGENCY_URL + "/{emergencyId}", emergencyResponseDTO.getVisitEmergencyId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmergencyResponseDTO.class)
                .isEqualTo(emergencyResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).getEmergencyByEmergencyId(emergencyResponseDTO.getVisitEmergencyId());
    }

  /*  @Test
    void postEmergency_whenValidRequest_thenReturnCreatedResponse() {
        // Arrange
        when(visitsServiceClient.createEmergency(any(Mono.class)))
                .thenReturn(Mono.just(emergencyResponseDTO));

        // Act
        webTestClient.post()
                .uri(EMERGENCY_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emergencyRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmergencyResponseDTO.class)
                .isEqualTo(emergencyResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).createEmergency(any(Mono.class));
    }

    @Test
    void updateEmergency_whenValidRequest_thenReturnOkResponse() {
        // Arrange
        when(visitsServiceClient.updateEmergency(eq(emergencyResponseDTO.getVisitEmergencyId()), any(Mono.class)))
                .thenReturn(Mono.just(emergencyResponseDTO));

        // Act
        webTestClient.put()
                .uri(EMERGENCY_URL + "/{emergencyId}", emergencyResponseDTO.getVisitEmergencyId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emergencyRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmergencyResponseDTO.class)
                .isEqualTo(emergencyResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).updateEmergency(eq(emergencyResponseDTO.getVisitEmergencyId()), any(Mono.class));
    }

    @Test
    void getEmergencyByEmergencyId_whenValidEmergencyId_thenReturnEmergencyResponseDTO() {
        // Arrange
        when(visitsServiceClient.getEmergencyByEmergencyId(emergencyResponseDTO.getVisitEmergencyId()))
                .thenReturn(Mono.just(emergencyResponseDTO));

        // Act
        webTestClient.get()
                .uri(EMERGENCY_URL + "/{emergencyId}", emergencyResponseDTO.getVisitEmergencyId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmergencyResponseDTO.class)
                .isEqualTo(emergencyResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).getEmergencyByEmergencyId(emergencyResponseDTO.getVisitEmergencyId());
    }

    @Test
    void getEmergencyByEmergencyId_whenBadRequest_thenReturnBadRequest() {
        // Arrange
        when(visitsServiceClient.getEmergencyByEmergencyId("invalidEmergencyId"))
                .thenReturn(Mono.empty());

        // Act
        webTestClient.get()
                .uri(EMERGENCY_URL + "/{emergencyId}", "invalidEmergencyId")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        // Assert
        verify(visitsServiceClient, times(1)).getEmergencyByEmergencyId("invalidEmergencyId");
    }


    @Test
    public void whenDeleteEmergencyById_returnEmergencyResponseDTO() {
        // Arrange
        when(visitsServiceClient.deleteEmergency(emergencyResponseDTO.getVisitEmergencyId()))
                .thenReturn(Mono.just(emergencyResponseDTO));

        // Act
        webTestClient.delete()
                .uri(EMERGENCY_URL + "/{emergencyId}", emergencyResponseDTO.getVisitEmergencyId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmergencyResponseDTO.class)
                .isEqualTo(emergencyResponseDTO);

        // Assert
        // Verify the deleteEmergency method is invoked, not getEmergencyByEmergencyId
        verify(visitsServiceClient, times(1)).deleteEmergency(emergencyResponseDTO.getVisitEmergencyId());
    }

   */

    @Test
    void getVisitsByOwnerId_whenOwnerDoesNotExist_thenReturnEmptyFlux() {
        // Arrange
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f03610";
        when(bffApiGatewayController.getVisitsByOwnerId(ownerId))
                .thenReturn(Flux.empty());

        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}", ownerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .hasSize(0);

        // Assert
        verify(bffApiGatewayController, times(1)).getVisitsByOwnerId(ownerId);
    }

    @Test
    void updateVisitStatus_ShouldReturnOK_WhenStatusUpdatedToCancelled() {
        String visitId = "12345";
        String status = "CANCELLED";

        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId(visitId)
                .status(Status.CANCELLED)
                .description("Test visit with cancelled status")
                .build();

        // Mocking the service layer to return the expected response
        when(visitsServiceClient.patchVisitStatus(eq(visitId), eq(status)))
                .thenReturn(Mono.just(visitResponseDTO));

        webTestClient.patch()
                .uri(BASE_VISIT_URL + "/{visitId}/{status}", visitId, status)
                .exchange()
                .expectStatus().isOk() // Expect 200 OK
                .expectBody(VisitResponseDTO.class)
                .value(response -> {
                    assertEquals(response.getVisitId(), visitId);
                    assertEquals(response.getStatus(), Status.CANCELLED);
                });

        // Verify that the service was called with the correct parameters
        verify(visitsServiceClient, times(1)).patchVisitStatus(eq(visitId), eq(status));
    }

    @Test
    void updateVisitStatus_ShouldReturnNotFound_WhenVisitDoesNotExist() {
        String visitId = "nonExistentVisitId";
        String status = "CANCELLED";

        // Mocking the service to return an empty Mono, simulating a not found scenario
        when(visitsServiceClient.patchVisitStatus(eq(visitId), eq(status)))
                .thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(BASE_VISIT_URL + "/{visitId}/{status}", visitId, status)
                .exchange()
                .expectStatus().isNotFound(); // Expect 404 NOT_FOUND

        // Verify that the service was called
        verify(visitsServiceClient, times(1)).patchVisitStatus(eq(visitId), eq(status));
    }

    @Test
    void archiveCompletedVisit_whenValidRequest_thenReturnVisitResponseDTO() {
        String visitId = "visitId1";
        VisitRequestDTO visitRequestDTO = VisitRequestDTO.builder()
                .description("Updated Visit Description")
                .status(Status.COMPLETED)
                .build();
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId(visitId)
                .description("Updated Visit Description")
                .status(Status.COMPLETED)
                .build();

        when(visitsServiceClient.archiveCompletedVisit(eq(visitId), any(Mono.class)))
                .thenReturn(Mono.just(visitResponseDTO));

        webTestClient.put()
                .uri(BASE_VISIT_URL + "/completed/{visitId}/archive", visitId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(visitRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VisitResponseDTO.class)
                .isEqualTo(visitResponseDTO);

        verify(visitsServiceClient, times(1)).archiveCompletedVisit(eq(visitId), any(Mono.class));
    }

    @Test
    void archiveCompletedVisit_whenInvalidRequest_thenReturnBadRequest() {
        String visitId = "visitId1";
        VisitRequestDTO visitRequestDTO = VisitRequestDTO.builder()
                .description("Updated Visit Description")
                .status(Status.COMPLETED)
                .build();

        when(visitsServiceClient.archiveCompletedVisit(eq(visitId), any(Mono.class)))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri(BASE_VISIT_URL + "/completed/{visitId}/archive", visitId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(visitRequestDTO)
                .exchange()
                .expectStatus().isBadRequest();

        verify(visitsServiceClient, times(1)).archiveCompletedVisit(eq(visitId), any(Mono.class));
    }

    @Test
    void getArchivedVisits_whenArchivedVisitsExist_thenReturnFluxVisitResponseDTO() {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("visitId1")
                .description("Archived Visit")
                .status(Status.ARCHIVED)
                .build();

        when(visitsServiceClient.getAllArchivedVisits())
                .thenReturn(Flux.just(visitResponseDTO));

        webTestClient.get()
                .uri(BASE_VISIT_URL + "/archived")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .hasSize(1)
                .contains(visitResponseDTO);

        verify(visitsServiceClient, times(1)).getAllArchivedVisits();
    }

    @Test
    void getArchivedVisits_whenNoArchivedVisitsExist_thenReturnEmptyFlux() {
        when(visitsServiceClient.getAllArchivedVisits())
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_VISIT_URL + "/archived")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .hasSize(0);

        verify(visitsServiceClient, times(1)).getAllArchivedVisits();
    }
  
  void deleteReview_whenValidReviewId_thenReturnOkResponse() {
        // Arrange
        when(visitsServiceClient.deleteReview(eq("R001")))
                .thenReturn(Mono.just(reviewResponseDTO)); // Mocking the service client to return a valid review response

        // Act
        webTestClient.delete()
                .uri(REVIEWS_URL + "/{reviewId}", "R001")  // Set the valid reviewId
                .exchange()
                .expectStatus().isOk()  // Expect 200 OK response
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO);  // Validate the response body

        // Assert
        verify(visitsServiceClient, times(1)).deleteReview(eq("R001"));  // Ensure the service method was called once
    }

    @Test
    void deleteReview_whenInvalidReviewId_thenReturnNotFoundResponse() {
        // Arrange
        when(visitsServiceClient.deleteReview(eq("INVALID_ID")))
                .thenReturn(Mono.empty());  // Mocking the service client to return empty Mono for a nonexistent review

        // Act
        webTestClient.delete()
                .uri(REVIEWS_URL + "/{reviewId}", "INVALID_ID")  // Set an invalid reviewId
                .exchange()
                .expectStatus().isNotFound();  // Expect 404 Not Found response

        // Assert
        verify(visitsServiceClient, times(1)).deleteReview(eq("INVALID_ID"));  // Ensure the service method was called once
    }

    @Test
    void getReviewsByOwnerId_whenOwnerExists_thenReturnFluxReviewResponseDTO() {
        // Arrange
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
        when(visitsServiceClient.getReviewsByOwnerId(ownerId))
                .thenReturn(Flux.just(reviewResponseDTO));

        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}/reviews", ownerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewResponseDTO.class)
                .hasSize(1)
                .contains(reviewResponseDTO);

        // Assert
        verify(visitsServiceClient, times(1)).getReviewsByOwnerId(ownerId);
    }

    @Test
    void getReviewsByOwnerId_whenOwnerDoesNotExist_thenReturnEmptyFlux() {
        // Arrange
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361e";
        when(visitsServiceClient.getReviewsByOwnerId(ownerId))
                .thenReturn(Flux.empty());

        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}/reviews", ownerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewResponseDTO.class)
                .hasSize(0);

        // Assert
        verify(visitsServiceClient, times(1)).getReviewsByOwnerId(ownerId);
    }

    @Test
    void addReviewCustomer_whenValidRequest_thenReturnCreatedResponse() {
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
        ReviewRequestDTO reviewRequestDTO = ReviewRequestDTO.builder()
                .review("Great service")
                .reviewerName("Jane Doe")
                .rating(5)
                .dateSubmitted(LocalDateTime.now())
                .build();
        ReviewResponseDTO reviewResponseDTO = ReviewResponseDTO.builder()
                .reviewId("R001")
                .review("Great service")
                .reviewerName("Jane Doe")
                .rating(5)
                .dateSubmitted(LocalDateTime.now())
                .build();

        when(visitsServiceClient.addCustomerReview(eq(ownerId), any(ReviewRequestDTO.class)))
                .thenReturn(Mono.just(reviewResponseDTO));

        webTestClient.post()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}/reviews", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reviewRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ReviewResponseDTO.class)
                .isEqualTo(reviewResponseDTO);

        verify(visitsServiceClient, times(1)).addCustomerReview(eq(ownerId), any(ReviewRequestDTO.class));
    }

    @Test
    void addReviewCustomer_whenInvalidRequest_thenReturnBadRequest() {
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
        ReviewRequestDTO reviewRequestDTO = ReviewRequestDTO.builder()
                .review("")
                .reviewerName("")
                .rating(0)
                .dateSubmitted(LocalDateTime.now())
                .build();

        when(visitsServiceClient.addCustomerReview(eq(ownerId), any(ReviewRequestDTO.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}/reviews", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reviewRequestDTO)
                .exchange()
                .expectStatus().isBadRequest();

        verify(visitsServiceClient, times(1)).addCustomerReview(eq(ownerId), any(ReviewRequestDTO.class));
    }

    @Test
    void deleteCustomerReview_whenValidOwnerIdAndReviewId_thenReturnOkResponse() {
        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
        String reviewId = UUID.randomUUID().toString();

        when(visitsServiceClient.deleteReview(ownerId, reviewId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}/reviews/{reviewId}", ownerId, reviewId)
                .exchange()
                .expectStatus().isNoContent();

        verify(visitsServiceClient, times(1)).deleteReview(ownerId, reviewId);
    }

    @Test
    void deleteCustomerReview_whenInvalidOwnerId_thenReturnNoContent() {
        String ownerId = "invalidOwnerId";
        String reviewId = UUID.randomUUID().toString();

        when(visitsServiceClient.deleteReview(ownerId, reviewId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}/reviews/{reviewId}", ownerId, reviewId)
                .exchange()
                .expectStatus().isNoContent();

        verify(visitsServiceClient, times(1)).deleteReview(ownerId, reviewId);
    }

    @Test
    void deleteCustomerReview_whenInvalidReviewId_thenReturnNoContent() {
        String ownerId = "validOwnerId";
        String reviewId = UUID.randomUUID().toString();

        when(visitsServiceClient.deleteReview(ownerId, reviewId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_VISIT_URL + "/owners/{ownerId}/reviews/{reviewId}", ownerId, reviewId)
                .exchange()
                .expectStatus().isNoContent();

        verify(visitsServiceClient, times(1)).deleteReview(ownerId, reviewId);
    }
    @Test
    void exportVisitsToCSV_ShouldReturnCSVFile() {
        // Sample data to return
        String csvContent = "VisitId,Description\n1,Checkup";
        InputStreamResource csvData = new InputStreamResource(new ByteArrayInputStream(csvContent.getBytes()));
        when(visitsServiceClient.exportVisitsToCSV()).thenReturn(Mono.just(csvData));

        webTestClient.get()
                .uri("/api/v2/gateway/visits/export")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=visits.csv")
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(String.class)
                .consumeWith(response -> {
                    // Check the response content
                    Assertions.assertEquals(csvContent, new String(response.getResponseBody()));
                });
    }


    @Test
    void exportVisitsToCSV_ShouldReturnServerError_WhenServiceFails() {
        when(visitsServiceClient.exportVisitsToCSV()).thenReturn(Mono.error(new RuntimeException("Service failed")));

        webTestClient.get()
                .uri("/api/v2/gateway/visits/export")
                .exchange()
                .expectStatus().is5xxServerError();
    }


}
