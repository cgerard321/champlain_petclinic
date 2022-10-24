package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.servicelayer.VetDTO;
import com.petclinic.vet.servicelayer.VetService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers=VetController.class)
class VetControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    VetService vetService;

    VetDTO vetDTO = buildVetDTO();
    VetDTO vetDTO2 = buildVetDTO2();
    Vet vet = buildVet();
    String VET_ID = vet.getVetId();
    String INVALID_VET_ID = "mjbedf";



    @Test
    void getAllVets() {
        when(vetService.getAll())
                .thenReturn(Flux.just(vetDTO));

        client
                .get()
                .uri("/vets")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetDTO.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetDTO.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetDTO.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetDTO.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetDTO.getEmail())
                .jsonPath("$[0].imageId").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vetDTO.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetDTO.getWorkday());

        Mockito.verify(vetService, times(1))
                .getAll();
    }

    @Test
    void getVetByVetId() {
        when(vetService.getVetByVetId(anyString()))
                .thenReturn(Mono.just(vetDTO));

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
                .jsonPath("$.imageId").isNotEmpty()
                .jsonPath("$.active").isEqualTo(vet.isActive())
                .jsonPath("$.workday").isEqualTo(vet.getWorkday());

        Mockito.verify(vetService, times(1))
                .getVetByVetId(VET_ID);
    }

    @Test
    void getActiveVets() {
        when(vetService.getVetByIsActive(anyBoolean()))
                .thenReturn(Flux.just(vetDTO2));

        client
                .get()
                .uri("/vets/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetDTO2.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetDTO2.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetDTO2.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetDTO2.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetDTO2.getEmail())
                .jsonPath("$[0].imageId").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vetDTO2.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetDTO2.getWorkday());

        Mockito.verify(vetService, times(1))
                .getVetByIsActive(vetDTO2.isActive());
    }

    @Test
    void createVet() {
        Mono<VetDTO> dto = Mono.just(vetDTO);
        when(vetService.insertVet(dto))
                .thenReturn(dto);

        client
                .post()
                .uri("/vets")
                .body(dto, Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(vetService, times(1))
                .insertVet(any(Mono.class));
    }

    @Test
    void updateVet() {
        when(vetService.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetDTO));

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

        Mockito.verify(vetService, times(1))
                .updateVet(anyString(), any(Mono.class));
    }

    @Test
    void getInactiveVets() {
        when(vetService.getVetByIsActive(anyBoolean()))
                .thenReturn(Flux.just(vetDTO));

        client
                .get()
                .uri("/vets/inactive")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetDTO.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetDTO.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetDTO.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetDTO.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetDTO.getEmail())
                .jsonPath("$[0].imageId").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vetDTO.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetDTO.getWorkday());

        Mockito.verify(vetService, times(1))
                .getVetByIsActive(vetDTO.isActive());
    }

    @Test
    void deleteVet() {
        when(vetService.deleteVetByVetId(anyString()))
                .thenReturn((Mono.empty()));

        client
                .delete()
                .uri("/vets/" + VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(vetService, times(1))
                .deleteVetByVetId(VET_ID);
    }

    @Test
    void getByVetId_Invalid() {
        when(vetService.getVetByVetId(anyString()))
                .thenReturn(Mono.just(vetDTO));

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
        when(vetService.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetDTO));

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
        when(vetService.deleteVetByVetId(anyString()))
                .thenReturn((Mono.empty()));

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
                .firstName("Pauline")
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

    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("678910")
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
    private VetDTO buildVetDTO2() {
        return VetDTO.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .imageId("kjd")
                .resume("Just became a vet")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(true)
                .build();
    }
}