package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.datalayer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import static reactor.core.publisher.Mono.just;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class BillControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private BillRepository repo;

    @Test
    void getBillByValidBillID() {

        Bill billEntity = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/" + billEntity.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(billEntity.getVisitType())
                .jsonPath("$.customerId").isEqualTo(billEntity.getCustomerId())
                .jsonPath("$.amount").isEqualTo(billEntity.getAmount());
    }

    @Test
    void getAllBills() {

        Bill billEntity = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(Bill.class)
                .consumeWith(response -> {
                    List<Bill> bills = response.getResponseBody();
                    Assertions.assertNotNull(bills);
                });
    }

    @Test
    void getAllPaidBills() {

        Bill billEntity = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/paid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(Bill.class)
                .consumeWith(response -> {
                    List<Bill> bills = response.getResponseBody();
                    Assertions.assertNotNull(bills);
                });
    }

    @Test
    void getAllUnpaidBills() {
        // Send a GET request to /bills/unpaid and expect a JSON response
        Bill billEntity = buildUnpaidBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/unpaid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(Bill.class)
                .consumeWith(response -> {
                    List<Bill> bills = response.getResponseBody();
                    Assertions.assertNotNull(bills);
                });
    }

    @Test
    void getAllOverdueBills() {
        // Send a GET request to /bills/overdue and expect a JSON response
        Bill billEntity = buildOverdueBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/overdue")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(Bill.class)
                .consumeWith(response -> {
                    List<Bill> bills = response.getResponseBody();
                    Assertions.assertNotNull(bills);
                });
    }

    @Test
    void createBill() {

        Bill billEntity = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.post()                                                            // Create the object
                .uri("/bills")
                .body(just(billEntity), Bill.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        client.get()                                                            // Check if the item was created properly
                .uri("/bills/" + billEntity.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(billEntity.getVisitType())
                .jsonPath("$.customerId").isEqualTo(billEntity.getCustomerId())
                .jsonPath("$.amount").isEqualTo(billEntity.getAmount());
    }

    @Test
    void updateBill() {

        Bill billEntity = buildBill();
        Bill billEntity2 = buildBill();

        billEntity2.setVisitType("Different");
        billEntity2.setAmount(199239);
        billEntity2.setId("2");

        String BILL_ID_OK = billEntity.getBillId();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier.create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/" + BILL_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(billEntity.getVisitType())
                .jsonPath("$.customerId").isEqualTo(billEntity.getCustomerId())
                .jsonPath("$.amount").isEqualTo(billEntity.getAmount());

        client.put()
                .uri("/bills/" + BILL_ID_OK)
                .body(just(billEntity2), Bill.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(billEntity2.getVisitType());

        client.get()
                .uri("/bills/" + BILL_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(billEntity2.getVisitType())
                .jsonPath("$.customerId").isEqualTo(billEntity2.getCustomerId())
                .jsonPath("$.amount").isEqualTo(billEntity2.getAmount());

    }

    @Test
    void getBillByCustomerId() {

        Bill billEntity = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/customer/" + billEntity.getCustomerId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(Bill.class)
                .consumeWith(response -> {
                    List<Bill> bills = response.getResponseBody();
                    Assertions.assertNotNull(bills);
                });
    }

    @Test
    void getBillByVetId() {

        Bill billEntity = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/vet/" + billEntity.getVetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(Bill.class)
                .consumeWith(response -> {
                    List<Bill> bills = response.getResponseBody();
                    Assertions.assertNotNull(bills);
                });
    }

    @Test
    void deleteBillByBillId() {

        Bill billEntity = buildBill();

        repo.save(billEntity);

        Publisher<Void> setup = repo.deleteBillByBillId(billEntity.getBillId());

        StepVerifier.create(setup)
                .expectNextCount(0)
                .verifyComplete();

        client.delete()
                .uri("/bills/" + billEntity.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();


    }

    @Test
    void deleteAllBills() {
        Bill billEntity = buildBill();

        Publisher<Void> setup = repo.deleteAll().thenMany(repo.delete(billEntity));

        StepVerifier.create(setup)
                .expectNextCount(0)
                .verifyComplete();

        client.delete()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

    }

    @Test
    void deleteBillByVetId() {

        Bill billEntity = buildBill();

        repo.save(billEntity);

        Publisher<Void> setup = repo.deleteBillsByVetId(billEntity.getVetId());

        StepVerifier.create(setup)
                .expectNextCount(0)
                .verifyComplete();

        client.delete()
                .uri("/bills/vet/" + billEntity.getVetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();


    }

    @Test
    void deleteBillsByCustomerId() {
        Bill billEntity = buildBill();
        repo.save(billEntity);
        Publisher<Void> setup = repo.deleteBillsByCustomerId(billEntity.getCustomerId());

        StepVerifier.create(setup)
                .expectNextCount(0)
                .verifyComplete();

        client.delete()
                .uri("/bills/customer/" + billEntity.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()//.isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                .expectBody();
    }

    private Bill buildBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022,Month.OCTOBER,15);


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.PAID).dueDate(dueDate).build();
    }

    private Bill buildUnpaidBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 5);


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.UNPAID).dueDate(dueDate).build();
    }

    private Bill buildOverdueBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.OVERDUE).dueDate(dueDate).build();
    }
    @Test
    void whenValidPageAndSizeProvided_thenReturnsCorrectBillsPage() {
        repo.deleteAll().block();
        for (int i = 1; i <= 15; i++) {
            repo.save(Bill.builder()
                    .billId("BillUUID" + i)
                    .customerId("Cust" + i)
                    .vetId("1")
                    .visitType("Routine Check")
                    .date(LocalDate.now())
                    .amount(100.0)
                    .billStatus(BillStatus.PAID)
                    .dueDate(LocalDate.now().plusDays(30))
                    .build()).block();
        }

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills")
                        .queryParam("page", 1)
                        .queryParam("size", 5)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BillResponseDTO.class)
                .hasSize(5);
    }

    @Test
    void whenNonExistentPageRequested_thenReturnsEmptyPage() {
        repo.deleteAll().block();
        for (int i = 1; i <= 5; i++) {
            repo.save(Bill.builder()
                    .billId("BillUUID" + i)
                    .customerId("Cust" + i)
                    .vetId("1")
                    .visitType("Routine Check")
                    .date(LocalDate.now())
                    .amount(100.0)
                    .billStatus(BillStatus.PAID)
                    .dueDate(LocalDate.now().plusDays(30))
                    .build()).block();
        }

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills")
                        .queryParam("page", 10)
                        .queryParam("size", 5)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BillResponseDTO.class)
                .hasSize(0);
    }
    @Test
    void getBillWithTimeRemaining() {

        Bill billEntity = buildBillWithTimeRemaining();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/" + billEntity.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(billEntity.getVisitType())
                .jsonPath("$.customerId").isEqualTo(billEntity.getCustomerId())
                .jsonPath("$.amount").isEqualTo(billEntity.getAmount())
                .jsonPath("$.timeRemaining").isEqualTo(expectedTimeRemaining(billEntity));
    }

    @Test
    void getBillWithNegativeTimeRemaining() {

        Bill billEntity = buildBillWithPastDueDate();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/" + billEntity.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(billEntity.getVisitType())
                .jsonPath("$.customerId").isEqualTo(billEntity.getCustomerId())
                .jsonPath("$.amount").isEqualTo(billEntity.getAmount())
                .jsonPath("$.timeRemaining").isEqualTo(0);
    }

    @Test
    void getBillsByMonth() {
        repo.deleteAll().block();
        for (int i = 1; i <= 5; i++) {
            repo.save(Bill.builder()
                    .billId("BillUUID" + i)
                    .customerId("Cust" + i)
                    .vetId("1")
                    .visitType("Routine Check")
                    .date(LocalDate.of(2022, 9, i))
                    .amount(100.0)
                    .billStatus(BillStatus.PAID)
                    .dueDate(LocalDate.of(2022, 9, i).plusDays(30))
                    .build()).block();
        }

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills/month")
                        .queryParam("year", 2022)
                        .queryParam("month", 9)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BillResponseDTO.class)
                .hasSize(5);
    }

    private Bill buildBillWithPastDueDate() {

        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDate dueDate = LocalDate.now().minusDays(15);

        return Bill.builder()
                .id("Id")
                .billId(UUID.randomUUID().toString())
                .customerId("1")
                .vetId("1")
                .visitType("Test Type")
                .date(date)
                .amount(100.0)
                .billStatus(BillStatus.OVERDUE)
                .dueDate(dueDate)
                .build();
    }

    private Bill buildBillWithTimeRemaining() {

        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDate dueDate = LocalDate.now().plusDays(15);

        return Bill.builder()
                .id("Id")
                .billId(UUID.randomUUID().toString())
                .customerId("1")
                .vetId("1")
                .visitType("Test Type")
                .date(date)
                .amount(100.0)
                .billStatus(BillStatus.UNPAID)
                .dueDate(dueDate)
                .build();
    }

    private long expectedTimeRemaining(Bill billEntity) {
        if (billEntity.getDueDate().isBefore(LocalDate.now())) {
            return 0L;
        }
        return Duration.between(LocalDate.now().atStartOfDay(), billEntity.getDueDate().atStartOfDay()).toDays();
    }

    @Test
    void payBill_SuccessfulPayment() {

        Bill billEntity = buildUnpaidBillForPayment();
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        // Setup: Save an unpaid bill
        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        // Perform payment request
        client.post()
                .uri("/bills/customer/" + billEntity.getCustomerId() + "/bills/" + billEntity.getBillId() + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .body(just(paymentRequestDTO), PaymentRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertTrue(responseBody.contains("Payment successful!"));
                });

        // Verify the bill status is updated to PAID
        client.get()
                .uri("/bills/" + billEntity.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.billStatus").isEqualTo(BillStatus.PAID.toString());
    }

    @Test
    void payBill_InvalidPaymentDetails() {

        Bill billEntity = buildUnpaidBillForPayment();
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234", "12", "1223"); // Invalid card number and CVV

        // Setup: Save an unpaid bill
        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        // Perform payment request with invalid payment details
        client.post()
                .uri("/bills/customer/" + billEntity.getCustomerId() + "/bills/" + billEntity.getBillId() + "/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .body(just(paymentRequestDTO), PaymentRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertTrue(responseBody.contains("Invalid payment details"));
                });

        // Verify the bill status is still UNPAID
        client.get()
                .uri("/bills/" + billEntity.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.billStatus").isEqualTo(BillStatus.UNPAID.toString());
    }

    @Test
    void payBill_BillNotFound() {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        // Attempt to pay a non-existing bill
        client.post()
                .uri("/bills/customer/customerId/bills/nonExistingBillId/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .body(just(paymentRequestDTO), PaymentRequestDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    Assertions.assertNotNull(responseBody);
                    Assertions.assertTrue(responseBody.contains("Bill not found"));
                });
    }

    // Helper methods to build a Bill entity for testing payment
    private Bill buildUnpaidBillForPayment() {
        LocalDate date = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate dueDate = LocalDate.of(2024, Month.JANUARY, 30);

        return Bill.builder()
                .id(UUID.randomUUID().toString())
                .billId(UUID.randomUUID().toString())
                .customerId("1")
                .vetId("1")
                .visitType("Routine Check")
                .date(date)
                .amount(150.0)
                .billStatus(BillStatus.UNPAID)
                .dueDate(dueDate)
                .build();
    }
}