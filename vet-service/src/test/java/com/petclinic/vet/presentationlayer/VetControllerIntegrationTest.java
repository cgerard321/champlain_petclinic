package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.servicelayer.VetDTO;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class VetControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    VetRepository vetRepository;


    Vet vet = buildVet();
    Vet vet2 = buildVet2();
    VetDTO vetDTO = buildVetDTO();
    String VET_ID = vet.getVetId();
    String VET_BILL_ID = vet.getVetBillId();
    String INVALID_VET_ID = "mjbedf";

    @Test
    void getAllVets() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vet.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vet.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vet.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vet.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vet.getEmail())
                .jsonPath("$[0].imageId").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vet.isActive())
                .jsonPath("$[0].workday").isEqualTo(vet.getWorkday());
    }

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
                .jsonPath("$.vetBillId").isEqualTo(vet.getVetBillId())
                .jsonPath("$.resume").isEqualTo(vet.getResume())
                .jsonPath("$.lastName").isEqualTo(vet.getLastName())
                .jsonPath("$.firstName").isEqualTo(vet.getFirstName())
                .jsonPath("$.email").isEqualTo(vet.getEmail())
                .jsonPath("$.active").isEqualTo(vet.isActive())
                .jsonPath("$.workday").isEqualTo(vet.getWorkday());

    }

    @Test
    void getVetByVetBillId() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));
        String uri = "/vets/vetBillId/{vetBillId}";

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/vetBillId/" + VET_BILL_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vet.getVetId())
                .jsonPath("$.vetBillId").isEqualTo(vet.getVetBillId())
                .jsonPath("$.resume").isEqualTo(vet.getResume())
                .jsonPath("$.lastName").isEqualTo(vet.getLastName())
                .jsonPath("$.firstName").isEqualTo(vet.getFirstName())
                .jsonPath("$.email").isEqualTo(vet.getEmail())
                .jsonPath("$.active").isEqualTo(vet.isActive())
                .jsonPath("$.workday").isEqualTo(vet.getWorkday());

    }

    @Test
    void updateVet() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(vetDTO), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vetDTO.getVetId())
                .jsonPath("$.resume").isEqualTo(vetDTO.getResume())
                .jsonPath("$.lastName").isEqualTo(vetDTO.getLastName())
                .jsonPath("$.firstName").isEqualTo(vetDTO.getFirstName())
                .jsonPath("$.email").isEqualTo(vetDTO.getEmail())
                .jsonPath("$.imageId").isNotEmpty()
                .jsonPath("$.active").isEqualTo(vetDTO.isActive())
                .jsonPath("$.workday").isEqualTo(vetDTO.getWorkday());

}

    @Test
    void getVetIsActive() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

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
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vet2.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vet2.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vet2.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vet2.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vet2.getEmail())
                .jsonPath("$[0].imageId").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vet2.isActive())
                .jsonPath("$[0].workday").isEqualTo(vet2.getWorkday());
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
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vet.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vet.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vet.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vet.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vet.getEmail())
                .jsonPath("$[0].imageId").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vet.isActive())
                .jsonPath("$[0].workday").isEqualTo(vet.getWorkday());
    }


    @Test
    void createVet() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(vet), Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VetDTO.class)
                .value((dto) -> {
                assertThat(dto.getFirstName()).isEqualTo(vet.getFirstName());
                assertThat(dto.getLastName()).isEqualTo(vet.getLastName());
                assertThat(dto.getPhoneNumber()).isEqualTo(vet.getPhoneNumber());
                assertThat(dto.getResume()).isEqualTo(vet.getResume());
                assertThat(dto.getEmail()).isEqualTo(vet.getEmail());
                assertThat(dto.getWorkday()).isEqualTo(vet.getWorkday());
                assertThat(dto.getImageId()).isEqualTo(vet.getImageId());
                assertThat(dto.isActive()).isEqualTo(vet.isActive());
                assertThat(dto.getSpecialties()).isEqualTo(vet.getSpecialties());
                });
    }


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

    @Test
    void toStringBuilders() {
        System.out.println(Vet.builder());
        System.out.println(VetDTO.builder());
    }


    @Test
    void getByVetId_Invalid() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/" + INVALID_VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");

    }
    @Test
    void updateByVetId_Invalid() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .put()
                .uri("/vets/" + INVALID_VET_ID)
                .body(Mono.just(vetDTO), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");

    }

    @Test
    void deleteByVetId_Invalid() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .delete()
                .uri("/vets/" + INVALID_VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");

    }


    private Vet buildVet() {
        return Vet.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday("Monday")
                .imageId("kjd")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private Vet buildVet2() {
        return Vet.builder()
                .vetId("678910")
                .vetBillId("2")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .active(true)
                .specialties(new HashSet<>())
                .build();
    }
    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }



}