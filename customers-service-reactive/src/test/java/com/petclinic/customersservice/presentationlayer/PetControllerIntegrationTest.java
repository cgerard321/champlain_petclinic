package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static com.mongodb.assertions.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
class PetControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private PetRepo repo;

    @MockBean
    private FilesServiceClient filesServiceClient;

    Pet petEntity = buildPet();
    String PET_ID = petEntity.getPetId();

    private String validPetId;

    @Test
    void deletePetByPetId() {
        repo.save(petEntity);
        Publisher<Void> setup = repo.deleteById(petEntity.getId());
        StepVerifier.create(setup)
                .expectNextCount(0)
                .verifyComplete();
        client.delete()
                .uri("/pets/" + petEntity.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBody();
    }

    @Test
    void getAllPets() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client
                .get()
                .uri("/pets")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(PetResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                });
    }

    @Test
    void getPetByPetId() {
        Mono<Pet> petMono = Mono.from(repo.findAll()
                .doOnNext(pet -> {
                    validPetId = pet.getPetId();
                    System.out.println(validPetId);
                }));

        StepVerifier.create(petMono)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/pets/{petId}", validPetId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(validPetId);
    }

    @Test
    void updatePetByPetId() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.put().uri("/pets/" + PET_ID)
                .body(Mono.just(petEntity), Pet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(petEntity.getPetId())
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$.ownerId").isEqualTo(petEntity.getOwnerId())
                .jsonPath("$.photoId").isEqualTo(petEntity.getPhotoId())
                .jsonPath("$.isActive").isEqualTo(petEntity.getIsActive());
    }

    @Test
    void insertPet() {
        Publisher<Void> setup = repo.deleteAll();
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();
        client.post().uri("/pets")
                .body(Mono.just(petEntity), Pet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(petEntity.getPetId())
                .jsonPath("$.name").isEqualTo(petEntity.getName())
                .jsonPath("$.petTypeId").isEqualTo(petEntity.getPetTypeId())
                .jsonPath("$.ownerId").isEqualTo(petEntity.getOwnerId())
//                .jsonPath("$.photoId").isEqualTo(petEntity.getPhotoId())
                .jsonPath("$.isActive").isEqualTo(petEntity.getIsActive());


    }

    @Test
    void updatePetIsActive() {
        Publisher<Pet> setup = repo.deleteAll().thenMany(repo.save(petEntity));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        client.patch().uri("/pets/" + PET_ID)
                .body(Mono.just(petEntity), Pet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.isActive").isEqualTo(petEntity.getIsActive());
    }

    private Pet buildPet() {
        return Pet.builder()
                .id("abc123")
                .petId("abc12345")
                .name("leonardo")
                .ownerId("111")
                .petTypeId("111")
                .photoId("111")
                .isActive("true")
                .build();
    }
}
