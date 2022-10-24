package com.petclinic.customersservice.presentation;

import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import static reactor.core.publisher.Mono.just;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class PetTypeControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetTypeRepo petTypeRepo;

    @Test
    void getAllPetTypes() {

        PetType petType = buildPetType();

        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(petType));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        webTestClient.get()
                .uri("/owners/petTypes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(petType.getId())
                .jsonPath("$[0].name").isEqualTo(petType.getName());
    }

    @Test
    void deletePetTypeByID(){
        PetType petType = buildPetType();

      petTypeRepo.save(petType);

      Publisher<Void> setup = petTypeRepo.deletePetTypeById(buildPetType().getId());

      StepVerifier
              .create(setup)
              .expectNextCount(0)
              .verifyComplete();

      webTestClient.delete()
              .uri("/owners/petTypes/" + petType.getId())
              .accept(MediaType.APPLICATION_JSON)
              .exchange()
              .expectStatus().isOk()
              .expectBody();
    }

//    @Test
//    void updatePetType(){
//        PetType pet1 = buildPetType();
//        PetType pet2 = buildPetType2();
//
//        pet2.setId(20);
//
//        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(pet1));
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(1)
//                .verifyComplete();
//
//        webTestClient.get()
//                .uri("/owners/petTypes/" + pet1.getId())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo(pet1.getName());
//
//        webTestClient.put()
//                .uri("/owners/petTypes/" + pet1.getId())
//                .body(just(pet2), PetType.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo(pet2.getName());
//
//        webTestClient.get()
//                .uri("/owners/petTypes/" + pet1.getId())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo(pet2.getName());
//    }

//    @Test
//    void insertPetType(){
//
//        PetType pet = buildPetType();
//
//        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(pet));
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(1)
//                .verifyComplete();
//
//        webTestClient.post()
//                .uri("/owners/petTypes")
//                .body(just(pet), PetType.class)
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody();
//
//        webTestClient.get()
//                .uri("/owners/petTypes/" + pet.getId())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo(pet.getName());
//
//    }

    private PetType buildPetType() {
        return PetType.builder().id(10).name("TestType").build();
    }
    private PetType buildPetType2() {
        return PetType.builder().id(20).name("TestType2").build();
    }

}