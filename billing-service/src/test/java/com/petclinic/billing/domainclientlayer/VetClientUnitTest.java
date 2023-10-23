package com.petclinic.billing.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.billing.datalayer.VetResponseDTO;
import okhttp3.mockwebserver.MockResponse;
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
import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;

public class VetClientUnitTest {

    private VetClient vetClient;
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
        vetClient = new VetClient("localhost", String.valueOf(mockBackEnd.getPort()));
        webTestClient = WebTestClient.bindToController(vetClient).build();
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
}