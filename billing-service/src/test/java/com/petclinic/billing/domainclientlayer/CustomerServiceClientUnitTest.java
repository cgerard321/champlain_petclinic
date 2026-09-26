package com.petclinic.billing.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.billing.domainclientlayer.models.CustomerResponseModel;
import com.petclinic.billing.exceptionshandling.exceptions.NotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

import java.io.IOException;
import java.rmi.ServerException;

public class CustomerServiceClientUnitTest {

    private CustomerServiceClient customerServiceClient;
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
    public void initialize() {
        customerServiceClient = new CustomerServiceClient("localhost", String.valueOf(mockBackEnd.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void getCustomerByCustomerId_Valid() throws JsonProcessingException {
        String customerId = "123";
        CustomerResponseModel customerResponseModel = new CustomerResponseModel(customerId, "John", "Doe", "address", "city", "514"/*, "string", null, null*/);

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(customerResponseModel))
        );

        Mono<CustomerResponseModel> customerResponseDTOMono = customerServiceClient.getCustomerByCustomerId(customerId);

        StepVerifier.create(customerResponseDTOMono)
                .expectNextMatches(customerResponseDTO1 -> customerResponseDTO1.getCustomerId().equals(customerId))
                .verifyComplete();
    }

    @Test
    public void getCustomerByCustomerId_Invalid() {
        String invalidId = "00000000";

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", "application/json"));

        Mono<CustomerResponseModel> result = customerServiceClient.getCustomerByCustomerId(invalidId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException && throwable.getMessage().equals("Owner not found with ownerId: " + invalidId))
                .verify();
    }

    @Test
    public void getCustomerByCustomerId_ClientError() {
        String customerId = "000";

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", "application/json"));

        Mono<CustomerResponseModel> result = customerServiceClient.getCustomerByCustomerId(customerId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException && throwable.getMessage().equals("Client error for customerId: " + customerId))
                .verify();
    }

    @Test
    public void getCustomerByCustomerId_ServerError() {
        String customerId = "000";

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Content-Type", "application/json"));

        Mono<CustomerResponseModel> result = customerServiceClient.getCustomerByCustomerId(customerId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ServerException && throwable.getMessage().equals("Server error for customerId: " + customerId))
                .verify();
    }
}
