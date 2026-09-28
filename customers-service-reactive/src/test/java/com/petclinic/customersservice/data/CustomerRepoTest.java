package com.petclinic.customersservice.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import org.reactivestreams.Publisher;
import org.junit.jupiter.api.Test;

@DataMongoTest
class CustomerRepoTest {

    @Autowired
    CustomerRepo repo;

    @Test
    void getAllCustomers_shouldSucceed() {
        Customer customer = buildCustomer();

        Publisher<Customer> setup = repo.deleteAll().thenMany(repo.save(customer));

        StepVerifier
                .create(setup)
                .expectNext(customer)
                .verifyComplete();
        StepVerifier
                .create(repo.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void insertCustomer() {
        Customer customer = buildCustomer();

        Publisher<Customer> setup = repo.deleteAll().thenMany(repo.save(customer));

        StepVerifier
                .create(setup)
                .consumeNextWith(foundCustomer -> {
                    //assertEquals(customer.getId(), foundCustomer.getId());
                    assertEquals(customer.getFirstName(), foundCustomer.getFirstName());
                    assertEquals(customer.getLastName(), foundCustomer.getLastName());
                    assertEquals(customer.getAddress(), foundCustomer.getAddress());
                    assertEquals(customer.getCity(), foundCustomer.getCity());
                    assertEquals(customer.getProvince(), foundCustomer.getProvince());
                    assertEquals(customer.getTelephone(), foundCustomer.getTelephone());
                    //assertEquals(customer.getPhotoId(), foundCustomer.getPhotoId());
                })
                .verifyComplete();
    }

    @Test
    void deleteCustomer() {

        Customer customer = buildCustomer();

        Publisher<Void> setup = repo.save(customer)
                .then(repo.deleteById(customer.getId()));

        StepVerifier
                .create(setup)
                .verifyComplete();
    }

    private Customer buildCustomer() {
        return Customer.builder()
                .id("55")
                .customerId("customerId-123")
                .firstName("Felix")
                .lastName("Labrie")
                .address("308 ave de Stanley")
                .city("Saint-Lambert")
                .province("Quebec")
                .telephone("514-516-1191")
                //.photoId("55")
                .build();
    }
}
