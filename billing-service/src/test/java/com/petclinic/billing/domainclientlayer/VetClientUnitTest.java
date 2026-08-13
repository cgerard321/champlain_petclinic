package com.petclinic.billing.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.billing.datalayer.VetResponseDTO;
import com.petclinic.billing.exceptions.NotFoundException;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;
import java.rmi.ServerException;

public class VetClientUnitTest {

    private VetClient vetClient;
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
        vetClient = new VetClient("localhost", String.valueOf(mockBackEnd.getPort()));
    }
    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void getVetByVetId_Valid() throws JsonProcessingException {
        String vetId = "123";
        VetResponseDTO vetResponseDTO = new VetResponseDTO(vetId, "1", "John", "Doe", "email", "1234567890", "resume", true, null);

        mockBackEnd.enqueue(new MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(objectMapper.writeValueAsString(vetResponseDTO))
        );

        Mono<VetResponseDTO> result = vetClient.getVetByVetId(vetId);
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getVetId().equals(vetId) &&
                        response.getFirstName().equals("John") &&
                        response.getLastName().equals("Doe"))
                .verifyComplete();
    }

    @Test
    public void getVetByVetId_Invalid(){
        String invalidId = "00000000";

        mockBackEnd.enqueue(new MockResponse()
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setResponseCode(404)
                        .addHeader("Content-Type", "application/json"));
        Mono<VetResponseDTO> result = vetClient.getVetByVetId(invalidId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException && throwable.getMessage().equals("Vet not found with vetId: " + invalidId))
                .verify();
    }

    @Test
    public void getVetByVetId_ClientError() {
        String vetId = "456";

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", "application/json"));

        Mono<VetResponseDTO> result = vetClient.getVetByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Client error for vetId: " + vetId))
                .verify();
    }

    @Test
    public void getVetByVetId_ServerError() {
        String vetId = "789";

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", "application/json"));

        Mono<VetResponseDTO> result = vetClient.getVetByVetId(vetId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ServerException && throwable.getMessage().equals("Server error for vetId: " + vetId))
                .verify();
    }
}