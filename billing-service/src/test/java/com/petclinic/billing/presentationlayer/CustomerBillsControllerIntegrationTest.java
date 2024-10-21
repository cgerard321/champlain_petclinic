package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.datalayer.BillStatus;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CustomerBillsControllerIntegrationTest {

        @Autowired
        private WebTestClient client;

        @Autowired
        private BillRepository billRepository;

        @Test
        void getBillsByCustomerId_shouldSucceed() {
                Bill bill = buildBill();
                Publisher<Bill> setup = billRepository.deleteAll().thenMany(billRepository.save(bill));

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
                                .assertNext(savedBill -> assertEquals(150.0, savedBill.getAmount()))
                                .verifyComplete();

                double expectedBalance = 150.0;

                client.get()
                                .uri("/bills/customer/" + bill.getCustomerId() + "/bills/current-balance")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(Double.class)
                                .value(balance -> assertEquals(expectedBalance, balance, 0.01));
        }

        @Test
        public void getCurrentBalance_InvalidCustomer_ShouldReturnNotFound() {

                String invalidCustomerId = "non-existent-id";

                client.get()
                                .uri("/bills/customer/" + invalidCustomerId + "/current-balance")
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
                                .amount(150.0)
                                .dueDate(LocalDate.now().plusDays(20))
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
                                .amount(150.0)
                                .billStatus(BillStatus.PAID)
                                .build();
        }
}