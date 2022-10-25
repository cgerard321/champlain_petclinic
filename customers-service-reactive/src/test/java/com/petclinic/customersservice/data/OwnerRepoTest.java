package com.petclinic.customersservice.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import org.reactivestreams.Publisher;
import org.junit.jupiter.api.Test;

@DataMongoTest
class OwnerRepoTest {

    @Autowired
    OwnerRepo repo;

    @Test
    void insertOwner() {
        Owner owner = buildOwner();

        Publisher<Owner> setup = repo.deleteAll().thenMany(repo.save(owner));

        StepVerifier
                .create(setup)
                .consumeNextWith(foundOwner -> {
                    //assertEquals(owner.getId(), foundOwner.getId());
                    assertEquals(owner.getFirstName(), foundOwner.getFirstName());
                    assertEquals(owner.getLastName(), foundOwner.getLastName());
                    assertEquals(owner.getAddress(), foundOwner.getAddress());
                    assertEquals(owner.getCity(), foundOwner.getCity());
                    assertEquals(owner.getTelephone(), foundOwner.getTelephone());
                    assertEquals(owner.getPhotoId(), foundOwner.getPhotoId());
                })
                .verifyComplete();
    }

    @Test
    void deleteOwner() {

        Owner owner = buildOwner();
        repo.save(owner);

        Publisher<Void> setup = repo.deleteByOwnerId(owner.getId());

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();
    }

    private Owner buildOwner() {
        return Owner.builder()
                .id("55")
                .firstName("Felix")
                .lastName("Labrie")
                .address("308 ave de Stanley")
                .city("Saint-Lambert")
                .telephone("514-516-1191")
                .photoId("55")
                .build();
    }
}
