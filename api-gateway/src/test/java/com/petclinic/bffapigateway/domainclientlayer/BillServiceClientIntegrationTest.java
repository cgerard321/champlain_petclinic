package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Bills.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;


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
            .amount(new BigDecimal("100.0"))
            .taxedAmount(new BigDecimal("115.0"))
            .customerId("1")
            .vetId("1")
            .visitType("Check up")
            .date( null)
            .billStatus(BillStatus.PAID)
            .dueDate(null)
            .build();

    private final BillResponseDTO billResponseDTO2 = BillResponseDTO.builder()
            .billId("2")
            .amount(new BigDecimal("150.0"))
            .taxedAmount(new BigDecimal("172.5"))
            .customerId("2")
            .vetId("2")
            .visitType("Check up")
            .date(null)
            .billStatus(BillStatus.UNPAID)
            .dueDate(null)
            .build();

    private final BillResponseDTO billResponseDTO3 = BillResponseDTO.builder()
            .billId("3")
            .amount(new BigDecimal("250.0"))
            .taxedAmount(new BigDecimal("287.5"))
            .customerId("3")
            .vetId("3")
            .visitType("Check up")
            .date(null)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(null)
            .build();

    @Test
    void getBillById() throws Exception {
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(billResponseDTO)).addHeader("Content-Type", "application/json"));

        Mono<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillById("1");
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
    void getBillsByOwnerName() throws Exception {
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(billResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillsByOwnerName("Joe", "Nuts");
        StepVerifier.create(billResponseDTOMono)
                .expectNextMatches(returnedBillResponseDTO1 -> returnedBillResponseDTO1.getCustomerId().equals("1"))
                .verifyComplete();
    }

    @Test
    void getBillsByVetName() throws Exception{
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(billResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillsByVetName("Joe", "Nuts");
        StepVerifier.create(billResponseDTOMono)
                .expectNextMatches(returnedBillResponseDTO1 -> returnedBillResponseDTO1.getCustomerId().equals("1"))
                .verifyComplete();
    }

    @Test
    void getBillsByVisitType() throws Exception{
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(mapper.writeValueAsString(billResponseDTO)).addHeader("Content-Type", "application/json"));

        Flux<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillsByVisitType("Regular");
        StepVerifier.create(billResponseDTOMono)
                .expectNextMatches(returnedBillResponseDTO1 -> returnedBillResponseDTO1.getCustomerId().equals("1"))
                .verifyComplete();
    }

//    @Test
//    void shouldDeleteBill() throws JsonProcessingException {
//        final BillDetails bill = BillDetails.builder()
//                .billId(UUID.randomUUID().toString())
//                .vetId("15")
//                .customerId("2")
//                .date(null)
//                .billStatus(BillStatus.PAID)
//                .dueDate(null)
//                .amount(100)
//                .visitType("Check")
//                .build();
//
//        final String body = mapper.writeValueAsString(mapper.convertValue(bill, BillDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final Mono<Void> empty = billServiceClient.deleteBill(bill.getBillId());
//
//        assertNull(empty.block());
//    }

    @Test
    void shouldDeleteBillByVetId() throws JsonProcessingException {
        final BillDetails bill = BillDetails.builder()
                .billId(UUID.randomUUID().toString())
                .vetId("15")
                .customerId("2")
                .date(null)
                .billStatus(BillStatus.UNPAID)
                .dueDate(null)
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
                .dueDate(null)
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
        billRequest.setDueDate(null);
        billRequest.setAmount(new BigDecimal("100.0"));
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
                    assertEquals(0, new BigDecimal("100.0").compareTo(createdBill.getAmount()));
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

        Flux<BillResponseDTO> billResponseFlux = billServiceClient.getAllBills();

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
    void getAllPaidBills() throws JsonProcessingException {
        // Prepare a list of bill responses as if they were returned from the service
        List<BillResponseDTO> billResponseList = Arrays.asList(
                billResponseDTO
        );

        final String body = mapper.writeValueAsString(billResponseList);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Flux<BillResponseDTO> billResponseFlux = billServiceClient.getAllPaidBills();

        StepVerifier.create(billResponseFlux.collectList())
                .expectNextMatches(returnedBillList -> {
                    assertEquals(1, returnedBillList.size());
                    assertTrue(returnedBillList.stream().anyMatch(bill -> "1".equals(bill.getBillId())));

                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getAllUnpaidBills() throws JsonProcessingException {
        List<BillResponseDTO> billResponseList = Arrays.asList(
                billResponseDTO2
        );

        final String body = mapper.writeValueAsString(billResponseList);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Flux<BillResponseDTO> billResponseFlux = billServiceClient.getAllUnpaidBills();

        StepVerifier.create(billResponseFlux.collectList())
                .expectNextMatches(returnedBillList -> {
                    assertEquals(1, returnedBillList.size());
                    assertTrue(returnedBillList.stream().anyMatch(bill -> "2".equals(bill.getBillId())));

                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getAllOverdueBills() throws JsonProcessingException {
        List<BillResponseDTO> billResponseList = Arrays.asList(billResponseDTO3);
        final String body = mapper.writeValueAsString(billResponseList);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Flux<BillResponseDTO> billResponseFlux = billServiceClient.getAllOverdueBills();

        StepVerifier.create(billResponseFlux.collectList())
                .expectNextMatches(returnedBillList -> {
                    assertEquals(1, returnedBillList.size());
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
                .dueDate(null)
                .amount(new BigDecimal("200.0"))
                .build();


        BillResponseDTO updatedResponse = BillResponseDTO.builder()
                .billId("1")
                .customerId("1")
                .visitType("New Visit Type")
                .vetId("New Vet ID")
                .date(null)
                .billStatus(BillStatus.PAID)
                .dueDate(null)
                .amount(new BigDecimal("200.0"))
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

    @Test
    void deleteAllBills() throws JsonProcessingException {

        final BillDetails bill = BillDetails.builder()
                .billId(UUID.randomUUID().toString())
                .vetId("15")
                .customerId("2")
                .date(null)
                .amount(100)
                .visitType("Check")
                .build();

        final String body = mapper.writeValueAsString(mapper.convertValue(bill, BillDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final Mono<Void> empty = billServiceClient.deleteAllBills();

        StepVerifier.create(empty)
                .expectComplete()
                .verify();

    }

    @Test
    void getNonExistentBillById() {
        server.enqueue(new MockResponse().setResponseCode(404));

        Mono<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillById("nonexistentId");

        StepVerifier.create(billResponseDTOMono)
                .expectError(WebClientResponseException.NotFound.class)
                .verify();
    }

    @Test
    void getBillByInvalidVetId() {
        server.enqueue(new MockResponse().setResponseCode(400));

        Flux<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillsByVetId("invalidVetId");

        StepVerifier.create(billResponseDTOMono)
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
    }

    @Test
    void getBillsByInvalidCustomerId() {
        server.enqueue(new MockResponse().setResponseCode(400));

        Flux<BillResponseDTO> billResponseDTOMono = billServiceClient.getBillsByOwnerId("invalidCustomerId");

        StepVerifier.create(billResponseDTOMono)
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
    }

    @Test
    void deleteNonExistentBill() {
        server.enqueue(new MockResponse().setResponseCode(404));

        Mono<Void> empty = billServiceClient.deleteBill("nonexistentId");

        StepVerifier.create(empty)
                .expectError(WebClientResponseException.NotFound.class)
                .verify();
    }

    @Test
    void deleteBillWithInvalidVetId() {
        server.enqueue(new MockResponse().setResponseCode(400));

        Flux<Void> empty = billServiceClient.deleteBillsByVetId("invalidVetId");

        StepVerifier.create(empty)
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
    }

    @Test
    void deleteBillWithInvalidCustomerId() {
        server.enqueue(new MockResponse().setResponseCode(400));

        Flux<Void> empty = billServiceClient.deleteBillsByCustomerId("invalidCustomerId");

        StepVerifier.create(empty)
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
    }

    @Test
    void createBillWithInvalidRequest() {
        BillRequestDTO invalidRequest = new BillRequestDTO();
        String requestJson = "";

        prepareResponse(response -> response
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(requestJson)
        );

        Mono<BillResponseDTO> createdBillMono = billServiceClient.createBill(invalidRequest);

        StepVerifier.create(createdBillMono)
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
    }

    @Test
    void whenGetAllBillsByPageWithValidParameters_thenReturnsPaginatedResults() throws Exception {
        List<BillResponseDTO> billResponses = Arrays.asList(billResponseDTO, billResponseDTO2);
        String jsonResponse = mapper.writeValueAsString(billResponses);

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonResponse));

        Flux<BillResponseDTO> resultFlux = billServiceClient.getAllBillsByPage(Optional.of(0), Optional.of(2),
                null, null, null, null, null, null, null,
                null);

        StepVerifier.create(resultFlux)
                .expectNextMatches(bill -> "1".equals(bill.getBillId()) && bill.getAmount().compareTo(new BigDecimal("100.0")) == 0)
                .expectNextMatches(bill -> "2".equals(bill.getBillId()) && bill.getAmount().compareTo(new BigDecimal("150.0")) == 0)
                .verifyComplete();
    }

    @Test
    void whenGetAllBillsByPageWithInvalidParameters_thenThrowsError() {
        prepareResponse(response -> response
                .setResponseCode(400)  // Bad Request
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Flux<BillResponseDTO> resultFlux = billServiceClient.getAllBillsByPage(Optional.of(-1),
                Optional.of(10), null, null, null, null, null,
                null, null, null);

        StepVerifier.create(resultFlux)
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
    }

    @Test
    void whenGetBillsByMonth_thenReturnsResults() throws Exception {
        List<BillResponseDTO> billResponses = Arrays.asList(billResponseDTO, billResponseDTO2);
        String jsonResponse = mapper.writeValueAsString(billResponses);

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonResponse));

        Flux<BillResponseDTO> resultFlux = billServiceClient.getBillsByMonth(1, 2022);

        StepVerifier.create(resultFlux)
                .expectNextMatches(bill -> "1".equals(bill.getBillId()) && bill.getAmount().compareTo(new BigDecimal("100.0")) == 0)
                .expectNextMatches(bill -> "2".equals(bill.getBillId()) && bill.getAmount().compareTo(new BigDecimal("150.0")) == 0)
                .verifyComplete();
    }

    @Test
    void payBill_Success() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        BillResponseDTO mockResponse = new BillResponseDTO();
        mockResponse.setBillId("1");
        mockResponse.setBillStatus(BillStatus.PAID);

        // Serialize before the lambda
        String body = new ObjectMapper().writeValueAsString(mockResponse);

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body)
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectNextMatches(bill -> bill.getBillId().equals("1") && bill.getBillStatus() == BillStatus.PAID)
                .verifyComplete();
    }



    @Test
    void payBill_Failure() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        prepareResponse(response -> response
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Payment failed\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 400)
                .verify();
    }

    @Test
    void payBill_InvalidCardNumber() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("123", "123", "12/23");

        prepareResponse(response -> response
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Invalid card number\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 400)
                .verify();
    }

    @Test
    void payBill_InvalidExpiryDate() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "01/15");

        prepareResponse(response -> response
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Invalid expiry date\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 400)
                .verify();
    }

    @Test
    void payBill_ExpiredCard() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "01/20");

        prepareResponse(response -> response
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Card expired\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 400)
                .verify();
    }

    @Test
    void payBill_InvalidCVV() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "12", "12/23");

        prepareResponse(response -> response
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Invalid CVV\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 400)
                .verify();
    }

    @Test
    void payBill_NonExistentBill() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        prepareResponse(response -> response
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Bill not found\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "nonexistent-bill-id", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void payBill_NetworkTimeout() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        prepareResponse(response -> response
                .setResponseCode(504)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Payment service timeout\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("1", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 504)
                .verify();
    }

    @Test
    void payBill_InvalidCustomerId() throws Exception {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        prepareResponse(response -> response
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\": \"Invalid customer ID\"}")
        );

        Mono<BillResponseDTO> resultMono = billServiceClient.payBill("invalid-customer-id", "1", paymentRequestDTO);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().value() == 400)
                .verify();
    }

}