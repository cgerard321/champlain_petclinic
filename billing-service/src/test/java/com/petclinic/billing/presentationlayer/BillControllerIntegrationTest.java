package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.dataaccesslayer.*;
import com.petclinic.billing.domainclientlayer.CustomerServiceClient;
import com.petclinic.billing.domainclientlayer.models.CustomerResponseModel;
import com.petclinic.billing.domainclientlayer.VetServiceClient;
import com.petclinic.billing.presentationlayer.models.BillRequestModel;
import com.petclinic.billing.presentationlayer.models.BillResponseModel;
import com.petclinic.billing.domainclientlayer.models.VetResponseModel;
import com.petclinic.billing.util.InterestCalculationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.just;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
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

    @MockBean
    private VetServiceClient vetServiceClient;

    @MockBean
    private CustomerServiceClient customerServiceClient;

    @BeforeEach
    void setup() {
        repo.deleteAll().block();
    }

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
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
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
    void createBill_ShouldReturnCreatedBillWithVetAndOwner() {
        // Arrange
        BillRequestModel billRequest = new BillRequestModel();
        billRequest.setBillStatus(BillStatus.PAID);
        billRequest.setVetId("vet-1");
        billRequest.setCustomerId("cust-1");
        billRequest.setVisitType("Checkup");
        billRequest.setAmount(new BigDecimal("100.00"));
        billRequest.setDueDate(LocalDate.now().plusDays(10));

        // Mock Vet + Owner service responses
        VetResponseModel vet = new VetResponseModel();
        vet.setFirstName("John");
        vet.setLastName("Doe");

        CustomerResponseModel customer = new CustomerResponseModel();
        customer.setFirstName("Alice");
        customer.setLastName("Smith");

        when(vetServiceClient.getVetByVetId("vet-1")).thenReturn(Mono.just(vet));
        when(customerServiceClient.getCustomerByCustomerId("cust-1")).thenReturn(Mono.just(customer));

        String testJwtToken = "test-jwt-token";

        // Act
        client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills")
                        .queryParam("sendEmail", false)
                        .queryParam("currency", "USD")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", testJwtToken)
                .body(Mono.just(billRequest), BillRequestModel.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.billId").isNotEmpty()
                .jsonPath("$.vetFirstName").isEqualTo("John")
                .jsonPath("$.vetLastName").isEqualTo("Doe")
                .jsonPath("$.customerFirstName").isEqualTo("Alice")
                .jsonPath("$.customerLastName").isEqualTo("Smith")
                .jsonPath("$.billStatus").isEqualTo("PAID")
                .jsonPath("$.amount").isEqualTo(100.00);

        // Verify mock interactions
        verify(vetServiceClient).getVetByVetId("vet-1");
        verify(customerServiceClient).getCustomerByCustomerId("cust-1");
    }

    @Test
    void updateBill() {

        Bill billEntity = buildBill();
        Bill billEntity2 = buildBill();

        billEntity2.setVisitType("Different");
        billEntity2.setAmount(new BigDecimal(199239));
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
        billEntity.setCustomerFirstName("John");
        billEntity.setCustomerLastName("Doe");

        // Mock the OwnerClient call
        CustomerResponseModel customer = new CustomerResponseModel();
        customer.setCustomerId(billEntity.getCustomerId());
        customer.setFirstName("John");
        customer.setLastName("Doe");

        when(customerServiceClient.getCustomerByCustomerId(billEntity.getCustomerId()))
                .thenReturn(Mono.just(customer));

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

        StepVerifier.create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/customer/" + billEntity.getCustomerId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(Bill.class)
                .consumeWith(response -> {
                    List<Bill> bills = response.getResponseBody();
                    Assertions.assertNotNull(bills);
                    Assertions.assertFalse(bills.isEmpty());
                    Assertions.assertEquals(billEntity.getCustomerId(), bills.get(0).getCustomerId());
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
                .expectStatus().isNoContent()
                .expectBody();
    }

    private Bill buildBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022,Month.OCTOBER,15);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.PAID).dueDate(dueDate).build();
    }

    private Bill buildUnpaidBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 5);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.UNPAID).dueDate(dueDate).build();
    }

    private Bill buildOverdueBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.OVERDUE).dueDate(dueDate).build();
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
                    .amount(new BigDecimal(100.0))
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
                .expectBodyList(BillResponseModel.class)
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
                    .amount(new BigDecimal(100.0))
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
                .expectBodyList(BillResponseModel.class)
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
                    .amount(new BigDecimal(100.0))
                    .billStatus(BillStatus.PAID)
                    .dueDate(LocalDate.of(2022, 9, i).plusDays(30))
                    .build()).block();
        }

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills/month")
                        .queryParam("year", 2022)
                        .queryParam("month", 9)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(BillResponseModel.class)
                .hasSize(5);
    }

    @Test
    void getBillsByOwnerName() {
        Bill billEntity = buildBill();

        repo.save(billEntity).block();

        Publisher<BillResponseModel> setup = repo.findByBillId(billEntity.getBillId())
                .map(bill -> BillResponseModel.builder()
                        .billId(bill.getBillId())
                        .customerId(bill.getCustomerId())
                        .vetId(bill.getVetId())
                        .visitType(bill.getVisitType())
                        .date(bill.getDate())
                        .amount(bill.getAmount())
                        .billStatus(bill.getBillStatus())
                        .dueDate(bill.getDueDate())
                        .customerFirstName("John")
                        .customerLastName("Doe")
                        .vetFirstName("Jane")
                        .vetLastName("Smith")
                        .build());
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getBillsByVetName() {
        Bill billEntity = Bill.builder()
                .billId("BillUUID1")
                .customerId("Cust1")
                .vetId("1")
                .visitType("Routine Check")
                .date(LocalDate.of(2022, 9, 1))
                .amount(new BigDecimal(100.0))
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2022, 9, 1).plusDays(30))
                .vetFirstName("VetFirstName")
                .vetLastName("VetLastName")
                .build();

        repo.save(billEntity).block();

        Publisher<BillResponseModel> setup = repo.findByBillId(billEntity.getBillId())
                .map(bill -> BillResponseModel.builder()
                        .billId(bill.getBillId())
                        .customerId(bill.getCustomerId())
                        .vetId(bill.getVetId())
                        .visitType(bill.getVisitType())
                        .date(bill.getDate())
                        .amount(bill.getAmount())
                        .billStatus(bill.getBillStatus())
                        .dueDate(bill.getDueDate())
                        .vetFirstName("VetFirstName")
                        .vetLastName("VetLastName")
                        .build());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getBillsByVisitType() {
        Bill billEntity = Bill.builder()
                .billId("BillUUID1")
                .customerId("Cust1")
                .vetId("1")
                .visitType("Routine Check")
                .date(LocalDate.of(2022, 9, 1))
                .amount(new BigDecimal(100.0))
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2022, 9, 1).plusDays(30))
                .build();

        repo.save(billEntity).block();

        Publisher<BillResponseModel> setup = repo.findByBillId(billEntity.getBillId())
                .map(bill -> BillResponseModel.builder()
                        .billId(bill.getBillId())
                        .customerId(bill.getCustomerId())
                        .vetId(bill.getVetId())
                        .visitType(bill.getVisitType())
                        .date(bill.getDate())
                        .amount(bill.getAmount())
                        .billStatus(bill.getBillStatus())
                        .dueDate(bill.getDueDate())
                        .build());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void whenGetAllBillsByPageAndPageSizeIsInvalid__thenReturnsBadRequest() {
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills")
                        .queryParam("page", -1)
                        .queryParam("size", 0)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
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
                .amount(new BigDecimal(100.0))
                .billStatus(BillStatus.OVERDUE)
                .dueDate(dueDate)
                .isArchived(false)
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
                .amount(new BigDecimal(100.0))
                .billStatus(BillStatus.UNPAID)
                .dueDate(dueDate)
                .isArchived(false)
                .build();
    }

    private long expectedTimeRemaining(Bill billEntity) {
        if (billEntity.getDueDate().isBefore(LocalDate.now())) {
            return 0L;
        }
        return Duration.between(LocalDate.now().atStartOfDay(), billEntity.getDueDate().atStartOfDay()).toDays();
    }

        @Test
        void getBillByValidBillID_Overdue_ShouldReturnInterest() {
                Bill billEntity = buildOverdueBill();

                Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(billEntity));

                StepVerifier.create(setup)
                        .expectNextCount(1)
                         .verifyComplete();

                // Use centralized utility for compound interest calculation
                BigDecimal expectedInterest = InterestCalculationUtil.calculateCompoundInterest(
                    billEntity.getAmount(), billEntity.getDueDate(), LocalDate.now());                client.get()
                        .uri("/bills/" + billEntity.getBillId())
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody()
                        .jsonPath("$.interest").isEqualTo(expectedInterest);
}
}