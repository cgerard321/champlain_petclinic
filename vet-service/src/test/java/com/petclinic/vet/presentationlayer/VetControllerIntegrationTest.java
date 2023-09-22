package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Rating;
import com.petclinic.vet.dataaccesslayer.RatingRepository;
import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.servicelayer.RatingRequestDTO;
import com.petclinic.vet.servicelayer.RatingResponseDTO;
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

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class VetControllerIntegrationTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    VetRepository vetRepository;

    @Autowired
    RatingRepository ratingRepository;


    Vet vet = buildVet();
    Vet vet2 = buildVet2();
    Rating rating1 = buildRating("12345", vet.getVetId(), 5.0);
    Rating rating2 = buildRating("12346", vet.getVetId(), 4.0);
    VetDTO vetDTO = buildVetDTO();
    String VET_ID = vet.getVetId();
    String VET_BILL_ID = vet.getVetBillId();
    String INVALID_VET_ID = "mjbedf";
    RatingRequestDTO updatedRating = RatingRequestDTO.builder()
            .rateScore(2.0)
            .vetId(vet.getVetId())
            .rateDescription("Vet cancelled last minute.")
            .rateDate("20/09/2023")
            .build();

    @Test
    void getAllRatingsForAVet_WithValidVetId_ShouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1))
                .thenMany(ratingRepository.save(rating2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(RatingResponseDTO.class)
                .value((list) -> {
                    assertEquals(2, list.size());
                    assertEquals(rating1.getRatingId(), list.get(0).getRatingId());
                    assertEquals(rating1.getVetId(), list.get(0).getVetId());
                    assertEquals(rating1.getRateScore(), list.get(0).getRateScore());
                    assertEquals(rating2.getRatingId(), list.get(1).getRatingId());
                    assertEquals(rating2.getVetId(), list.get(1).getVetId());
                    assertEquals(rating2.getRateScore(), list.get(1).getRateScore());
                });
    }

    @Test
    void getNumberOfRatingsForAVet_WithValidVetId_ShouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1))
                .thenMany(ratingRepository.save(rating2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/" + VET_ID + "/ratings/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Integer.class)
                .value((count) -> {
                    assertEquals(2, count);
                });
    }

    @Test
    void addRatingToAVet_WithValidValues_ShouldSucceed() {
        StepVerifier
                .create(ratingRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();

        client.post()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertNotNull(dto.getRatingId());
                    assertThat(dto.getVetId()).isEqualTo(ratingRequestDTO.getVetId());
                    assertThat(dto.getRateScore()).isEqualTo(ratingRequestDTO.getRateScore());
                    assertThat(dto.getRateDescription()).isEqualTo(ratingRequestDTO.getRateDescription());
                    assertThat(dto.getRateDate()).isEqualTo(ratingRequestDTO.getRateDate());
                });

    }

    @Test
    void updateRating_withValidVetIdAndValidRatingId_shouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String existingRatingId = rating1.getRatingId();

        client.put()
                .uri("/vets/" + VET_ID + "/ratings/" + existingRatingId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRating)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseDTO.class)
                .value(ratingResponseDTO -> {
                    assertNotNull(ratingResponseDTO);
                    assertNotNull(ratingResponseDTO.getRatingId());
                    assertThat(ratingResponseDTO.getRatingId()).isEqualTo(existingRatingId);
                    assertThat(ratingResponseDTO.getVetId()).isEqualTo(updatedRating.getVetId());
                    assertThat(ratingResponseDTO.getRateScore()).isEqualTo(updatedRating.getRateScore());
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(updatedRating.getRateDescription());
                    assertThat(ratingResponseDTO.getRateDate()).isEqualTo(updatedRating.getRateDate());
                });
    }

    @Test
    void updateRating_withValidVetIdAndInvalidRatingId_shouldNotSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String invalidRatingId = "123";

        client.put()
                .uri("/vets/" + VET_ID + "/ratings/" + invalidRatingId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRating)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Rating with id " + invalidRatingId + " not found.");
    }

    @Test
    void updateRating_withInvalidValues_shouldNotSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        Double invalidRateScore=9.0;

        RatingRequestDTO invalidRating = RatingRequestDTO.builder()
                .rateScore(invalidRateScore)
                .vetId(vet.getVetId())
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        client.put()
                .uri("/vets/" + VET_ID + "/ratings/" + rating1.getRatingId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRating)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("rateScore should be between 1 and 5" + invalidRateScore);
    }

    @Test
    void deleteARatingForVet_WithValidId_ShouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll().
                thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .delete()
                .uri("/vets/" + vet.getVetId() + "/ratings/{ratingId}", rating1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }


    @Test
    void getAverageRatingByVetId_ShouldSucceed() {

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(vet.getVetId())
                .rateScore(rating1.getRateScore()).build();


        client.get()
                .uri("/vets/" + ratingRequestDTO.getVetId() + "/ratings" + "/average")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Double.class)
                .value(resp -> {
                    assertEquals(rating1.getRateScore(), ratingRequestDTO.getRateScore());
                });
    }

    @Test
    void getAverageRatingByVetId_withInvalidVetId_ShouldThrowNotFound() {
        client
                .get()
                .uri("/vets/" + INVALID_VET_ID + "/ratings/average")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .value(avg -> {
                            assertEquals(0.0, avg);
                        }
                );
    }

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

    private Rating buildRating(String ratingId, String vetId, Double rateScore) {
        return Rating.builder()
                .ratingId(ratingId)
                .vetId(vetId)
                .rateScore(rateScore)
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
