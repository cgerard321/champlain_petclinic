package com.petclinic.bffapigateway.presentationlayer.v2.visit;

import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Visits.Status;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.VisitController;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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

    private final String BASE_VISIT_URL = "/api/v2/gateway/visits";
    private final String REVIEWS_URL = BASE_VISIT_URL + "/reviews";

    //VisitResponseDTO Objects for testing purposes
    private final VisitResponseDTO visitResponseDTO1 = VisitResponseDTO.builder()
            .visitId("V001")
            .visitStartDate(LocalDate.of(2021, 5, 1).atStartOfDay())
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
            .visitEndDate(LocalDate.of(2021, 5, 1).atStartOfDay())
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
        when(visitsServiceClient.getAllVisits())
                .thenReturn(Flux.just(visitResponseDTO1));

        // Act
        webTestClient.get()
                .uri(BASE_VISIT_URL)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList( VisitResponseDTO.class)
                .hasSize(1);
        // Assert
        verify(visitsServiceClient, times(1)).getAllVisits();
    }

    @Test
    void whenAddVisit_asAdmin_thenReturnCreatedVisitDTO() {
        // Arrange
        VisitRequestDTO newVisitRequestDTO = VisitRequestDTO.builder()
                .visitStartDate(LocalDateTime.of(2023, 10, 10, 10, 0))
                .description("Routine check-up")
                .petId("P001")
                .practitionerId("PR001")
                .status(Status.UPCOMING)
                .build();

        VisitResponseDTO createdVisitResponseDTO = VisitResponseDTO.builder()
                .visitId("V001")
                .visitStartDate(newVisitRequestDTO.getVisitStartDate())
                .description(newVisitRequestDTO.getDescription())
                .petId(newVisitRequestDTO.getPetId())
                .practitionerId(newVisitRequestDTO.getPractitionerId())
                .status(newVisitRequestDTO.getStatus())
                .visitEndDate(newVisitRequestDTO.getVisitStartDate())
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
                    assertEquals(newVisitRequestDTO.getVisitStartDate(), visitResponseDTO.getVisitStartDate());
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
        // Arrange
        when(visitsServiceClient.getAllVisits())
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
        verify(visitsServiceClient, times(1)).getAllVisits();
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





}
