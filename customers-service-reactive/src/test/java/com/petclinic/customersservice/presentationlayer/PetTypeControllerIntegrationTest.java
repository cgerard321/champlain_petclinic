package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.PetTypeRepo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static com.mongodb.assertions.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
class PetTypeControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PetTypeRepo petTypeRepo;

    PetType petTypeEntity2 = buildPetType2();

    String PETTYPE_ID = petTypeEntity2.getId();
    String PUBLIC_PETTYPE_ID = petTypeEntity2.getPetTypeId();


    /*
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

     */


    @Test
    void getAllPetTypes_shouldSucceed() {
        webTestClient.get()
                .uri("/owners/petTypes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PetTypeResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(6, list.size());
                });
    }

/*
    @Test
    void deletePetTypeByPetTypeId() {
        petTypeRepo.save(petTypeEntity2);
        Publisher<Void> setup = petTypeRepo.deleteById(PUBLIC_PETTYPE_ID);
        StepVerifier.create(setup).expectNextCount(0).verifyComplete();
        webTestClient.delete().uri("/owners/petTypes/" + PUBLIC_PETTYPE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk().expectBody();

    }*/


    /*
    @Test
    void updatePetType() {
        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(petTypeEntity2));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        webTestClient.put().uri("/owners/petTypes/" + PUBLIC_PETTYPE_ID)
                .body(Mono.just(petTypeEntity2), PetType.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petTypeId").isEqualTo(petTypeEntity2.getPetTypeId())
                .jsonPath("$.name").isEqualTo(petTypeEntity2.getName())
                .jsonPath("$.petTypeDescription").isEqualTo(petTypeEntity2.getPetTypeDescription());


    }



    @Test
    void getOwnerByOwnerId() {
        Publisher<PetType> setup = petTypeRepo.deleteAll().thenMany(petTypeRepo.save(petTypeEntity2));
        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
        webTestClient.get().uri("/owners/petTypes/" + PUBLIC_PETTYPE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PetTypeResponseDTO.class)
                .value(petTypeResponseDTO -> {
                    assertNotNull(petTypeResponseDTO);
                    assertEquals(petTypeResponseDTO.getPetTypeId(),petTypeEntity2.getPetTypeId());
                    assertEquals(petTypeResponseDTO.getName(),petTypeEntity2.getName());
                    assertEquals(petTypeResponseDTO.getPetTypeDescription(),petTypeEntity2.getPetTypeDescription());

                });

    }

     */

    @Test
    void deletePetType_ShouldReturnNoContent() {
        PetType petType = PetType.builder()
                .id("4283c9b8-4ffd-4866-a5ed-287117c60a40")
                .petTypeId("4283c9b8-4ffd-4866-a5ed-287117c60a40")
                .name("Cat")
                .petTypeDescription("Mammal")
                .build();

        try {
            PetType savedPetType = petTypeRepo.save(petType).block();

            System.out.println("Saved pet type: " + savedPetType);
            if (savedPetType != null) {
                System.out.println("Pet type ID: " + savedPetType.getPetTypeId());
            }

            webTestClient.delete()
                    .uri("/owners/petTypes/4283c9b8-4ffd-4866-a5ed-287117c60a40")
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();

            StepVerifier.create(petTypeRepo.findOPetTypeById("4283c9b8-4ffd-4866-a5ed-287117c60a40")
                            .timeout(Duration.ofSeconds(5))
                            .onErrorMap(TimeoutException.class, e ->
                                    new RuntimeException("Database query timed out", e)))
                    .expectComplete()
                    .verify();

        } catch (NotFoundException e) {
            fail("Pet type not found during test: " + e.getMessage());
        } catch (InvalidInputException e) {
            fail("Invalid input during test: " + e.getMessage());
        }catch (Exception e) {
            fail("Test failed with unexpected exception: " + e.getMessage());
        } finally {
            try {
                petTypeRepo.deleteByPetTypeId("4283c9b8-4ffd-4866-a5ed-287117c60a40").block();
            } catch (Exception cleanupException) {
                System.err.println("Cleanup failed: " + cleanupException.getMessage());
            }
        }


    }






    private PetType buildPetType() {
        return PetType.builder().id("10").name("TestType").build();
    }

    private PetType buildPetType2() {
        return PetType.builder()
                .id("10")
                .petTypeId("petTypeId-1234")
                .name("Dog")
                .petTypeDescription("Mammal")
                .build();
    }
}


