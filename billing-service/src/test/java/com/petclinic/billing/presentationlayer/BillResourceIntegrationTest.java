package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.http.HttpErrorInfo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import static reactor.core.publisher.Mono.just;
import java.util.Calendar;
import java.util.Date;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 27017"})
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
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].visitType").isEqualTo(billEntity.getVisitType())
                .jsonPath("$[0].customerId").isEqualTo(billEntity.getCustomerId());
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
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].visitType").isEqualTo(billEntity.getVisitType())
                .jsonPath("$[0].customerId").isEqualTo(billEntity.getCustomerId())
                .jsonPath("$[0].amount").isEqualTo(billEntity.getAmount());
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
    void deleteBillsByCustomerId() {

        Bill billEntity = buildBill();

        repo.save(billEntity);

        Publisher<Void> setup = repo.deleleBillsByCustomerId(billEntity.getCustomerId());

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
        Date date = calendar.getTime();


        return Bill.builder().id("Id").billId("BillUUID").customerId(1).visitType("Test Type").visitDate(date).amount(13.37).build();
    }
}