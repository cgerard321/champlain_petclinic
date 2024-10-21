package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.petclinic.bffapigateway.dtos.Emailing.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.io.IOException;

import java.time.Instant;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

public class EmailingServiceClientIntegrationTest {

    private static MockWebServer mockWebServer;
    private EmailingServiceClient emailingServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void initialize() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        emailingServiceClient = new EmailingServiceClient(webClientBuilder, "localhost", String.valueOf(mockWebServer.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getAllEmails() throws Exception {
        EmailModelResponseDTO emailModelResponseDTO = new EmailModelResponseDTO(
                120,"xilef992@gmail.com","cardio","MyHtmlBody","Sent"
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emailModelResponseDTO)));

        Flux<EmailModelResponseDTO> emailFlux = emailingServiceClient.getAllEmails();

        StepVerifier.create(emailFlux)
                .expectNext(emailModelResponseDTO)
                .verifyComplete();
    }

    @Test
    void addHtmlTemplate() throws Exception {
        String templateName = "testTemplate";
        String htmlContent = "<h1>HTML Template</h1>";

        mockWebServer.enqueue(new MockResponse()
                .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .setBody("Template added successfully"));

        Mono<String> result = emailingServiceClient.addHtmlTemplate(templateName, htmlContent);

        StepVerifier.create(result)
                .expectNext("Template added successfully")
                .verifyComplete();
    }

    @Test
    void sendEmail() throws Exception {
        DirectEmailModelRequestDTO requestDTO = new DirectEmailModelRequestDTO(
                "xilef992@gmail.com",
                "Your Email Title",
                "Default",
                "PetClinic",
                "YourpetIsDead, Please pick it up",
                "This is a footer",
                "John Porc",
                "Felix"

        );

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<HttpStatus> result = emailingServiceClient.sendEmail(requestDTO);

        StepVerifier.create(result)
                .expectNext(HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void sendEmailNotification() throws Exception {
        NotificationEmailModelRequestDTO notificationRequestDTO = new NotificationEmailModelRequestDTO(
                "xilef992@gmail.com",
                "Your Email Title",
                "Default",
                "PetClinic",
                "YourpetIsDead, Please pick it up",
                "This is a footer",
                "John Porc",
                "Felix",
                Instant.now().atZone(ZoneOffset.ofHours(-4)).toLocalDateTime()
        );

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<HttpStatus> result = emailingServiceClient.sendEmailNotification(notificationRequestDTO);

        StepVerifier.create(result)
                .expectNext(HttpStatus.OK)
                .verifyComplete();
    }
    @Test
    void sendRawEmail() throws Exception {
        RawEmailModelRequestDTO rawRequestDTO = new RawEmailModelRequestDTO(
                "xilef992@gmail.com",
                "Your Email Title",
                "Default"
        );

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<HttpStatus> result = emailingServiceClient.sendRawEmail(rawRequestDTO);

        StepVerifier.create(result)
                .expectNext(HttpStatus.OK)
                .verifyComplete();
    }
    @Test
    void getAllReceivedEmails() throws Exception {
        // Create a sample ReceivedEmailResponseDTO object
        ReceivedEmailResponseDTO receivedEmailResponseDTO = new ReceivedEmailResponseDTO(
                "xilef992@gmail.com", "Subject", new Date(), "a body"
        );

        // Enqueue a mock response from the server
        mockWebServer.enqueue(new MockResponse()
                .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(List.of(receivedEmailResponseDTO))) // Wrap in a list for the Flux response
        );

        // Call the method to test
        Flux<ReceivedEmailResponseDTO> emailFlux = emailingServiceClient.getAllReceivedEmails();

        // Verify the response using StepVerifier
        StepVerifier.create(emailFlux)
                .expectNext(receivedEmailResponseDTO) // Expect the received email response DTO
                .verifyComplete();
    }
}