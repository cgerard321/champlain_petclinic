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
    void deletePetTypeByID(){

        PetType petType = buildPetType();

        petTypeRepo.save(petType);

        Publisher<Void> setup = petTypeRepo.deletePetTypeById(petType.getId());

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findPetTypeByID(){
        PetType pet = buildPetType();

        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(pet));
        Publisher<PetType> find = petTypeRepo.findPetTypesById(pet.getId());

        StepVerifier
                .create(setup)
                .expectNext(pet)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();

    }

    private PetType buildPetType() {
        return PetType.builder().id(10).name("TestType").build();
    }

}
