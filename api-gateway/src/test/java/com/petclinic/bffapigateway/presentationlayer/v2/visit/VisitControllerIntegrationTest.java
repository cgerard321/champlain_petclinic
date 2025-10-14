package com.petclinic.bffapigateway.presentationlayer.v2.visit;

import com.petclinic.bffapigateway.dtos.Visits.Status;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigVisitService;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Date;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

// Disables MongoDB for testing as mock servers handle all data interactions.
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=0"})
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

public class VisitControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    private MockServerConfigVisitService mockServerConfigVisitService;
    private MockServerConfigAuthService mockServerConfigAuthService;


    private final VisitResponseDTO visitResponseDTO1 = VisitResponseDTO.builder()
            .visitDate(LocalDateTime.of(2023, 10, 1, 10, 0))
            .description("Regular check-up")
            .petId("pet123")
            .petName("Buddy")
            .petBirthDate(new Date(120, 5, 15)) // June 15, 2020
            .practitionerId("practitioner456")
            .vetFirstName("John")
            .vetLastName("Doe")
            .vetEmail("john.doe@example.com")
            .vetPhoneNumber("123-456-7890")
            .status(Status.UPCOMING)
            .visitId("visit789")
            .visitEndDate(LocalDateTime.of(2023, 10, 1, 11, 0))
            .build();
    private final VisitResponseDTO visitResponseDTO2 = VisitResponseDTO.builder()
            .visitDate(LocalDateTime.of(2023, 10, 2, 14, 0))
            .description("Vaccination")
            .petId("pet456")
            .petName("Max")
            .petBirthDate(new Date(119, 3, 20)) // April 20, 2019
            .practitionerId("practitioner789")
            .vetFirstName("Jane")
            .vetLastName("Smith")
            .vetEmail("jane.smith@example.com")
            .vetPhoneNumber("987-654-3210")
            .status(Status.COMPLETED)
            .visitId("visit101112")
            .visitEndDate(LocalDateTime.of(2023, 10, 2, 15, 0))
            .build();


    @BeforeAll
    void startService() {
        mockServerConfigVisitService = new MockServerConfigVisitService();
        mockServerConfigVisitService.registerGetAllVisitsEndpoint();
        mockServerConfigVisitService.registerDeleteCompletedVisitsByIdEndpoint();
        mockServerConfigVisitService.registerDeleteCompletedVisit_ByInvalidIdEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
    }

    @AfterAll
    void stopService() {
        mockServerConfigVisitService.stopServer();
        mockServerConfigAuthService.stopMockServer();

    }

    //    @BeforeEach
//    void setUp(){
//        mockServerConfigVisitService.registerGetAllVisitsEndpoint();
//        mockServerConfigVisitService.registerDeleteCompletedVisitsByIdEndpoint();
//        mockServerConfigVisitService.registerDeleteCompletedVisit_ByInvalidIdEndpoint();
//        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
//
//    }
    @Test
    void whenGetAllVisits_asAdmin_thenReturnAllVisits() {
        Flux<VisitResponseDTO> result = webTestClient.get()
                .uri("/api/gateway/visits")
                .cookie("Bearer", jwtTokenForValidAdmin) // admin token
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .returnResult(VisitResponseDTO.class)
                .getResponseBody();

        StepVerifier
                .create(result)
                .expectNextCount(3)
                .verifyComplete();
    }
}
