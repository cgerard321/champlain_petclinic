package com.petclinic.billing.datalayer;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.util.Calendar;
import java.util.Date;


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
    void shouldFindBillByOwnerId(){
        Bill bill = buildBill();

        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(buildBill()));
        Publisher<Bill> find = repo.findByOwnerId(bill.getOwnerId());

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
    void shouldDeleteBillsByOwnerId(){

        Bill bill = buildBill();
        Publisher<Bill> setup = repo.deleteAll().thenMany(repo.save(bill));

        Publisher<Void> delete = repo.deleteBillsByOwnerId(bill.getOwnerId());

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
        Date date = calendar.getTime();


        return Bill.builder().id("Id").billId("BillUUID").ownerId("1").vetId("1").visitType("Test Type").visitDate(date).amount(13.37).build();
    }
}
