package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.datalayer.*;

import com.petclinic.billing.domainclientlayer.OwnerClient;
import com.petclinic.billing.domainclientlayer.Auth.AuthServiceClient;
import com.petclinic.billing.domainclientlayer.Auth.UserDetails;
import com.petclinic.billing.domainclientlayer.Mailing.Mail;
import com.petclinic.billing.domainclientlayer.Mailing.MailService;
import com.petclinic.billing.util.InterestCalculationUtil;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CustomerBillsControllerIntegrationTest {

        @MockBean
        private AuthServiceClient authClient;

        @MockBean
        private MailService mailService;

        @Autowired
        private WebTestClient client;

        @MockBean
        private OwnerClient ownerClient;


        @Autowired
        private BillRepository billRepository;

    @BeforeEach
    void setup() {
        // Fake user details
        UserDetails fakeUser = new UserDetails();
        fakeUser.setUserId("cust-123");
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("fakeUser@example.com");

        // Mock the AuthClient reactive call
        when(authClient.getUserById(anyString(), anyString()))
                .thenReturn(Mono.just(fakeUser));

        // Mock the MailService to avoid sending real emails
        when(mailService.sendMail(any(Mail.class)))
                .thenReturn("Mail sent successfully");
    }


    @Test
    void getBillsByCustomerId_shouldSucceed() {
        Bill bill = buildBill();
        bill.setOwnerFirstName("John");   // <-- set this
        bill.setOwnerLastName("Doe");     // <-- set this

        Publisher<Bill> setup = billRepository.deleteAll().thenMany(billRepository.save(bill));

        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId(bill.getCustomerId());
        owner.setFirstName("John");
        owner.setLastName("Doe");

        when(ownerClient.getOwnerByOwnerId(bill.getCustomerId()))
                .thenReturn(Mono.just(owner));

        StepVerifier.create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bills/customer/{customerId}/bills", bill.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo(bill.getCustomerId());
    }


    @Test
        void testDownloadBillPdf() {

                Bill bill = buildBill();

                Mono<Void> setup = billRepository.deleteAll()
                                .then(billRepository.save(bill))
                                .then();

                StepVerifier.create(setup)
                                .verifyComplete();

                client.get()
                                .uri("/bills/customer/{customerId}/bills/{billId}/pdf", bill.getCustomerId(),
                                                bill.getBillId())
                                .accept(MediaType.APPLICATION_PDF)
                                .exchange()
                                .expectStatus().isOk()
                                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                                .expectBody(byte[].class)
                                .consumeWith(response -> {
                                        byte[] pdf = response.getResponseBody();
                                        assertNotNull(pdf);
                                        assertTrue(pdf.length > 0);
                                });
        }

        @Test
        public void getCurrentBalance_ValidCustomer_ShouldReturnBalance() {
                billRepository.deleteAll().block();

                Bill bill = buildBill2();
                billRepository.save(bill).block();

                StepVerifier.create(billRepository.findByCustomerId(bill.getCustomerId()))
                                .assertNext(savedBill -> {
                                        assertEquals(new BigDecimal("150.00"), savedBill.getAmount());
                                })
                                .verifyComplete();

                // buildBill2() creates UNPAID bill with no dueDate, so no interest is calculated
                BigDecimal expectedBalance = new BigDecimal("150.00");

                client.get()
                                .uri("/bills/customer/" + bill.getCustomerId() + "/bills/current-balance")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(BigDecimal.class)
                                .value(balance -> assertEquals(expectedBalance, balance));
        }

        @Test
        public void getCurrentBalance_InvalidCustomer_ShouldReturnNotFound() {

                String invalidCustomerId = "non-existent-id";

                client.get()
                                .uri("/bills/customer/" + invalidCustomerId + "/current-balance")
                                .exchange()
                                .expectStatus().isNotFound();
        }

    @Test
    void payBill_ValidRequest_ShouldUpdateBillStatus() {
        Bill bill = Bill.builder()
                .billId("bill-456")
                .customerId("cust-123")
                .amount(new BigDecimal(200.0))
                .billStatus(BillStatus.UNPAID)
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        billRepository.deleteAll().then(billRepository.save(bill)).block();

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/25");

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", bill.getCustomerId(), bill.getBillId())
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", "dummy-jwt-token")
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BillResponseDTO.class)
                .consumeWith(response -> {
                    assert response.getResponseBody() != null;
                    assertEquals(BillStatus.PAID, response.getResponseBody().getBillStatus());
                });

        Bill updatedBill = billRepository.findByCustomerIdAndBillId(bill.getCustomerId(), bill.getBillId()).block();
        assertEquals(BillStatus.PAID, updatedBill.getBillStatus());
    }

    @Test
    void payBill_NonExistentBill_ShouldReturnNotFound() {
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/25");

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", "cust-404", "bill-404")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", "dummy-jwt-token")
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    private Bill buildBill() {
                return Bill.builder()
                                .billId("1")
                                .customerId("custId")
                                .vetId("vetId")
                                .visitType("surgery")
                                .date(LocalDate.now().minusDays(10))
                                .amount(new BigDecimal(150.0))
                                .billStatus(BillStatus.UNPAID)
                                .dueDate(LocalDate.now().plusDays(20))
                                .archive(false)
                                .build();
        }

        private Bill buildBill2() {
                Calendar calendar = Calendar.getInstance();
                calendar.set(2022, Calendar.SEPTEMBER, 25);
                LocalDate date = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return Bill.builder()
                                .billId("1")
                                .customerId("custId")
                                .ownerFirstName("John")
                                .ownerLastName("Doe")
                                .vetId("vetId")
                                .visitType("surgery")
                                .date(date)
                                .amount(new BigDecimal("150.00"))
                                .billStatus(BillStatus.UNPAID)
                                .archive(false)
                                .build();
        }

        @Test
        void getBillByBillId_ShouldReturnInterest() {
                billRepository.deleteAll().block();

                Bill overdueBill = Bill.builder()
                        .billId("overdue-1")
                        .customerId("custId")
                        .amount(new BigDecimal("100.00"))
                        .billStatus(BillStatus.OVERDUE)
                        .dueDate(LocalDate.now().minusMonths(1))
                        .build();

                billRepository.save(overdueBill).block();

                // Use centralized utility for compound interest calculation
                BigDecimal expectedInterest = InterestCalculationUtil.calculateCompoundInterest(
                    overdueBill.getAmount(), overdueBill.getDueDate(), LocalDate.now());

                client.get()
                        .uri("/bills/" + overdueBill.getBillId())
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody()
                        .jsonPath("$.interest").isEqualTo(expectedInterest.doubleValue());
        }

    @Test
    void getBillsByAmountRange_integrationTest() {
        Bill bill = buildBill();
        billRepository.deleteAll().then(billRepository.save(bill)).block();

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills/customer/{customerId}/bills/filter-by-amount")
                        .queryParam("minAmount", "100")
                        .queryParam("maxAmount", "200")
                        .build(bill.getCustomerId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo(bill.getCustomerId());
    }

    @Test
    void getBillsByDueDateRange_integrationTest() {
        Bill bill = buildBill();
        billRepository.deleteAll().then(billRepository.save(bill)).block();

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills/customer/{customerId}/bills/filter-by-due-date")
                        .queryParam("startDate", LocalDate.now().minusDays(1))
                        .queryParam("endDate", LocalDate.now().plusDays(30))
                        .build(bill.getCustomerId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo(bill.getCustomerId());
    }

    @Test
    void getBillsByCustomerIdAndDateRange_integrationTest() {
        Bill bill = buildBill();
        billRepository.deleteAll().then(billRepository.save(bill)).block();

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills/customer/{customerId}/bills/filter-by-date")
                        .queryParam("startDate", LocalDate.now().minusDays(20))
                        .queryParam("endDate", LocalDate.now())
                        .build(bill.getCustomerId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo(bill.getCustomerId());
    }

    private Bill buildBill3() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return Bill.builder()
                .billId("staffBill-1")
                .customerId("custId")
                .ownerFirstName("John")
                .ownerLastName("Doe")
                .vetId("vetId")
                .visitType("surgery")
                .date(date)
                .dueDate(date.plusDays(15))
                .amount(new BigDecimal("150.00"))
                .billStatus(BillStatus.UNPAID)
                .archive(false)
                .build();
    }

    @Test
    void testDownloadStaffBillPdf() {

        Bill bill = buildBill3();

        // Clean DB, insert test bill
        Mono<Void> setup = billRepository.deleteAll()
                .then(billRepository.save(bill))
                .then();

        StepVerifier.create(setup)
                .verifyComplete();

        // Perform GET /api/staff/bills/{billId}/pdf
        client.get()
                .uri("/bills/{billId}/pdf", bill.getBillId())
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    byte[] pdf = response.getResponseBody();
                    assertNotNull(pdf);
                    assertTrue(pdf.length > 0, "PDF bytes should not be empty");
                });
    }
    @Test
    void testDownloadStaffBillPdf_BillNotFound() {
        // Clean DB to ensure no bills exist
        Mono<Void> setup = billRepository.deleteAll().then();
        StepVerifier.create(setup).verifyComplete();

        // Request non-existing bill
        client.get()
                .uri("/bills/{billId}/pdf", "nonexistent-bill-id")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isEqualTo(500); // INTERNAL_SERVER_ERROR
    }



}