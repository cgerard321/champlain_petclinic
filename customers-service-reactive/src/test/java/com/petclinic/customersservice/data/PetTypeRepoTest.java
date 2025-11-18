package com.petclinic.customersservice.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class PetTypeRepoTest {

    @Autowired
    PetTypeRepo petTypeRepo;

    @Test
    void insertPetType() {
        PetType petType = buildPetType();

        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(petType));

        StepVerifier
                .create(setup)
                .consumeNextWith(foundPetType -> {
                    assertEquals(petType.getId(), foundPetType.getId());
                    assertEquals(petType.getId(), foundPetType.getId());
                })
                .verifyComplete();
    }

    @Test
    void getAllOwners_shouldSucceed() {
        PetType petType1= buildPetType();

        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(petType1));

        StepVerifier
                .create(setup)
                .expectNext(petType1)
                .verifyComplete();
        StepVerifier
                .create(petTypeRepo.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }


    private PetType buildPetType1() {
        return PetType.builder().id("10").name("TestType").build();
    }


    private PetType buildPetType() {
        return PetType.builder()
                .id("1")
                .petTypeId("petTypeId-123")
                .name("Dog")
                .build();
    }

}
