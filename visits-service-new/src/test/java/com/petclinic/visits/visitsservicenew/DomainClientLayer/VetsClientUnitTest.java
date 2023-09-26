package com.petclinic.visits.visitsservicenew.DomainClientLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(VetsClient.class)
class VetsClientUnitTest {

    @MockBean
    private WebClient webClient;

    @MockBean
    private VetsClient vetsClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize(){
        vetsClient = new VetsClient("localhost", String.valueOf(mockBackEnd.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException{
        mockBackEnd.shutdown();
    }

    @Test
    void getVetByVetId_Valid() throws IOException {
        // Create a sample vet response
        VetDTO vetDTO = new VetDTO();
        vetDTO.setVetId("123");
        vetDTO.setFirstName("John");
        vetDTO.setLastName("Doe");

        // Enqueue a mock response from the server
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(vetDTO))
        );

        // Call the method and verify the response
        Mono<VetDTO> vetDTOMono = vetsClient.getVetByVetId("123");
        StepVerifier.create(vetDTOMono)
                .expectNextMatches(response -> response.getFirstName().equals("John"))
                .verifyComplete();
    }

    @Test
    void getVetByVetId_VetNotFound() {
        String invalidVetId = "3333";

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", "application/json"));

        Mono<VetDTO> result = vetsClient.getVetByVetId(invalidVetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException && throwable.getMessage().equals("No veterinarian was found with vetId: " + invalidVetId))
                .verify();
    }

}