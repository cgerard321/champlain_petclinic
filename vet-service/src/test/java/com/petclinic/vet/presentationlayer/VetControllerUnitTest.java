package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.servicelayer.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @MockBean
    RatingService ratingService;

    VetDTO vetDTO = buildVetDTO();
    VetDTO vetDTO2 = buildVetDTO2();

    RatingResponseDTO ratingDTO = buildRatingDTO();
    Vet vet = buildVet();
    String VET_ID = vet.getVetId();
    String VET_BILL_ID = vet.getVetBillId();
    String INVALID_VET_ID = "mjbedf";


    @Test
    void getAllRatingForVetByVetId_ShouldSucceed() {
        when(ratingService.getAllRatingsByVetId(anyString()))
                .thenReturn(Flux.just(ratingDTO));

        client
                .get()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].ratingId").isEqualTo(ratingDTO.getRatingId())
                .jsonPath("$[0].vetId").isEqualTo(ratingDTO.getVetId())
                .jsonPath("$[0].rateScore").isEqualTo(ratingDTO.getRateScore());

        Mockito.verify(ratingService, times(1))
                .getAllRatingsByVetId(VET_ID);
    }
    @Test
    void deleteRatingForVetByRatingId_ShouldSucceed() {
        String ratingId = "794ac37f-1e07-43c2-93bc-61839e61d989";
        String vetId = "694ac37f-1e07-43c2-93bc-61839e61d989";

        when(ratingService.deleteRatingByRatingId(vetId,ratingId))
                .thenReturn((Mono.empty()));

        client
                .delete()
                .uri("/vets/" + vetId + "/ratings/{ratingId}", ratingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(ratingService, times(1))
                .deleteRatingByRatingId(vetId,ratingId);
    }

    @Test
    void addRatingWithVetId_ValidValues_ShouldSucceed() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                        .vetId(VET_ID)
                        .rateScore(3.5)
                        .rateDescription("The vet was decent but lacked table manners.")
                        .rateDate("16/09/2023")
                        .build();
        RatingResponseDTO rating = buildRatingResponseDTO(ratingRequestDTO.getRateDescription(), ratingRequestDTO.getRateScore());

        when(ratingService.addRatingToVet(VET_ID, Mono.just(ratingRequestDTO)))
                .thenReturn(Mono.just(rating));

        client.post()
                .uri("/vets/{vetId}/ratings", VET_ID)
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(ratingService, times(1))
                .addRatingToVet(anyString(), any(Mono.class));
    }

    @Test
    void getNumberOfRatingsByVetId_ShouldSucceed() {
        when(ratingService.getNumberOfRatingsByVetId(anyString()))
                .thenReturn(Mono.just(1));

        client
                .get()
                .uri("/vets/" + VET_ID + "/ratings/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo(1);

        Mockito.verify(ratingService, times(1))
                .getNumberOfRatingsByVetId(VET_ID);
    }


    @Test
    void getAverageRatingForEachVetByVetId_ShouldSucceed(){

        String vetId = "1";
        Double averageRating = 5.0;

        when(vetService.getVetByVetId(vetId))
                .thenReturn(Mono.just(vetDTO));

        when(ratingService.getAverageRatingByVetId(vetId))
                .thenReturn(Mono.just(ratingDTO.getRateScore()));


        client.get()
                .uri("/vets/" + vetId + "/ratings" + "/average")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Double.class)
                .value(d ->{
                    assertEquals(d,averageRating);
                });

        Mockito.verify(ratingService, times(1))
                .getAverageRatingByVetId(ratingDTO.getVetId());

    }


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
                .jsonPath("$.vetBillId").isEqualTo(vet.getVetBillId())
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
    void getVetByVetBillId() {
        when(vetService.getVetByVetBillId(anyString()))
                .thenReturn(Mono.just(vetDTO));

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

        Mockito.verify(vetService, times(1))
                .getVetByVetBillId(VET_BILL_ID);
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
                .vetBillId("1")
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
    private VetDTO buildVetDTO2() {
        return VetDTO.builder()
                .vetId("678910")
                .vetBillId("2")
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

    private RatingResponseDTO buildRatingDTO() {
        return RatingResponseDTO.builder()
                .ratingId("1")
                .vetId("1")
                .rateScore(5.0)
                .build();
    }
    private RatingResponseDTO buildRatingResponseDTO(String description, double score) {
        return RatingResponseDTO.builder()
                .ratingId("2")
                .vetId(VET_ID)
                .rateScore(score)
                .rateDescription(description)
                .rateDate("16/09/2023")
                .build();
    }
}