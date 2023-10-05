package com.petclinic.customersservice.presentationlayer;

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
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

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


