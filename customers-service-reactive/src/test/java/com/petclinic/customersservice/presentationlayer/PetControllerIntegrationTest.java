package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@SpringBootTest
@AutoConfigureWebTestClient
class PetControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private PetRepo repo;
    Pet petEntity = buildPet();
    String PET_ID = petEntity.getId();

    @Test
    void deletePetByPetId() {
        repo.save(petEntity);
        Publisher<Void> setup = repo.deleteById(petEntity.getId());
        StepVerifier.create(setup)
                .expectNextCount(0)
                .verifyComplete();
        client.delete()
                .uri("/pet/" + petEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBody();
    }

    @Test
    void getAllPets() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.get().uri("/pet/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(petEntity.getId())
                .jsonPath("$[0].name").isEqualTo(petEntity.getName())
                .jsonPath("$[0].petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$[0].ownerId").isEqualTo(petEntity.getOwnerId())
                .jsonPath("$[0].photoId").isEqualTo(petEntity.getPhotoId());
    }

    @Test
    void getPetByPetId() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.get().uri("/pet/" + PET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(petEntity.getId())
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$.ownerId").isEqualTo(petEntity.getOwnerId())
                .jsonPath("$.photoId").isEqualTo(petEntity.getPhotoId());
    }

    @Test
    void updatePetByPetId() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.put().uri("/pet/" + PET_ID)
                .body(Mono.just(petEntity), Pet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(petEntity.getId())
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$.ownerId").isEqualTo(petEntity.getOwnerId())
                .jsonPath("$.photoId").isEqualTo(petEntity.getPhotoId());
    }

    @Test
    void insertPet() {
        Publisher<Void> setup = repo.deleteAll();
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();
        client.post().uri("/pet")
                .body(Mono.just(petEntity), Pet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(petEntity.getId())
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$.ownerId").isEqualTo(petEntity.getOwnerId())
                .jsonPath("$.photoId").isEqualTo(petEntity.getPhotoId());

    }

    private Pet buildPet() {
        return Pet.builder()
                .id("abc123")
                .name("leonardo")
                .ownerId("111")
                .petTypeId("111")
                .photoId("111")
                .build();
    }

}
