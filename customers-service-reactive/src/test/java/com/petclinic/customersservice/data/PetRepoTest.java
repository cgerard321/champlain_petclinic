package com.petclinic.customersservice.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import org.reactivestreams.Publisher;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@DataMongoTest
class PetRepoTest {

    @Autowired
    PetRepo repo;
    Date date = new Date(20221010);

    @Test
    void insertPet() {
        Pet pet = buildPet();
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(pet));

        StepVerifier
                .create(setup)
                .consumeNextWith(foundPet -> {
                    assertEquals(pet.getId(), foundPet.getId());
                    assertEquals(pet.getName(), foundPet.getName());
                    assertEquals(pet.getBirthDate(), foundPet.getBirthDate());
                    assertEquals(pet.getPhotoId(), foundPet.getPhotoId());
                    assertEquals(pet.getOwnerId(), foundPet.getOwnerId());
                    assertEquals(pet.getIsActive(),foundPet.getIsActive());
                })
                .verifyComplete();
    }

    @Test
    void deletePet() {

        Pet pet = buildPet();
        repo.save(pet);

        Publisher<Void> setup = repo.deleteById(pet.getId());

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();
    }

    private Pet buildPet() {
        return Pet.builder()
                .id("55")
                .name("Coco")
                .birthDate(date)
                .petTypeId("2")
                .photoId("2")
                .ownerId("2")
                .isActive("true")
                .build();
    }

}
