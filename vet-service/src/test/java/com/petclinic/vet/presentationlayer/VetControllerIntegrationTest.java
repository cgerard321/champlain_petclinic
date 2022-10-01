package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.servicelayer.VetDTO;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class VetControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    VetRepository vetRepository;

    VetDTO vetDTO = buildVetDTO();
    Vet vet = buildVet();
    String VET_ID = vet.getVetId();

    @Test
    void getVetByVetId() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/" + VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vet.getVetId())
                .jsonPath("$.resume").isEqualTo(vet.getResume())
                .jsonPath("$.lastName").isEqualTo(vet.getLastName())
                .jsonPath("$.firstName").isEqualTo(vet.getFirstName())
                .jsonPath("$.email").isEqualTo(vet.getEmail())
                .jsonPath("$.image").isEqualTo(vet.getImage())
                .jsonPath("$.active").isEqualTo(vet.isActive())
                .jsonPath("$.workday").isEqualTo(vet.getWorkday());


    }

    @Test
    void getVetIsActive() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    void getVetIsInactive() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/inactive")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }


//    @Test
//    void createStudent() {
//        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(1)
//                .verifyComplete();
//
//        client
//                .post()
//                .uri("/vets/")
//                .body(Mono.just(vet), Vet.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.vetId").isEqualTo(vet.getVetId())
//                .jsonPath("$.resume").isEqualTo(vet.getResume())
//                .jsonPath("$.lastName").isEqualTo(vet.getLastName())
//                .jsonPath("$.firstName").isEqualTo(vet.getFirstName())
//                .jsonPath("$.email").isEqualTo(vet.getEmail())
//                .jsonPath("$.image").isEqualTo(vet.getImage())
//                .jsonPath("$.active").isEqualTo(vet.isActive())
//                .jsonPath("$.workday").isEqualTo(vet.getWorkday());
//    }


    @Test
    void deleteVet() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .delete()
                .uri("/vets/" + VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

    private Vet buildVet() {
        return Vet.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday("Monday")
                .specialties(new HashSet<>())
                .isActive(false)
                .build();
    }
    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday("Monday")
                .specialties(new HashSet<>())
                .isActive(false)
                .build();
    }



}