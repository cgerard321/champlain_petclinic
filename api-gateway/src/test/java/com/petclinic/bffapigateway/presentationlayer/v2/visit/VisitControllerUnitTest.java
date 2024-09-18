package com.petclinic.bffapigateway.presentationlayer.v2.visit;

import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Visits.Status;
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







}
