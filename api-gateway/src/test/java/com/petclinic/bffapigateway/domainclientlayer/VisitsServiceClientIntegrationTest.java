package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.*;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.EmergencyResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.Emergency.UrgencyLevel;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.reviews.ReviewResponseDTO;
import com.petclinic.bffapigateway.exceptions.BadRequestException;
import com.petclinic.bffapigateway.exceptions.DuplicateTimeException;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(value = VisitsServiceClient.class, excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
        classes = {JwtTokenFilter.class, RoleFilter.class}), useDefaultFilters = false)
@AutoConfigureWebTestClient
class VisitsServiceClientIntegrationTest {
    @MockBean
    private VisitsServiceClient visitsServiceClient;
    private static MockWebServer server;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String VISIT_ID = "visitId4";

    private static final String PET_ID = "1";

    private static final String STATUS = "UPCOMING";

    String reviewUrl = "http://localhost:8080/reviews";

    @BeforeAll
    static void beforeAllSetUp() throws IOException{
        server = new MockWebServer();
        server.start();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    @BeforeEach
    void setUp() {
        visitsServiceClient = new VisitsServiceClient("localhost", "" + server.getPort());
    }
    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void getAllVisits() throws JsonProcessingException {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
        VisitResponseDTO visitResponseDTO2 = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(Arrays.asList(visitResponseDTO, visitResponseDTO2))).addHeader("Content-Type", "application/json"));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitsServiceClient.getAllVisits(visitResponseDTO.getDescription());
        StepVerifier.create(visitResponseDTOFlux)
                .expectNext(visitResponseDTO)
                .expectNext(visitResponseDTO2)
                .verifyComplete();
    }
    @Test
    void getAllVisits_400Error()throws IllegalArgumentException{
        String description = "test"; // Add a description here
        server.enqueue(new MockResponse().setResponseCode(400).addHeader("Content-Type", "application/json"));
        Flux<VisitResponseDTO> visitResponseDTOFlux = visitsServiceClient.getAllVisits(description); // Pass the description to the method
        StepVerifier.create(visitResponseDTOFlux)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && Objects.equals(throwable.getMessage(), "Something went wrong and we got a 400 error"))
                .verify();
    }
    @Test
    void getAllVisits_500Error()throws IllegalArgumentException{
        String description = "test"; // Add a description here
        server.enqueue(new MockResponse().setResponseCode(500).addHeader("Content-Type", "application/json"));
        Flux<VisitResponseDTO> visitResponseDTOFlux = visitsServiceClient.getAllVisits(description);
        StepVerifier.create(visitResponseDTOFlux)
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && Objects.equals(throwable.getMessage(), "Something went wrong and we got a 500 error"))
            .verify();
    }

    @Test
    void getVisitsForStatus() throws JsonProcessingException{
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .build();
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(visitResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitsServiceClient.getVisitsForStatus(STATUS);
        StepVerifier.create(visitResponseDTOFlux)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }

    @Test
    void getVisitByPractitionerId() throws JsonProcessingException {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .build();
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(visitResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitsServiceClient.getVisitByPractitionerId("2");
        StepVerifier.create(visitResponseDTOFlux)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }

    @Test
    void createVisitForPet_Valid() throws JsonProcessingException {
        // Arrange
        VisitRequestDTO visitRequestDTO = new VisitRequestDTO(
                LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Test Visit",
                "1",
                "f470653d-05c5-4c45-b7a0-7d70f003d2ac",
                "testJwtToken",
                "2",
                "73b5c112-5703-4fb7-b7bc-ac8186811ae1"
        );

        // Mock the server response
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(visitResponseDTO))
        );

        // Act
        Mono<VisitResponseDTO> resultMono = visitsServiceClient.createVisitForPet(visitRequestDTO);

        // Assert
        StepVerifier.create(resultMono)
                .expectNext()
                .expectNext()
                .expectNextMatches(visitResponse -> Objects.equals(visitResponse.getVisitId(), visitResponseDTO.getVisitId()))
                .verifyComplete();

    }

    //DuplicateTime Exception Test
    @Test
    void createVisitForPet_DuplicateTime_ThrowsDuplicateTimeException() throws JsonProcessingException {
        // Arrange
        VisitRequestDTO visitRequestDTO = new VisitRequestDTO(
                LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Test Visit",
                "1",
                "f470653d-05c5-4c45-b7a0-7d70f003d2ac",
                "testJwtToken",
                "2",
                "73b5c112-5703-4fb7-b7bc-ac8186811ae1"
        );

        String errorMessage = "{\"message\":\"A visit with the same time already exists.\"}";
        // Mock the server response for duplicate time
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.CONFLICT.value()) // 409 status
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(errorMessage));

        // Act
        Mono<VisitResponseDTO> resultMono = visitsServiceClient.createVisitForPet(visitRequestDTO);

        // Assert
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof DuplicateTimeException
                        && throwable.getMessage().contains("A visit with the same time already exists."))
                .verify();
    }

    @Test
    void createVisitForPet_NotFound_ThrowsNotFoundException() throws JsonProcessingException {
        // Arrange
        VisitRequestDTO visitRequestDTO = new VisitRequestDTO(
                LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Test Visit",
                "1",
                "f470653d-05c5-4c45-b7a0-7d70f003d2ac",
                "testJwtToken",
                "2",
                "73b5c112-5703-4fb7-b7bc-ac8186811ae1"
        );

        String errorMessage = "{\"message\":\"Visit not found.\"}";
        // Mock the server response for not found
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()) // 404 status
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(errorMessage));

        // Act
        Mono<VisitResponseDTO> resultMono = visitsServiceClient.createVisitForPet(visitRequestDTO);

        // Assert
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("Visit not found."))
                .verify();
    }

    @Test
    void createVisitForPet_BadRequest_ThrowsBadRequestException() throws JsonProcessingException {
        // Arrange
        VisitRequestDTO visitRequestDTO = new VisitRequestDTO(
                LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Test Visit",
                "1",
                "f470653d-05c5-4c45-b7a0-7d70f003d2ac",
                "testJwtToken",
                "2",
                "73b5c112-5703-4fb7-b7bc-ac8186811ae1"
        );

        String errorMessage = "{\"message\":\"Invalid request.\"}";
        // Mock the server response for bad request
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value()) // 400 status
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(errorMessage));

        // Act
        Mono<VisitResponseDTO> resultMono = visitsServiceClient.createVisitForPet(visitRequestDTO);

        // Assert
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().contains("Invalid request."))
                .verify();
    }

    @Test
    void createVisitForPet_InvalidErrorResponse_ThrowsBadRequestException() throws JsonProcessingException {
        // Arrange
        VisitRequestDTO visitRequestDTO = new VisitRequestDTO(
                LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Test Visit",
                "1",
                "f470653d-05c5-4c45-b7a0-7d70f003d2ac",
                "testJwtToken",
                "2",
                "73b5c112-5703-4fb7-b7bc-ac8186811ae1"
        );

        // Mock the server error response with a bad request status and non-JSON body, which should trigger an IOException during parsing
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value()) // 400 status
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE) // setting non-JSON response type
                .setBody("Invalid response"));

        // Act
        Mono<VisitResponseDTO> resultMono = visitsServiceClient.createVisitForPet(visitRequestDTO);

        // Assert
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().contains("Bad Request")) // checking that the error message is what's set in the IOException catch block
                .verify();
    }


    @Test
    void getVisitsForPet() throws Exception {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(objectMapper.writeValueAsString(visitResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<VisitResponseDTO> visits = visitsServiceClient.getVisitsForPet("2");
        StepVerifier.create(visits)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }
    @Test
    void getVisitById() throws Exception {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).setBody(objectMapper.writeValueAsString(visitResponseDTO)).addHeader("Content-Type", "application/json"));

        Mono<VisitResponseDTO> visitResponseDTOMono = visitsServiceClient.getVisitByVisitId("773fa7b2-e04e-47b8-98e7-4adf7cfaaeee");
        StepVerifier.create(visitResponseDTOMono)
                .expectNextMatches(returnedVisitResponseDTO1 -> Objects.equals(returnedVisitResponseDTO1, visitResponseDTO))
                .verifyComplete();
    }

