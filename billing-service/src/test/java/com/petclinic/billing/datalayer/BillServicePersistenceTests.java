package com.petclinic.billing.datalayer;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataMongoTest
public class BillServicePersistenceTests {

    @Autowired
    BillRepository repo;



    @Test
    void shouldSaveOneBill(){

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(buildBill()));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldFindBillByBillUUID(){

        Bill bill = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(buildBill()));
        Publisher<Bill> find = repo.findByBillId(bill.getBillId());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();

    }


    @Test
    void shouldFindBillByCustomerId(){

        Bill bill = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(buildBill()));
        Publisher<Bill> find = repo.findByCustomerId(bill.getCustomerId());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();

    }
    @Test
    void shouldFindBillByVetId(){

        Bill bill = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(buildBill()));
        Publisher<Bill> find = repo.findByVetId(bill.getVetId());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void shouldUpdateBillInformation() {

        Bill originalBill = buildBill();
        repo.save(originalBill).block();


        Bill updatedBill = Bill.builder()
                .id(originalBill.getId())
                .billId(originalBill.getBillId())
                .customerId(originalBill.getCustomerId())
                .vetId(originalBill.getVetId())
                .visitType("New Visit Type")
                .date(originalBill.getDate())
                .amount(42.0)
                .build();


        Mono<Bill> updateMono = repo.save(updatedBill);


        StepVerifier
                .create(updateMono)
                .expectNext(updatedBill)
                .verifyComplete();


        Mono<Bill> retrievedBillMono = repo.findById(originalBill.getId());

        StepVerifier
                .create(retrievedBillMono)
                .assertNext(retrievedBill -> {

                    assertEquals(updatedBill.getVisitType(), retrievedBill.getVisitType());
                    assertEquals(updatedBill.getAmount(), retrievedBill.getAmount());
                })
                .verifyComplete();
    }

    @Test
    void shouldDeleteBillByBillUUID(){

        Bill bill = buildBill();
        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(bill));

        Publisher<Void> delete = repo.deleteBillByBillId(bill.getBillId());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(delete)
                .expectNextCount(0)
                .verifyComplete();



    }

    @Test
    void shouldDeleteBillByVetId(){

        Bill bill = buildBill();
        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(bill));

        Publisher<Void> delete = repo.deleteBillsByVetId(bill.getVetId());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(delete)
                .expectNextCount(0)
                .verifyComplete();



    }
    @Test
    void shouldDeleteBillsByCustomerId(){

        Bill bill = buildBill();
        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(bill));

        Publisher<Void> delete = repo.deleteBillsByCustomerId(bill.getCustomerId());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(delete)
                .expectNextCount(0)
                .verifyComplete();
    }


    private Bill buildBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();;


        return Bill.builder().id("Id").billId("BillUUID").customerId(1).vetId("1").visitType("Test Type").date(date).amount(13.37).build();
    }
}
