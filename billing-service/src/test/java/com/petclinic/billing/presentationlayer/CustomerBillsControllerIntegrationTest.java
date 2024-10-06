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
                .uri("/customers/{customerId}/bills", bill.getCustomerId())
                .accept(MediaType.APPLICATION_JSON) // Use application/json instead of stream
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerId").isEqualTo(bill.getCustomerId()); // Assuming a list is returned
    }


    @Test
    void testDownloadBillPdf() {
        // Set up test data
        Bill bill = buildBill(); // Build a sample bill

        // Chain the operations to ensure data is saved before the GET request
        Mono<Void> setup = billRepository.deleteAll()
                .then(billRepository.save(bill))
                .then();

        // Wait until setup is complete
        StepVerifier.create(setup)
                .verifyComplete();

        // Perform GET request
        client.get()
                .uri("/customers/{customerId}/bills/{billId}/pdf", bill.getCustomerId(), bill.getBillId())
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    byte[] pdf = response.getResponseBody();
                    assertNotNull(pdf);
                    assertTrue(pdf.length > 0); // Check that the PDF is not empty
                });
    }

    // Helper method to build Bill entity
    private Bill buildBill() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Bill.builder()
                .billId("1")
                .customerId("custId")
                .vetId("vetId")
                .visitType("surgery")
                .date(date)
                .amount(150.0)
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