//    @Test
//    void getVisitsForPets_withAvailableVisitsService() {
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody("{\"items\":[{\"visitId\":\"773fa7b2-e04e-47b8-98e7-4adf7cfaaeee\"," +
//                        "\"date\":\"2018-11-15\",\"description\":\"test visit\",\"petId\":\"1\"}]}"));
//
//        Mono<Visits> visits = visitsServiceClient.getVisitsForPets(Collections.singletonList(1));
//
//        assertVisitDescriptionEquals(visits.block(), PET_ID,"test visit");
//    }


/*
    @Test
    void shouldDeleteVisitsForPet() throws JsonProcessingException {
        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
                .petId("15")
                .practitionerId(2)
                .visitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Cat is crazy")
                .status(Status.CANCELLED)
                .build();

        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final Mono<Void> empty = visitsServiceClient.deleteVisitByVisitId(visit.getVisitId());

        assertNull(empty.block());
    }
 */
    @Test
    void shouldCreateVisitForPet() throws JsonProcessingException {
        // Given
        final VisitRequestDTO visitRequest = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2023-10-01T13:00:00"))
                .description("Dog needs grooming")
                .petId(PET_ID)
                .practitionerId("3")
                .status(Status.UPCOMING)
                .build();

        final VisitResponseDTO expectedVisitResponse = VisitResponseDTO.builder()
                .visitId(UUID.randomUUID().toString())
                .visitDate(visitRequest.getVisitDate())
                .description(visitRequest.getDescription())
                .petId(visitRequest.getPetId())
                .practitionerId(visitRequest.getPractitionerId())
                .status(visitRequest.getStatus()) // use isStatus here
                .visitEndDate(visitRequest.getVisitDate().plusHours(1))
                .build();

        final String responseBody = objectMapper.writeValueAsString(expectedVisitResponse);

        // Mocking the server response
        server.enqueue(
                new MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setResponseCode(HttpStatus.OK.value())
                        .setBody(responseBody)
        );

        // When
        VisitResponseDTO actualVisitResponse = visitsServiceClient.createVisitForPet(visitRequest).block();

        // Then
        assertEquals(expectedVisitResponse.getVisitId(), actualVisitResponse.getVisitId());
        assertEquals(expectedVisitResponse.getVisitDate(), actualVisitResponse.getVisitDate());
        assertEquals(expectedVisitResponse.getDescription(), actualVisitResponse.getDescription());
        assertEquals(expectedVisitResponse.getPetId(), actualVisitResponse.getPetId());
        assertEquals(expectedVisitResponse.getPractitionerId(), actualVisitResponse.getPractitionerId());
        assertEquals(expectedVisitResponse.getStatus(), actualVisitResponse.getStatus());
    }

/*    @Test
    void shouldUpdateVisitsForPet() throws JsonProcessingException {

        final VisitDetails visit = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
                .petId("15")
                .practitionerId(2)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Cat is crazy")
                .status(Status.UPCOMING)
                .build();
        final VisitDetails visit2 = VisitDetails.builder()
                .visitId(UUID.randomUUID().toString())
                .petId("201")
                .practitionerId(22)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Dog is sick")
                .status(Status.UPCOMING)
                .build();

//        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
        final String body2 = objectMapper.writeValueAsString(objectMapper.convertValue(visit2, VisitDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body2));

//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));

        visitsServiceClient.updateVisitForPet(visit2);
        final VisitDetails petVisit = visitsServiceClient.getVisitsForPet("201").blockFirst();

        assertEquals(visit2.getVisitId(), petVisit.getVisitId());
        assertEquals(visit2.getPetId(), petVisit.getPetId());
        assertEquals(visit2.getPractitionerId(), petVisit.getPractitionerId());
        assertEquals(visit2.getVisitDate(), petVisit.getVisitDate());
        assertEquals(visit2.getDescription(), petVisit.getDescription());
        assertEquals(visit2.getStatus(), petVisit.getStatus());

    }*/

    /*
    @Test
    void shouldUpdateStatusForVisitByVisitId() throws JsonProcessingException {

        String status = "CANCELLED";

        final VisitResponseDTO visit = VisitResponseDTO.builder()
                .visitId(UUID.randomUUID().toString())
                .petId("201")
                .practitionerId("22")
                .visitDate(LocalDateTime.parse("2021-12-12T13:00"))
                .description("Dog is sick")
                .status(Status.REQUESTED)
                .build();

        String visitId = visit.getVisitId();

        StepVerifier.create(visitsServiceClient.updateStatusForVisitByVisitId(visitId, status))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visit.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visit.getPractitionerId(), visitDTO1.getPractitionerId());
                    assertEquals(visit.getStatus(), Status.CANCELLED);
                }).verifyComplete();

    }
     */


//    @Test
//    void shouldGetVisitsForPractitioner() throws JsonProcessingException {
//        final VisitDetails visit = VisitDetails.builder()
//                .visitId(UUID.randomUUID().toString())
//                .petId(21)
//                .practitionerId(2)
//                .date("2021-12-7")
//                .description("Cat is sick")
//                .status(false)
//                .build();
//
//        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final VisitDetails previousVisits = visitsServiceClient.getVisitForPractitioner(21).blockFirst();
//
//        assertEquals(visit.getVisitId(), previousVisits.getVisitId());
//        assertEquals(visit.getPetId(), previousVisits.getPetId());
//        assertEquals(visit.getPractitionerId(), previousVisits.getPractitionerId());
//        assertEquals(visit.getDate(), previousVisits.getDate());
//        assertEquals(visit.getDescription(), previousVisits.getDescription());
//        assertEquals(visit.getStatus(), previousVisits.getStatus());
//    }
//
//    @Test
//    void shouldGetVisitsByPractitionerIdandMonth() throws JsonProcessingException {
//        final VisitDetails visit = VisitDetails.builder()
//                .visitId(UUID.randomUUID().toString())
//                .petId(21)
//                .practitionerId(2)
//                .date("2021-12-7")
//                .description("Cat is sick")
//                .status(false)
//                .build();
//
//        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final VisitDetails previousVisits = visitsServiceClient.getVisitsByPractitionerIdAndMonth(21,"start","end").blockFirst();
//
//        assertEquals(visit.getVisitId(), previousVisits.getVisitId());
//        assertEquals(visit.getPetId(), previousVisits.getPetId());
//        assertEquals(visit.getPractitionerId(), previousVisits.getPractitionerId());
//        assertEquals(visit.getDate(), previousVisits.getDate());
//        assertEquals(visit.getDescription(), previousVisits.getDescription());
//        assertEquals(visit.getStatus(), previousVisits.getStatus());
//    }
//
//    @Test
//    void shouldGetPreviousVisitsForPet() throws JsonProcessingException {
//        final VisitDetails visit = VisitDetails.builder()
//                .visitId(UUID.randomUUID().toString())
//                .petId(21)
//                .practitionerId(2)
//                .date("2021-12-7")
//                .description("Cat is sick")
//                .status(false)
//                .build();
//
//        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final VisitDetails previousVisits = visitsServiceClient.getPreviousVisitsForPet(21).blockFirst();
//
//        assertEquals(visit.getVisitId(), previousVisits.getVisitId());
//        assertEquals(visit.getPetId(), previousVisits.getPetId());
//        assertEquals(visit.getPractitionerId(), previousVisits.getPractitionerId());
//        assertEquals(visit.getDate(), previousVisits.getDate());
//        assertEquals(visit.getDescription(), previousVisits.getDescription());
//        assertEquals(visit.getStatus(), previousVisits.getStatus());
//    }
//
//    @Test
//    void shouldGetScheduledVisitsForPet() throws JsonProcessingException {
//        final VisitDetails visit = VisitDetails.builder()
//                .visitId(UUID.randomUUID().toString())
//                .petId(21)
//                .practitionerId(2)
//                .date("2021-12-7")
//                .description("Cat is sick")
//                .status(true)
//                .build();
//
//        final String body = objectMapper.writeValueAsString(objectMapper.convertValue(visit, VisitDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final VisitDetails scheduledVisits = visitsServiceClient.getScheduledVisitsForPet(21).blockFirst();
//
//        assertEquals(visit.getVisitId(), scheduledVisits.getVisitId());
//        assertEquals(visit.getPetId(), scheduledVisits.getPetId());
//        assertEquals(visit.getPractitionerId(), scheduledVisits.getPractitionerId());
//        assertEquals(visit.getDate(), scheduledVisits.getDate());
//        assertEquals(visit.getDescription(), scheduledVisits.getDescription());
//        assertEquals(visit.getStatus(), scheduledVisits.getStatus());
//    }
/*
    private void assertVisitDescriptionEq(VisitDetails visits, String petId, String description) {
        assertEquals("773fa7b2-e04e-47b8-98e7-4adf7cfaaeee", visits.getVisitId());
        assertEquals(description, visits.getDescription());
    }
    private void assertVisitDescriptionEquals(Visits visits, String petId, String description) {
        assertEquals(1, visits.getItems().size());
        assertNotNull(visits.getItems().get(0));
        assertEquals(petId, visits.getItems().get(0).getPetId());
        assertEquals(description, visits.getItems().get(0).getDescription());
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        server.enqueue(response);
    }*/

    @Test
    void deleteAllCancelledVisits_shouldSucceed() {
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(204)); //no content

        Mono<Void> result = visitsServiceClient.deleteAllCancelledVisits();

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void deleteVisitByVisitId_shouldSucceed() {
        // Declare a testUUID to pass
        String testUUID = UUID.randomUUID().toString();

        // Enqueue mock respons of delete
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(204)); // No Content

        Mono<Void> result = visitsServiceClient.deleteVisitByVisitId(testUUID);

        StepVerifier.create(result)
                .verifyComplete();
    }



    //Reviews
    private static final String REVIEW_ID = UUID.randomUUID().toString();
    @Test
    void getReviewByReviewId() throws JsonProcessingException {
        ReviewResponseDTO reviewResponse = ReviewResponseDTO.builder()
                .reviewId(REVIEW_ID)
                .rating(4)
                .reviewerName("zako")
                .review("hahaha")
                .dateSubmitted(LocalDateTime.now())
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(reviewResponse)));

        Mono<ReviewResponseDTO> reviewMono = visitsServiceClient.getReviewByReviewId(REVIEW_ID);
        StepVerifier.create(reviewMono)
                .expectNextMatches(review -> review.getReviewId().equals(REVIEW_ID) && review.getRating() == 4)
                .verifyComplete();
    }

    //add visit
    @Test
    void addVisit_Valid() throws JsonProcessingException {
        // Arrange
        VisitRequestDTO visitRequestDTO = new VisitRequestDTO(
                LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "Routine Check-up",
                "1", // Pet ID
                "practitionerId", // Practitioner ID
                "jwtToken", // JWT Token
                "ownerId",
                "73b5c112-5703-4fb7-b7bc-ac8186811ae1"
        );

        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Routine Check-up")
                .petId("1")
                .practitionerId("practitionerId")
                .vetFirstName("John")
                .vetLastName("Doe")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

        // Mock the server response
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(visitResponseDTO)));

        // Act
        Mono<VisitRequestDTO> requestMono = Mono.just(visitRequestDTO); // Wrap VisitRequestDTO in Mono
        Mono<VisitResponseDTO> resultMono = visitsServiceClient.addVisit(requestMono); // Call addVisit with Mono

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(visitResponse -> visitResponse.getVisitId().equals(visitResponseDTO.getVisitId()))
                .verifyComplete();
    }



    @Test
    void createReview() throws JsonProcessingException {
        ReviewRequestDTO reviewRequest = ReviewRequestDTO.builder()
                .rating(5)
                .reviewerName("zako")
                .review("hadsks")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewResponseDTO reviewResponse = ReviewResponseDTO.builder()
                .reviewId(REVIEW_ID)
                .rating(5)
                .reviewerName("zako")
                .review("hadsks")
                .dateSubmitted(LocalDateTime.now())
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(reviewResponse)));

        Mono<ReviewResponseDTO> reviewMono = visitsServiceClient.createReview(Mono.just(reviewRequest));
        StepVerifier.create(reviewMono)
                .expectNextMatches(review -> review.getReviewId().equals(REVIEW_ID) && review.getRating() == 5)
                .verifyComplete();
    }


    @Test
    void updateReview() throws JsonProcessingException {
        ReviewRequestDTO updatedReviewRequest = ReviewRequestDTO.builder()
                .rating(5)
                .reviewerName("zako")
                .review("hadsks")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewResponseDTO updatedReviewResponse = ReviewResponseDTO.builder()
                .reviewId(REVIEW_ID)
                .rating(5)
                .reviewerName("zako")
                .review("hadsks")
                .dateSubmitted(LocalDateTime.now())
                .build();
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(updatedReviewResponse)));

        Mono<ReviewResponseDTO> reviewMono = visitsServiceClient.updateReview(REVIEW_ID, Mono.just(updatedReviewRequest));
        StepVerifier.create(reviewMono)
                .expectNextMatches(review -> review.getReviewId().equals(REVIEW_ID) && review.getRating() == 5)
                .verifyComplete();

    }


    @Test
    void getAllReviews() throws JsonProcessingException {
        ReviewResponseDTO review1 = ReviewResponseDTO.builder()
                .reviewId(REVIEW_ID)
                .rating(5)
                .reviewerName("zako")
                .review("hadsks")
                .dateSubmitted(LocalDateTime.now())
                .build();

        ReviewResponseDTO review2 = ReviewResponseDTO.builder()
                .reviewId(REVIEW_ID)
                .rating(3)
                .reviewerName("zako")
                .review("hadsks")
                .dateSubmitted(LocalDateTime.now())
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(new ReviewResponseDTO[]{review1, review2})));

        StepVerifier.create(visitsServiceClient.getAllReviews())
                .expectNext(review1)
                .expectNext(review2)
                .verifyComplete();
    }
