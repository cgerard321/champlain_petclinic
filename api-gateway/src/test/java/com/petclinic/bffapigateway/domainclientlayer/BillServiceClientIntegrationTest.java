package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Bills.BillDetails;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


class BillServiceClientIntegrationTest {

    private BillServiceClient billServiceClient;

    private static MockWebServer server;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp(){

        server = new MockWebServer();
        billServiceClient = new BillServiceClient(
                WebClient.builder(),
                server.getHostName(),
                String.valueOf(server.getPort())
        );
        mapper = new ObjectMapper();
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
            .customerId("1")
            .vetId("1")
            .visitType("Check up")
            .date( null)
            .billStatus(BillStatus.PAID)
            .build();

    private final BillResponseDTO billResponseDTO2 = BillResponseDTO.builder()
            .billId("2")
            .amount(150.0)
            .customerId("2")
            .vetId("2")
            .visitType("Check up")
            .date(null)
            .billStatus(BillStatus.UNPAID)
            .build();

    private final BillResponseDTO billResponseDTO3 = BillResponseDTO.builder()
            .billId("3")
            .amount(250.0)
            .customerId("3")
            .vetId("3")
            .visitType("Check up")
            .date(null)
            .billStatus(BillStatus.OVERDUE)

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
    void getBillByVetId() throws Exception {
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(billResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillsByVetId("1");
        StepVerifier.create(billResponseDTOMono)
                .expectNextMatches(returnedBillResponseDTO1 -> returnedBillResponseDTO1.getVetId().equals("1"))
                .verifyComplete();
    }

    @Test
    void getBillsByCustomerId() throws Exception {
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(billResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillsByOwnerId("1");
        StepVerifier.create(billResponseDTOMono)
                .expectNextMatches(returnedBillResponseDTO1 -> returnedBillResponseDTO1.getCustomerId().equals("1"))
                .verifyComplete();
    }

    @Test
    void shouldDeleteBill() throws JsonProcessingException {
        final BillDetails bill = BillDetails.builder()
                .billId(UUID.randomUUID().toString())
                .vetId("15")
                .customerId("2")
                .date(null)
                .billStatus(BillStatus.PAID)
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

    @Test
    void shouldDeleteBillByVetId() throws JsonProcessingException {
        final BillDetails bill = BillDetails.builder()
                .billId(UUID.randomUUID().toString())
                .vetId("15")
                .customerId("2")
                .date(null)
                .billStatus(BillStatus.UNPAID)
                .amount(100)
                .visitType("Check")
                .build();

        final String body = mapper.writeValueAsString(mapper.convertValue(bill, BillDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final Flux<Void> empty = billServiceClient.deleteBillsByVetId(bill.getVetId());

        StepVerifier.create(empty)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldDeleteBillByCustomerId() throws JsonProcessingException {
        final BillDetails bill = BillDetails.builder()
                .billId(UUID.randomUUID().toString())
                .vetId("15")
                .customerId("2")
                .date(null)
                .billStatus(BillStatus.UNPAID)
                .amount(100)
                .visitType("Check")
                .build();

        final String body = mapper.writeValueAsString(mapper.convertValue(bill, BillDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final Flux<Void> empty = billServiceClient.deleteBillsByCustomerId(bill.getCustomerId());

        StepVerifier.create(empty)
                .expectComplete()
                .verify();
    }

    @Test
    void createBill() throws JsonProcessingException {
        // Create a sample BillRequestDTO object to send in the request
        BillRequestDTO billRequest = new BillRequestDTO();
        billRequest.setVetId("1");
        billRequest.setCustomerId("1");
        billRequest.setDate(null);
        billRequest.setBillStatus(BillStatus.PAID);
        billRequest.setAmount(100.0);
        billRequest.setVisitType("Check up");

        // Serialize the BillRequestDTO object to JSON
        String requestJson = mapper.writeValueAsString(billRequest);

        // Prepare a MockResponse for the createBill request
        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(requestJson)
        );

        // Send a request to create the bill
        Mono<BillResponseDTO> createdBillMono = billServiceClient.createBill(billRequest);

        // Verify the response
        StepVerifier.create(createdBillMono)
                .expectNextMatches(createdBill -> {
                    assertNotNull(createdBill);
                    assertEquals("1", createdBill.getVetId());
                    assertEquals(100.0, createdBill.getAmount());
                    assertEquals("Check up", createdBill.getVisitType());
                    return true;
                })
                .verifyComplete();
    }


    @Test
    void getAllBills() throws JsonProcessingException {
        // Prepare a list of bill responses as if they were returned from the service
        List<BillResponseDTO> billResponseList = Arrays.asList(
            billResponseDTO, billResponseDTO2, billResponseDTO3
        );

        final String body = mapper.writeValueAsString(billResponseList);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Flux<BillResponseDTO> billResponseFlux = billServiceClient.getAllBilling();

        StepVerifier.create(billResponseFlux.collectList())
                .expectNextMatches(returnedBillList -> {
                    assertEquals(3, returnedBillList.size());
                    assertTrue(returnedBillList.stream().anyMatch(bill -> "1".equals(bill.getBillId())));
                    assertTrue(returnedBillList.stream().anyMatch(bill -> "2".equals(bill.getBillId())));
                    assertTrue(returnedBillList.stream().anyMatch(bill -> "3".equals(bill.getBillId())));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateSpecificFieldsOfBill() throws Exception {

        BillRequestDTO updateRequest = BillRequestDTO.builder()
                .customerId("1")
                .visitType("New Visit Type")
                .vetId("New Vet ID")
                .date(null)
                .billStatus(BillStatus.UNPAID)
                .amount(200.0)
                .build();


        BillResponseDTO updatedResponse = BillResponseDTO.builder()
                .billId("1")
                .customerId("1")
                .visitType("New Visit Type")
                .vetId("New Vet ID")
                .date(null)
                .billStatus(BillStatus.PAID)
                .amount(200.0)
                .build();


        server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(updatedResponse))
                .addHeader("Content-Type", "application/json"));


        Mono<BillRequestDTO> monoUpdateRequest = Mono.just(updateRequest);


        Mono<BillResponseDTO> updatedBillResponseMono = billServiceClient.updateBill("1", monoUpdateRequest);


        StepVerifier.create(updatedBillResponseMono)
                .expectNext(updatedResponse)
                .verifyComplete();
    }


}