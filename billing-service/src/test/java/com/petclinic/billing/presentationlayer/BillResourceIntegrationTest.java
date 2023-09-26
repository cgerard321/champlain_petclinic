package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class BillResourceIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private BillRepository repo;


    @Test
    void findAllBills() {

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
    void findBillByValidBillID() {

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


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).build();
    }
}