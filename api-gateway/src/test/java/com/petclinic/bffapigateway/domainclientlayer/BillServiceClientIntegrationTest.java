package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Bills.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
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
import java.nio.charset.StandardCharsets;
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

    private BillResponseDTO bill(String id, String customerId, BillStatus status) {
        BillResponseDTO dto = new BillResponseDTO();
        dto.setBillId(id);
        dto.setCustomerId(customerId);
        dto.setBillStatus(status); // enum
        return dto;
    }

    private byte[] fakePdf() {
        String pdf = "%PDF-1.4\n1 0 obj\n<<>>\nendobj\nxref\n0 1\n0000000000 65535 f \ntrailer\n<<>>\nstartxref\n0\n%%EOF";
        return pdf.getBytes(StandardCharsets.UTF_8);
    }

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

        BillRequestDTO billRequest = new BillRequestDTO();
        billRequest.setVetId("1");
        billRequest.setCustomerId("1");
        billRequest.setDate(null);
        billRequest.setBillStatus(BillStatus.PAID);
        billRequest.setDueDate(null);
        billRequest.setAmount(new BigDecimal("100.0"));
        billRequest.setVisitType("Check up");

        String requestJson = mapper.writeValueAsString(billRequest);

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(requestJson)
        );

        Mono<BillResponseDTO> createdBillMono = billServiceClient.createBill(billRequest);

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

    @Test
    void archiveBill_Success() throws Exception {
        List<BillResponseDTO> archivedBills = Arrays.asList(
                BillResponseDTO.builder()
                        .billId("1")
                        .amount(new BigDecimal(100.0))
                        .taxedAmount(new BigDecimal(115.0))
                        .customerId("1")
                        .vetId("1")
                        .visitType("Check up")
                        .billStatus(BillStatus.PAID)
                        .archive(false)
                        .build(),
                BillResponseDTO.builder()
                        .billId("2")
                        .amount(new BigDecimal(200.0))
                        .taxedAmount(new BigDecimal(230.0))
                        .customerId("2")
                        .vetId("2")
                        .visitType("Surgery")
                        .billStatus(BillStatus.PAID)
                        .archive(false)
                        .build()
        );

        String responseBody = mapper.writeValueAsString(archivedBills);

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseBody)
        );

        Flux<BillResponseDTO> result = billServiceClient.archiveBill();

        StepVerifier.create(result.collectList())
                .expectNextMatches(bills -> {
                    assertEquals(2, bills.size());
                    assertTrue(bills.stream().anyMatch(bill -> "1".equals(bill.getBillId())));
                    assertTrue(bills.stream().anyMatch(bill -> "2".equals(bill.getBillId())));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void archiveBill_FailsForUnpaidOrOverdueBills() throws Exception {
        List<BillResponseDTO> bills = Arrays.asList(
                BillResponseDTO.builder()
                        .billId("1")
                        .amount(new BigDecimal(100.0))
                        .taxedAmount(new BigDecimal(115.0))
                        .customerId("1")
                        .vetId("1")
                        .visitType("Check up")
                        .billStatus(BillStatus.UNPAID)
                        .archive(false)
                        .build(),
                BillResponseDTO.builder()
                        .billId("2")
                        .amount(new BigDecimal(200.0))
                        .taxedAmount(new BigDecimal(230.0))
                        .customerId("2")
                        .vetId("2")
                        .visitType("Surgery")
                        .billStatus(BillStatus.OVERDUE)
                        .archive(false)
                        .build()
        );

        String responseBody = mapper.writeValueAsString(bills);

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseBody)
        );

        Flux<BillResponseDTO> result = billServiceClient.archiveBill();

        StepVerifier.create(result.collectList())
                .expectNextMatches(returnedBills -> {
                    assertEquals(2, returnedBills.size());
                    assertTrue(returnedBills.stream().allMatch(bill -> !bill.getArchive())); // Ensure no bills are archived
                    return true;
                })
                .verifyComplete();
    }


    void deleteBill_WhenBillIsUnpaid_ShouldReturn422_AndNotInvokeDelete() throws Exception {
        String billId = "B-123";

        BillResponseDTO bill = BillResponseDTO.builder()
                .billId(billId)
                .customerId("C-1")
                .billStatus(BillStatus.UNPAID) // triggers the if-guard
                .amount(new BigDecimal("100.00"))
                .taxedAmount(new BigDecimal("115.00"))
                .build();

        String body = new com.fasterxml.jackson.databind.ObjectMapper()
                .findAndRegisterModules()
                .writeValueAsString(bill);

        server.enqueue(new okhttp3.mockwebserver.MockResponse()
                .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        reactor.core.publisher.Mono<Void> result = billServiceClient.deleteBill(billId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof org.springframework.web.server.ResponseStatusException);
                    org.springframework.web.server.ResponseStatusException rse =
                            (org.springframework.web.server.ResponseStatusException) ex;

                    assertEquals("Cannot delete a bill that is unpaid or overdue.", rse.getReason());
                })
                .verify();

        okhttp3.mockwebserver.RecordedRequest first = server.takeRequest(1, java.util.concurrent.TimeUnit.SECONDS);
        assertNotNull(first);
        assertEquals("GET", first.getMethod());
        assertTrue(first.getPath().endsWith("/" + billId));
        assertEquals(1, server.getRequestCount());
    }

    @Test
    void getBillsByCustomerIdPaginated_Positive_ReturnsFlux() throws Exception {
        String customerId = "C-1";
        List<BillResponseDTO> payload = Arrays.asList(
                bill("B-1", customerId, BillStatus.PAID),
                bill("B-2", customerId, BillStatus.PAID)
        );
        // Build JSON *before* the lambda to avoid checked exception in the lambda
        String bodyJson = mapper.writeValueAsString(payload);

        prepareResponse(r -> r
                .setResponseCode(200)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody(bodyJson)
        );

        Flux<BillResponseDTO> result = billServiceClient.getBillsByCustomerIdPaginated(
                customerId, Optional.of(0), Optional.of(2));

        StepVerifier.create(result)
                .expectNextMatches(b ->
                        "B-1".equals(b.getBillId()) &&
                                "C-1".equals(b.getCustomerId()) &&
                                b.getBillStatus() == BillStatus.PAID)
                .expectNextMatches(b ->
                        "B-2".equals(b.getBillId()) &&
                                "C-1".equals(b.getCustomerId()) &&
                                b.getBillStatus() == BillStatus.PAID)
                .verifyComplete();

        RecordedRequest req = server.takeRequest();
        assertTrue(req.getPath().contains("/customer/" + customerId + "/paginated"));
        assertTrue(req.getPath().contains("page=0"));
        assertTrue(req.getPath().contains("size=2"));
        assertEquals("GET", req.getMethod());
    }

    @Test
    void getBillsByCustomerIdPaginated_Negative_PropagatesError() {
        prepareResponse(r -> r
                .setResponseCode(500)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Internal error\"}")
        );

        Flux<BillResponseDTO> result = billServiceClient.getBillsByCustomerIdPaginated(
                "C-1", Optional.of(1), Optional.of(5));

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof WebClientResponseException);
                    WebClientResponseException w = (WebClientResponseException) ex;
                    assertEquals(500, w.getRawStatusCode());
                })
                .verify();
    }

    @Test
    void downloadBillPdf_Negative_NotFound() {
        prepareResponse(r -> r
                .setResponseCode(404)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Bill not found\"}")
        );

        Mono<byte[]> result = billServiceClient.downloadBillPdf("C-404", "B-missing");

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof WebClientResponseException);
                    WebClientResponseException w = (WebClientResponseException) ex;
                    assertEquals(404, w.getRawStatusCode());
                })
                .verify();
    }

    @Test
    void downloadBillPdf_Positive_ReturnsBytes() throws Exception {
        String customerId = "C-1";
        String billId = "B-99";
        byte[] pdf = fakePdf();

        prepareResponse(r -> r
                .setResponseCode(200)
                .addHeader("Content-Type", MediaType.APPLICATION_PDF_VALUE)
                .setBody(new Buffer().write(pdf))
        );

        Mono<byte[]> result = billServiceClient.downloadBillPdf(customerId, billId);

        StepVerifier.create(result)
                .assertNext(bytes -> {
                    assertNotNull(bytes);
                    assertArrayEquals(pdf, bytes);
                })
                .verifyComplete();

        RecordedRequest req = server.takeRequest(); // <-- throws InterruptedException
        assertTrue(req.getPath().contains("/customer/" + customerId + "/bills/" + billId + "/pdf"));
        assertEquals("GET", req.getMethod());
        String acceptHeader = req.getHeader("Accept");
        assertNotNull(acceptHeader);
        assertTrue(acceptHeader.contains("application/pdf"));
    }

    @Test
    void getCurrentBalance_Positive_ReturnsBalance() throws Exception {
        String customerId = "C-123";
        Double balance = 250.75;

        String bodyJson = mapper.writeValueAsString(balance);

        prepareResponse(r -> r
                .setResponseCode(200)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody(bodyJson)
        );

        Mono<Double> result = billServiceClient.getCurrentBalance(customerId);

        StepVerifier.create(result)
                .expectNext(balance)
                .verifyComplete();

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertTrue(req.getPath().contains("/customer/" + customerId + "/bills/current-balance"));
    }

    @Test
    void getCurrentBalance_Negative_NotFound() {
        String customerId = "C-404";

        prepareResponse(r -> r
                .setResponseCode(404)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Customer not found\"}")
        );

        Mono<Double> result = billServiceClient.getCurrentBalance(customerId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof WebClientResponseException);
                    WebClientResponseException w = (WebClientResponseException) ex;
                    assertEquals(404, w.getRawStatusCode());
                })
                .verify();
    }

    @Test
    void getTotalNumberOfBills_Positive_ReturnsCount() throws Exception {
        // Arrange
        long totalBills = 100L;
        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(String.valueOf(totalBills)));

        // Act
        Mono<Long> result = billServiceClient.getTotalNumberOfBills();

        // Assert
        StepVerifier.create(result)
                .expectNext(totalBills)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().endsWith("/bills-count"));
    }

    @Test
    void getTotalNumberOfBillsWithFilters_Positive_ReturnsFilteredCount() throws Exception {
        // Arrange
        long filteredBills = 50L;
        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(String.valueOf(filteredBills)));

        // Act
        Mono<Long> result = billServiceClient.getTotalNumberOfBillsWithFilters(
                "1", "123", "John", "Doe", "Checkup", "456", "Jane", "Smith");

        // Assert
        StepVerifier.create(result)
                .expectNext(filteredBills)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/bills-filtered-count"));
        assertTrue(request.getPath().contains("billId=1"));
        assertTrue(request.getPath().contains("customerId=123"));
        assertTrue(request.getPath().contains("ownerFirstName=John"));
        assertTrue(request.getPath().contains("ownerLastName=Doe"));
        assertTrue(request.getPath().contains("visitType=Checkup"));
        assertTrue(request.getPath().contains("vetId=456"));
        assertTrue(request.getPath().contains("vetFirstName=Jane"));
        assertTrue(request.getPath().contains("vetLastName=Smith"));
    }

    @Test
    void getTotalNumberOfBillsWithFilters_MissingFilters_ReturnsFilteredCount() throws Exception {
        // Arrange
        long filteredBills = 30L;
        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(String.valueOf(filteredBills)));

        // Act
        Mono<Long> result = billServiceClient.getTotalNumberOfBillsWithFilters(
                null, "123", null, "Doe", null, null, null, null);

        // Assert
        StepVerifier.create(result)
                .expectNext(filteredBills)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/bills-filtered-count"));
        assertTrue(request.getPath().contains("customerId=123"));
        assertTrue(request.getPath().contains("ownerLastName=Doe"));
        assertFalse(request.getPath().contains("billId="));
        assertFalse(request.getPath().contains("visitType="));
    }

    @Test
    void getInterest_Positive_ReturnsInterest() throws Exception {
        // Arrange
        String billId = "B-123";
        Double interest = 15.75;

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(String.valueOf(interest)));

        // Act
        Mono<Double> result = billServiceClient.getInterest(billId);

        // Assert
        StepVerifier.create(result)
                .expectNext(interest)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/" + billId + "/interest"));
    }

    @Test
    void getTotalWithInterest_Positive_ReturnsTotal() throws Exception {
        // Arrange
        String billId = "B-123";
        Double totalWithInterest = 115.75;

        prepareResponse(response -> response
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(String.valueOf(totalWithInterest)));

        // Act
        Mono<Double> result = billServiceClient.getTotalWithInterest(billId);

        // Assert
        StepVerifier.create(result)
                .expectNext(totalWithInterest)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/" + billId + "/total"));
    }

    @Test
    void setInterestExempt_Positive_SetsExemption() throws Exception {
        // Arrange
        String billId = "B-123";
        boolean exempt = true;

        prepareResponse(response -> response
                .setResponseCode(204)); // No Content

        // Act
        Mono<Void> result = billServiceClient.setInterestExempt(billId, exempt);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("PATCH", request.getMethod());
        assertTrue(request.getPath().contains("/" + billId + "/exempt-interest"));
        assertTrue(request.getPath().contains("exempt=true"));
    }
}
