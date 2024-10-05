package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Products.ImageResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void whenGetImageByImageId_thenReturnImageResponse() throws JsonProcessingException {
        ImageResponseDTO imageResponseDTO = new ImageResponseDTO(
                "imageId",
                "imageName",
                "imageType",
                "imageData".getBytes()
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(imageResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ImageResponseDTO> imageResponseDTOMono = imageServiceClient
                .getImageByImageId("imageId");

        StepVerifier.create(imageResponseDTOMono)
                .expectNextMatches(imageResponse -> imageResponse.getImageId().equals("imageId")
                        && imageResponse.getImageName().equals("imageName")
                        && imageResponse.getImageType().equals("imageType"))
                .verifyComplete();
    }

    @Test
    void whenAddImage_thenReturnImageResponse() throws JsonProcessingException {
        byte[] imageDataBytes = "sampleImageData".getBytes();
        ImageResponseDTO imageResponseDTO = new ImageResponseDTO(
                "imageId",
                "imageName",
                "imageType",
                imageDataBytes
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(imageResponseDTO))
                .addHeader("Content-Type", "application/json"));

        String imageName = "testImage";
        String imageType = "image/png";
        FilePart imageData = mock(FilePart.class);

        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

        DataBuffer dataBuffer = dataBufferFactory.wrap(imageDataBytes);

        when(imageData.filename()).thenReturn("testImage.png");
        when(imageData.content()).thenReturn(Flux.just(dataBuffer));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_PNG);
        when(imageData.headers()).thenReturn(httpHeaders);

        Mono<ImageResponseDTO> imageResponseDTOMono = imageServiceClient
                .createImage(imageName, imageType, imageData);

        StepVerifier.create(imageResponseDTOMono)
                .expectNextMatches(imageResponse -> imageResponse.getImageId().equals("imageId")
                        && imageResponse.getImageName().equals("imageName")
                        && imageResponse.getImageType().equals("imageType"))
                .verifyComplete();
    }
}