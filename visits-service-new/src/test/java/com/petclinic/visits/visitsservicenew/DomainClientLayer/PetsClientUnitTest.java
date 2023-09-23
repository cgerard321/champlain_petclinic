package com.petclinic.visits.visitsservicenew.DomainClientLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebFluxTest(PetsClient.class)
class PetsClientUnitTest {

    @MockBean
    private WebClient webClient;

    @MockBean
    private PetsClient petsClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize(){
        petsClient = new PetsClient("localhost", String.valueOf(mockBackEnd.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException{
        mockBackEnd.shutdown();
    }

    @Test
    void getPetById_Valid() throws IOException {
        // Create a sample pet response
        PetResponseDTO petResponseDTO = new PetResponseDTO("123", "Billy", new Date(), "dog", "123");

        // Enqueue a mock response from the server
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(petResponseDTO))
        );

        // Call the method and verify the response
        Mono<PetResponseDTO> petResponseDTOMono = petsClient.getPetById(123);
        StepVerifier.create(petResponseDTOMono)
                .expectNextMatches(response -> response.getName().equals("Billy"))
                .verifyComplete();
    }


    @Test
    void getPetById_PetNotFound() {
        String invalidId = "3333";

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", "application/json"));

        Mono<PetResponseDTO> result = petsClient.getPetById(Integer.parseInt(invalidId));


        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException && throwable.getMessage().equals("No pet was found with petId: " + invalidId))
                .verify();
    }

}