//TODO I could not get this test to work no matter how hard i tried the date does not get passed down correctly
/*
    @Test
    void testUpdateVisitByVisitId() throws Exception {
        // Arrange
        VisitRequestDTO requestDTO = new VisitRequestDTO();
        requestDTO.setVisitDate(LocalDateTime.of(2025, 12, 24, 18, 0)); // Setting a specific date for testing
        requestDTO.setDescription("Dog Needs Physio-Therapy UPDATE");
        requestDTO.setPetId("0e4d8481-b611-4e52-baed-af16caa8bf8a");
        requestDTO.setPractitionerId("69f85d2e-625b-11ee-8c99-0242ac120002");
        requestDTO.setStatus(Status.UPCOMING);

        VisitResponseDTO responseDTO = new VisitResponseDTO();
        responseDTO.setVisitId(VISIT_ID);
        responseDTO.setVisitDate(requestDTO.getVisitDate().minusHours(4));
        responseDTO.setDescription(requestDTO.getDescription());
        responseDTO.setPetId(requestDTO.getPetId());
        responseDTO.setPractitionerId(requestDTO.getPractitionerId());
        responseDTO.setStatus(requestDTO.getStatus());
        responseDTO.setVisitEndDate(requestDTO.getVisitDate().plusHours(1));

        // Simulate HTTP response from the MockWebServer
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(responseDTO)));

        // Act
        Mono<VisitResponseDTO> result = visitsServiceClient.updateVisitByVisitId(VISIT_ID, Mono.just(requestDTO));

        // Assert using StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(updatedVisit -> {
                    // Assert each field
                    assertNotNull(updatedVisit);
                    assertEquals(VISIT_ID, updatedVisit.getVisitId());
                    assertEquals(requestDTO.getDescription(), updatedVisit.getDescription());
                    assertEquals(requestDTO.getVisitDate(), updatedVisit.getVisitDate());
                    assertEquals(requestDTO.getPetId(), updatedVisit.getPetId());
                    assertEquals(requestDTO.getPractitionerId(), updatedVisit.getPractitionerId());
                    assertEquals(requestDTO.getStatus(), updatedVisit.getStatus());
                    assertEquals(requestDTO.getVisitDate().plusHours(1), updatedVisit.getVisitEndDate());
                    return true;
                })
                .verifyComplete();
    }

 */

    
    @Test
    void testUpdateVisitByVisitId_BadRequest() {
        // Arrange
        VisitRequestDTO requestDTO = new VisitRequestDTO();
        requestDTO.setVisitDate(null); // Invalid input to trigger BadRequestException

        // Act and Assert
        StepVerifier.create(visitsServiceClient.updateVisitByVisitId(VISIT_ID, Mono.just(requestDTO)))
                .expectError(BadRequestException.class)
                .verify();
    }


    //Emergency
    private static final String EMERGENCY_ID = UUID.randomUUID().toString();

    /*@Test
    void createEmergency() throws JsonProcessingException {
        EmergencyRequestDTO emergencyRequest = EmergencyRequestDTO.builder()
                .urgencyLevel(UrgencyLevel.HIGH)
                .description("Emergency case")
                .visitDate(LocalDateTime.now())
                .emergencyType("Medical")
                .petName("Buddy")
                .build();

        EmergencyResponseDTO emergencyResponse = EmergencyResponseDTO.builder()
                .visitEmergencyId(EMERGENCY_ID)
                .urgencyLevel(UrgencyLevel.HIGH)
                .description("Emergency case")
                .visitDate(LocalDateTime.now())
                .emergencyType("Medical")
                .petName("Buddy")
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emergencyResponse)));

        Mono<EmergencyResponseDTO> emergencyMono = visitsServiceClient.createEmergency(Mono.just(emergencyRequest));
        StepVerifier.create(emergencyMono)
                .expectNextMatches(emergency -> emergency.getVisitEmergencyId().equals(EMERGENCY_ID) && emergency.getUrgencyLevel().equals(UrgencyLevel.HIGH))
                .verifyComplete();
    }

    @Test
    void updateEmergency() throws JsonProcessingException {
        EmergencyRequestDTO updatedEmergencyRequest = EmergencyRequestDTO.builder()
                .urgencyLevel(UrgencyLevel.HIGH)
                .description("Updated Emergency case")
                .visitDate(LocalDateTime.now())
                .emergencyType("Medical")
                .petName("Buddy")
                .build();

        EmergencyResponseDTO updatedEmergencyResponse = EmergencyResponseDTO.builder()
                .visitEmergencyId(EMERGENCY_ID)
                .urgencyLevel(UrgencyLevel.HIGH)
                .description("Updated Emergency case")
                .visitDate(LocalDateTime.now())
                .emergencyType("Medical")
                .petName("Buddy")
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(updatedEmergencyResponse)));

        Mono<EmergencyResponseDTO> emergencyMono = visitsServiceClient.updateEmergency(EMERGENCY_ID, Mono.just(updatedEmergencyRequest));
        StepVerifier.create(emergencyMono)
                .expectNextMatches(emergency -> emergency.getVisitEmergencyId().equals(EMERGENCY_ID) && emergency.getUrgencyLevel().equals(UrgencyLevel.HIGH))
                .verifyComplete();
    }

     */

    @Test
    void getAllEmergencies() throws JsonProcessingException {
        DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String fixedStr1 = "2024-09-28 19:54";
        String fixedStr2 = "2024-09-28 19:55";
        LocalDateTime fixedLdt1 = LocalDateTime.parse(fixedStr1, FMT);
        LocalDateTime fixedLdt2 = LocalDateTime.parse(fixedStr2, FMT);

        EmergencyRequestDTO emergencyRequestDTO = EmergencyRequestDTO.builder()
                .visitDate(fixedLdt1)
                .description("Updated Emergency")
                .petId("Oscar")
                .practitionerId("2332222232323234hhh232")
                .urgencyLevel(UrgencyLevel.MEDIUM)
                .emergencyType("Accident")
                .build();

        EmergencyRequestDTO emergencyRequestDTO2 = EmergencyRequestDTO.builder()
                .visitDate(fixedLdt2)
                .description("Updated Emergency2")
                .petId("Oscar2")
                .practitionerId("2332222232323234hhh232")
                .urgencyLevel(UrgencyLevel.MEDIUM)
                .emergencyType("Accident2")
                .build();

        Date fixedBirth = new Date(0);

        EmergencyResponseDTO emergency1 = EmergencyResponseDTO.builder()
                .visitEmergencyId(UUID.randomUUID().toString())
                .visitDate(fixedLdt1)
                .description(emergencyRequestDTO.getDescription())
                .petId(emergencyRequestDTO.getPetId())
                .petName("hamid")
                .petBirthDate(fixedBirth)
                .practitionerId(emergencyRequestDTO.getPractitionerId())
                .vetFirstName("carlos")
                .vetLastName("ambock")
                .vetEmail("carlos@gmail.com")
                .vetPhoneNumber("540-233-2323")
                .urgencyLevel(emergencyRequestDTO.getUrgencyLevel())
                .emergencyType(emergencyRequestDTO.getEmergencyType())
                .build();

        EmergencyResponseDTO emergency2 = EmergencyResponseDTO.builder()
                .visitEmergencyId(UUID.randomUUID().toString())
                .visitDate(fixedLdt2)
                .description(emergencyRequestDTO2.getDescription())
                .petId(emergencyRequestDTO2.getPetId())
                .petName("hamid")
                .petBirthDate(fixedBirth)
                .practitionerId(emergencyRequestDTO2.getPractitionerId())
                .vetFirstName("carlos")
                .vetLastName("ambock")
                .vetEmail("carlos@gmail.com")
                .vetPhoneNumber("540-233-2323")
                .urgencyLevel(emergencyRequestDTO2.getUrgencyLevel())
                .emergencyType(emergencyRequestDTO2.getEmergencyType())
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(new EmergencyResponseDTO[]{emergency1, emergency2})));

        StepVerifier.create(visitsServiceClient.getAllEmergency())
                .expectNext(emergency1)
                .expectNext(emergency2)
                .verifyComplete();
    }


    @Test
    void createEmergency() throws JsonProcessingException {
        // Predefine the visitEmergencyId to ensure it matches the expected value
        String EMERGENCY_ID = "df7da576-8f99-4d57-82ce-48e695416f8e"; // Example UUID

        // Create EmergencyRequestDTO object
        EmergencyRequestDTO emergencyRequestDTO = EmergencyRequestDTO.builder()
                .visitDate(LocalDateTime.now())
                .description("Updated Emergency")
                .petId("Oscar")
                .practitionerId("2332222232323234hhh232")
                .urgencyLevel(UrgencyLevel.HIGH)
                .emergencyType("Accident")
                .build();

        // Create EmergencyResponseDTO object, using the predefined EMERGENCY_ID
        EmergencyResponseDTO emergencyResponseDTO = EmergencyResponseDTO.builder()
                .visitEmergencyId(EMERGENCY_ID)
                .visitDate(emergencyRequestDTO.getVisitDate())
                .description(emergencyRequestDTO.getDescription())
                .petId(emergencyRequestDTO.getPetId())
                .petName("hamid")
                .petBirthDate(new Date()) // Using a new date for simplicity
                .practitionerId(emergencyRequestDTO.getPractitionerId())
                .vetFirstName("carlos")
                .vetLastName("ambock")
                .vetEmail("carlos@gmail.com")
                .vetPhoneNumber("540-233-2323")
                .urgencyLevel(emergencyRequestDTO.getUrgencyLevel())
                .emergencyType(emergencyRequestDTO.getEmergencyType())
                .build();

        // Set up the MockWebServer to return the mock response
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emergencyResponseDTO)));

        // Call the visitsServiceClient method to create the emergency
        Mono<EmergencyResponseDTO> emergencyMono = visitsServiceClient.createEmergency(Mono.just(emergencyRequestDTO));

        // Use StepVerifier to verify the response matches expected values
        StepVerifier.create(emergencyMono)
                .expectNextMatches(emergency ->
                        emergency.getVisitEmergencyId().equals(EMERGENCY_ID) && // Check if visitEmergencyId matches
                                emergency.getUrgencyLevel().equals(UrgencyLevel.HIGH)  // Check if urgencyLevel is HIGH
                )
                .verifyComplete();
    }


    @Test
    void getEmergencyVisitsForPet() throws Exception {
        DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String fixedStr = "2024-09-28 19:48";
        LocalDateTime fixedLdt = LocalDateTime.parse(fixedStr, FMT);

        EmergencyRequestDTO emergencyRequestDTO = EmergencyRequestDTO.builder()
                .visitDate(fixedLdt)
                .description("Updated Emergency")
                .petId("2")
                .practitionerId("2332222232323234hhh232")
                .urgencyLevel(UrgencyLevel.MEDIUM)
                .emergencyType("Accident")
                .build();

        EmergencyResponseDTO emergency1 = EmergencyResponseDTO.builder()
                .visitEmergencyId(UUID.randomUUID().toString())
                .visitDate(fixedLdt)
                .description(emergencyRequestDTO.getDescription())
                .petId(emergencyRequestDTO.getPetId())
                .petName("hamid")
                .petBirthDate(new Date(0))
                .practitionerId(emergencyRequestDTO.getPractitionerId())
                .vetFirstName("carlos")
                .vetLastName("ambock")
                .vetEmail("carlos@gmail.com")
                .vetPhoneNumber("540-233-2323")
                .urgencyLevel(emergencyRequestDTO.getUrgencyLevel())
                .emergencyType(emergencyRequestDTO.getEmergencyType())
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emergency1)));

        Flux<EmergencyResponseDTO> visits = visitsServiceClient.getEmergencyVisitForPet("2");

        StepVerifier.create(visits)
                .expectNext(emergency1)
                .verifyComplete();
    }


    @Test
    void getEmergencyByEmergencyId() throws JsonProcessingException {
        // Create an EmergencyRequestDTO with HIGH urgency level
        EmergencyRequestDTO emergencyRequestDTO = EmergencyRequestDTO.builder()
                .visitDate(LocalDateTime.now())
                .description("Updated Emergency")
                .petId("2")
                .practitionerId("2332222232323234hhh232")
                .urgencyLevel(UrgencyLevel.HIGH)  // Set urgency level to HIGH
                .emergencyType("Accident")
                .build();

        // Create an EmergencyResponseDTO with HIGH urgency level to match the request
        EmergencyResponseDTO emergencyResponse = EmergencyResponseDTO.builder()
                .visitEmergencyId(UUID.randomUUID().toString())
                .visitDate(emergencyRequestDTO.getVisitDate())
                .description(emergencyRequestDTO.getDescription())
                .petId(emergencyRequestDTO.getPetId())
                .petName("hamid")
                .petBirthDate(new Date())  // Set pet birth date
                .practitionerId(emergencyRequestDTO.getPractitionerId())
                .vetFirstName("carlos")
                .vetLastName("ambock")
                .vetEmail("carlos@gmail.com")
                .vetPhoneNumber("540-233-2323")
                .urgencyLevel(UrgencyLevel.HIGH)  // Set urgency level to HIGH
                .emergencyType(emergencyRequestDTO.getEmergencyType())
                .build();

        // Enqueue a mock response with the above emergencyResponse
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emergencyResponse)));

        // Fetch the emergency by ID from the service client
        Mono<EmergencyResponseDTO> emergencyMono = visitsServiceClient.getEmergencyByEmergencyId(emergencyResponse.getVisitEmergencyId());

        // Verify the response using StepVerifier
        StepVerifier.create(emergencyMono)
                .expectNextMatches(emergency ->
                        emergency.getVisitEmergencyId().equals(emergencyResponse.getVisitEmergencyId()) &&  // Check if ID matches
                                emergency.getUrgencyLevel().equals(UrgencyLevel.HIGH) &&  // Check urgency level is HIGH
                                emergency.getDescription().equals("Updated Emergency"))  // Ensure description matches
                .verifyComplete();
    }


    /*

    @Test
    void getEmergencyByEmergencyId() throws JsonProcessingException {
        EmergencyResponseDTO emergencyResponse = EmergencyResponseDTO.builder()
                .visitEmergencyId(EMERGENCY_ID)
                .urgencyLevel(UrgencyLevel.HIGH)
                .description("Specific Emergency case")
                .visitDate(LocalDateTime.now())
                .emergencyType("Medical")
                .petName("Buddy")
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emergencyResponse)));

        Mono<EmergencyResponseDTO> emergencyMono = visitsServiceClient.getEmergencyByEmergencyId(EMERGENCY_ID);
        StepVerifier.create(emergencyMono)
                .expectNextMatches(emergency -> emergency.getVisitEmergencyId().equals(EMERGENCY_ID) && emergency.getUrgencyLevel().equals(UrgencyLevel.HIGH))
                .verifyComplete();
    }

    @Test
    void deleteEmergency() throws JsonProcessingException {
        EmergencyResponseDTO emergencyResponse = EmergencyResponseDTO.builder()
                .visitEmergencyId(EMERGENCY_ID)
                .urgencyLevel(UrgencyLevel.HIGH)
                .description("Emergency to delete")
                .visitDate(LocalDateTime.now())
                .emergencyType("Medical")
                .petName("Buddy")
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emergencyResponse)));

        Mono<EmergencyResponseDTO> emergencyMono = visitsServiceClient.deleteEmergency(EMERGENCY_ID);
        StepVerifier.create(emergencyMono)
                .expectNextMatches(emergency -> emergency.getVisitEmergencyId().equals(EMERGENCY_ID))
                .verifyComplete();
    }

     */
  
    @Test
      void updateVisitStatus_ShouldSucceed_WhenStatusUpdatedToCancelled() {
          String visitId = "12345";
          String status = "CANCELLED";

        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId(visitId)
                .status(Status.CANCELLED)
                .description("Test visit with cancelled status")
                .build();

        // Mocking the service client to return the expected response
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{ \"visitId\": \"" + visitId + "\", \"status\": \"CANCELLED\", \"description\": \"Test visit with cancelled status\" }")
                .setResponseCode(200));

        Mono<VisitResponseDTO> result = visitsServiceClient.patchVisitStatus(visitId, status);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getVisitId().equals(visitId) && response.getStatus().equals(Status.CANCELLED))
                .verifyComplete();
    }

    // Test for the NOT_FOUND scenario (when visit does not exist)
    @Test
    void updateVisitStatus_ShouldReturnNotFound_WhenVisitDoesNotExist() {
        String visitId = "nonExistentVisitId";
        String status = "CANCELLED";

        // Mocking the service client to simulate a 404 Not Found response
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(404));

        Mono<VisitResponseDTO> result = visitsServiceClient.patchVisitStatus(visitId, status);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException.NotFound)
                .verify();
    }

    @Test
    void deleteReview_Success() throws JsonProcessingException {
        // Simulate a successful deletion response
        ReviewResponseDTO expectedResponse = ReviewResponseDTO.builder()
                .reviewId(REVIEW_ID)
                .build();

        // Enqueue a successful response
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedResponse)));

        // Call the deleteReview method
        Mono<ReviewResponseDTO> responseMono = visitsServiceClient.deleteReview(REVIEW_ID);

        // Verify that the response matches the expected values
        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.getReviewId().equals(REVIEW_ID))
                .verifyComplete();
    }

    @Test
    void deleteReview_Failure() {
        // Enqueue a failure response (404 Not Found)
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Call the deleteReview method
        Mono<ReviewResponseDTO> responseMono = visitsServiceClient.deleteReview(REVIEW_ID);

        // Verify that an error occurs
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException.NotFound)
                .verify();
    }

    @Test
    void getReviewsByOwnerId_shouldReturnReviews_whenOwnerIdIsValid() throws JsonProcessingException {
        ReviewResponseDTO review1 = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID().toString())
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .build();

        ReviewResponseDTO review2 = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID().toString())
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(Arrays.asList(review1, review2))));

        Flux<ReviewResponseDTO> reviewsFlux = visitsServiceClient.getReviewsByOwnerId("validOwnerId");
        StepVerifier.create(reviewsFlux)
                .expectNext(review1)
                .expectNext(review2)
                .verifyComplete();
    }

    @Test
    void getReviewsByOwnerId_shouldReturnEmpty_whenOwnerIdHasNoReviews() {
        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("[]"));

        Flux<ReviewResponseDTO> reviewsFlux = visitsServiceClient.getReviewsByOwnerId("ownerIdWithNoReviews");
        StepVerifier.create(reviewsFlux)
                .verifyComplete();
    }

    @Test
    void addCustomerReview_shouldReturnReview_whenRequestIsValid() throws JsonProcessingException {
        ReviewRequestDTO reviewRequest = ReviewRequestDTO.builder()
                .review("Great service!")
                .rating(5)
                .build();

        ReviewResponseDTO reviewResponse = ReviewResponseDTO.builder()
                .reviewId(UUID.randomUUID().toString())
                .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .review("Great service!")
                .rating(5)
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(reviewResponse)));

        Mono<ReviewResponseDTO> reviewMono = visitsServiceClient.addCustomerReview("validOwnerId", reviewRequest);
        StepVerifier.create(reviewMono)
                .expectNext(reviewResponse)
                .verifyComplete();
    }

    @Test
    void addCustomerReview_shouldReturnError_whenRequestIsInvalid() {
        ReviewRequestDTO reviewRequest = ReviewRequestDTO.builder()
                .review("")
                .rating(0)
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value()));

        Mono<ReviewResponseDTO> reviewMono = visitsServiceClient.addCustomerReview("e6c7398e-8ac4-4e10-9ee0-03ef33f0361e", reviewRequest);
        StepVerifier.create(reviewMono)
                .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException.BadRequest)
                .verify();
    }

    @Test
    void deleteReview_shouldSucceed_whenReviewExists() {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value()));

        Mono<Void> result = visitsServiceClient.deleteReview("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", UUID.randomUUID().toString());
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void deleteReview_shouldReturnServerError_whenServerFails() {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        Mono<Void> result = visitsServiceClient.deleteReview("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", UUID.randomUUID().toString());
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Server error during review deletion"))
                .verify();
    }

    @Test
    void getAllVetsForAvailability_shouldReturnVets() throws JsonProcessingException {
        VetResponseDTO vet1 = VetResponseDTO.builder()
                .vetId("vet-123")
                .firstName("John")
                .lastName("Doe")
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(Arrays.asList(vet1))));

        Flux<VetResponseDTO> result = visitsServiceClient.getAllVetsForAvailability();

        StepVerifier.create(result)
                .expectNextMatches(vet -> vet.getVetId().equals("vet-123"))
                .verifyComplete();
    }

    @Test
    void getAllVetsForAvailability_whenServerError_shouldReturnError() {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        Flux<VetResponseDTO> result = visitsServiceClient.getAllVetsForAvailability();

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    void getAvailableTimeSlots_shouldReturnTimeSlots() throws JsonProcessingException {
        TimeSlotDTO slot1 = new TimeSlotDTO(
                LocalDateTime.of(2025, 10, 13, 9, 0),
                LocalDateTime.of(2025, 10, 13, 10, 0),
                true
        );

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(Arrays.asList(slot1))));

        Flux<TimeSlotDTO> result = visitsServiceClient.getAvailableTimeSlots("vet-123", "2025-10-13");

        StepVerifier.create(result)
                .expectNextMatches(slot -> slot.getStartTime().equals(LocalDateTime.of(2025, 10, 13, 9, 0)))
                .verifyComplete();
    }

    @Test
    void getAvailableTimeSlots_whenVetNotFound_shouldReturnError() {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        Flux<TimeSlotDTO> result = visitsServiceClient.getAvailableTimeSlots("invalid-vet", "2025-10-13");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException)
                .verify();
    }

    @Test
    void getAvailableDates_whenVetNotFound_shouldReturnError() {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        Flux<String> result = visitsServiceClient.getAvailableDates("invalid-vet", "2025-10-13", "2025-10-20");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException)
                .verify();
    }

    @Test
    void getVeterinarianAvailability_shouldReturnVet() throws JsonProcessingException {
        VetResponseDTO vet = VetResponseDTO.builder()
                .vetId("vet-123")
                .firstName("John")
                .lastName("Doe")
                .build();

        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(vet)));

        Mono<VetResponseDTO> result = visitsServiceClient.getVeterinarianAvailability("vet-123");

        StepVerifier.create(result)
                .expectNextMatches(v -> v.getVetId().equals("vet-123"))
                .verifyComplete();
    }

    @Test
    void getVeterinarianAvailability_whenVetNotFound_shouldReturnError() {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        Mono<VetResponseDTO> result = visitsServiceClient.getVeterinarianAvailability("invalid-vet");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException)
                .verify();
    }


}