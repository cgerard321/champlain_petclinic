package com.petclinic.customersservice.presentation;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

@SpringBootTest
@AutoConfigureWebTestClient
class PetControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetRepo petRepo;



//    @Test
//    void deletePetByPetId() {
//
//        Pet pet = buildPet();
//        petRepo.save(pet);
//
//        Publisher<Void> setup = petRepo.deletePetById(buildPet().getId());
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(0)
//                .verifyComplete();
//
//        webTestClient.delete()
//                .uri("/pets/" + pet.getId())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody();
//
//    }

    Date date = new Date(20221010);

    private Pet buildPet() {
        return Pet.builder()
                .id(55)
                .name("Test Pet")
                .petTypeId(5)
                .photoId(3)
                .birthDate(date)
                .ownerId(4)
                .build();
    }
}