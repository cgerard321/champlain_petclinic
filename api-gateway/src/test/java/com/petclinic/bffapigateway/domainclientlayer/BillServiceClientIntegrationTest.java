package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Bills.BillDetails;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class BillServiceClientIntegrationTest {

    private BillServiceClient billServiceClient;

    private static MockWebServer server;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void beforeAllSetUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }
    @BeforeEach
    void setUp() {
        billServiceClient = new BillServiceClient(WebClient.builder(),
                server.getHostName(),
                String.valueOf(server.getPort()));
    }
    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @AfterEach
    void shutdown() throws IOException {
        this.server.shutdown();
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        server.enqueue(response);
    }

    private final BillResponseDTO billResponseDTO = BillResponseDTO.builder()
            .billId("1")
            .amount(100.0)
            .customerId(1)
            .vetId("1")
            .visitType("Check up")
            .date(null)
            .build();

    @Test
    void getBillById() throws Exception {
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(billResponseDTO)).addHeader("Content-Type", "application/json"));

        Mono<BillResponseDTO> billResponseDTOMono = billServiceClient.getBilling("1");
        StepVerifier.create(billResponseDTOMono)
                .expectNextMatches(returnedBillResponseDTO1 -> returnedBillResponseDTO1.getBillId().equals("1"))
                .verifyComplete();
    }

    @Test
    void shouldDeleteBill() throws JsonProcessingException {
        final BillDetails bill = BillDetails.builder()
                .billId(UUID.randomUUID().toString())
                .vetId("15")
                .customerId(2)
                .date(null)
                .amount(100)
                .visitType("Check")
                .build();

        final String body = mapper.writeValueAsString(mapper.convertValue(bill, BillDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final Mono<Void> empty = billServiceClient.deleteBill(bill.getBillId());

        assertNull(empty.block());
    }


}