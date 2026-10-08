package com.petclinic.customersservice.domainclientlayer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CartServiceClientTest {

    private static MockWebServer mockBackEnd;
    private CartServiceClient cartServiceClient;

    @BeforeAll
    static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        cartServiceClient = new CartServiceClient(
                WebClient.builder(),
                "localhost",
                String.valueOf(mockBackEnd.getPort())
        );
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void deleteCartByCustomerId_ShouldCallCustomerCartDeleteEndpoint() throws InterruptedException {
        String customerId = "11111111-1111-4111-8111-111111111111";

        mockBackEnd.enqueue(new MockResponse().setResponseCode(204));

        StepVerifier.create(cartServiceClient.deleteCartByCustomerId(customerId))
                .verifyComplete();

        RecordedRequest request = mockBackEnd.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("DELETE", request.getMethod());
        assertEquals("/api/v1/customers/" + customerId + "/cart", request.getPath());
    }
}
