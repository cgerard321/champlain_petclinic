package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceClientIntegrationTest {

    @MockBean
    private ImageServiceClient imageServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void initialize() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        imageServiceClient = new ImageServiceClient(webClientBuilder, "localhost",
                String.valueOf(mockWebServer.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

//    @Test
//    void whenAddImage_thenReturnsImage() {
//        ImageRequestDTO imageRequestDTO = ImageRequestDTO.builder()
//                .imageId("4affcab7-3ab1-4917-a114-2b6301aa5565")
//                .imageData("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/4QAWRXhpZgAATU0AKgAAAAgAA1EQAAEAAAABAQAAAFERAAEAAAABAAAAAFESAAEAAAABAAAAAFIAAAEAAAABAAAAAFgAAAEAAAABAAAAAABgAAAEAAAABAAAAAABkAAAEAAAABAAAAAABMAAEAAAABAAAAAABQAAAEAAAABAAAAAABYAAE
//    }
}