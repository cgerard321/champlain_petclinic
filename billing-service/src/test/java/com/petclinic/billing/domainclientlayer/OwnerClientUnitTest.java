package com.petclinic.billing.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.billing.datalayer.OwnerResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

public class OwnerClientUnitTest {
    private OwnerClient ownerClient;
    private WebTestClient webTestClient;
    private static MockWebServer mockBackEnd;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WebClient webClient;
    @BeforeAll
    public static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }
    @BeforeEach
    public void initialize(){
        ownerClient = new OwnerClient("localhost", String.valueOf(mockBackEnd.getPort()));
        webTestClient = WebTestClient.bindToController(ownerClient).build();
    }
    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void getOwnerByOwnerId_Valid() throws JsonProcessingException {
        String ownerId = "123";
        OwnerResponseDTO ownerResponseDTO = new OwnerResponseDTO(ownerId, "John", "Doe", "address", "city", "514", "string", null, null);

        mockBackEnd.enqueue(new MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(ownerResponseDTO))
        );

        Mono<OwnerResponseDTO> ownerResponseDTOMono = ownerClient.getOwnerByOwnerId(ownerId);

        StepVerifier.create(ownerResponseDTOMono)
                .expectNextMatches(ownerResponseDTO1 -> ownerResponseDTO1.getOwnerId().equals(ownerId))
                .verifyComplete();
    }
}