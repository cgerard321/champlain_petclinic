package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.*;
import com.petclinic.vet.servicelayer.EducationResponseDTO;
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

    @Autowired
    EducationRepository educationRepository;

    Education education1 = buildEducation();
    Education education2 = buildEducation2();
    Vet vet = buildVet("1234");
    Vet vet2 = buildVet2("2345");
    Rating rating1 = buildRating("12345", "678910", 5.0);
    Rating rating2 = buildRating("12346", "678910", 4.0);
    VetDTO vetDTO = buildVetDTO("3456");
    String VET_ID = "678910";
    String VET_BILL_ID = vet.getVetBillId();
    String INVALID_VET_ID = "mjbedf";
    RatingRequestDTO updatedRating = RatingRequestDTO.builder()
            .rateScore(2.0)
            .vetId("678910")
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
    void getAllRatingsForAVet_WithInvalidVetId_ShouldNotSucceed() {
        String invalidVetId="123";
        client
                .get()
                .uri("/vets/" + 123 + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("vetId not found: " + invalidVetId);
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
    void getNumberOfRatingsForAVet_WithInvalidVetId_ShouldNotSucceed() {
        String invalidVetId="123";

        client
                .get()
                .uri("/vets/" + invalidVetId + "/ratings/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("vetId not found: " + invalidVetId);
    }

    @Test
    void addRatingToAVet_WithPredefinedDescriptionOnly_ShouldSetRateDescriptionToPredefinedDescription() {
        StepVerifier
                .create(ratingRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(5.0)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("21/09/2023")
                .build();

        client.post()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseDTO.class)
                .value(ratingResponseDTO -> {
                    assertNotNull(ratingResponseDTO);
                    assertNotNull(ratingResponseDTO.getRatingId());
                    assertThat(ratingResponseDTO.getVetId()).isEqualTo(ratingRequestDTO.getVetId());
                    assertThat(ratingResponseDTO.getRateScore()).isEqualTo(ratingRequestDTO.getRateScore());
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription().name());
                    assertThat(ratingResponseDTO.getPredefinedDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription());
                    assertThat(ratingResponseDTO.getRateDate()).isEqualTo(ratingRequestDTO.getRateDate());
                });
    }
    @Test
    void addRatingWithWrittenDescriptionAndPredefinedValue_ShouldSetRatingDescriptionToPredefinedValue(){
        StepVerifier
                .create(ratingRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(5.0)
                .rateDescription("Vet was very gentle with my hamster.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("21/09/2023")
                .build();

        client.post()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseDTO.class)
                .value(ratingResponseDTO -> {
                    assertNotNull(ratingResponseDTO);
                    assertNotNull(ratingResponseDTO.getRatingId());
                    assertThat(ratingResponseDTO.getVetId()).isEqualTo(ratingRequestDTO.getVetId());
                    assertThat(ratingResponseDTO.getRateScore()).isEqualTo(ratingRequestDTO.getRateScore());
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription().name());
                    assertThat(ratingResponseDTO.getPredefinedDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription());
                    assertThat(ratingResponseDTO.getRateDate()).isEqualTo(ratingRequestDTO.getRateDate());
                });
    }
    @Test
    void addRatingToAVet_WithInvalidVetId_ShouldNotSucceed() {
        StepVerifier
                .create(vetRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier
                .create(ratingRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        String invalidVetId="123";

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(invalidVetId)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();

        client.post()
                .uri("/vets/" + invalidVetId + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("vetId not found: " + invalidVetId);
    }

    @Test
    void addRatingToAVet_WithInvalidRateScore_ShouldNotSucceed() {
        StepVerifier
                .create(ratingRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(8.0)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();

        client.post()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("rateScore should be between 1 and 5: " + ratingRequestDTO.getRateScore());
    }

    @Test
    void addRatingToVet_WithPredefinedDescription_ShouldSetRateDescriptionToPredefinedDescription() {
        StepVerifier
                .create(ratingRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(5.0)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("21/09/2023")
                .build();

        client.post()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseDTO.class)
                .value(ratingResponseDTO -> {
                    assertNotNull(ratingResponseDTO);
                    assertNotNull(ratingResponseDTO.getRatingId());
                    assertThat(ratingResponseDTO.getVetId()).isEqualTo(ratingRequestDTO.getVetId());
                    assertThat(ratingResponseDTO.getRateScore()).isEqualTo(ratingRequestDTO.getRateScore());
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription().name());
                    assertThat(ratingResponseDTO.getPredefinedDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription());
                    assertThat(ratingResponseDTO.getRateDate()).isEqualTo(ratingRequestDTO.getRateDate());
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
    void updateRating_withInvalidVetIdAndValidRatingId_shouldNotSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String existingRatingId = rating1.getRatingId();

        String invalidVetId="123";

        client.put()
                .uri("/vets/" + invalidVetId + "/ratings/" + existingRatingId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRating)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("vetId not found: " + invalidVetId);
    }

    @Test
    void updateRating_withPredefinedDescriptionOnly_ShouldSetRateDescriptionToPredefinedDescription() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String existingRatingId = rating1.getRatingId();

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(5.0)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("21/09/2023")
                .build();

        client.put()
                .uri("/vets/" + VET_ID + "/ratings/" + existingRatingId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ratingRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseDTO.class)
                .value(ratingResponseDTO -> {
                    assertNotNull(ratingResponseDTO);
                    assertNotNull(ratingResponseDTO.getRatingId());
                    assertThat(ratingResponseDTO.getVetId()).isEqualTo(ratingRequestDTO.getVetId());
                    assertThat(ratingResponseDTO.getRateScore()).isEqualTo(ratingRequestDTO.getRateScore());
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription().name());
                    assertThat(ratingResponseDTO.getPredefinedDescription()).isEqualTo(ratingRequestDTO.getPredefinedDescription());
                    assertThat(ratingResponseDTO.getRateDate()).isEqualTo(ratingRequestDTO.getRateDate());
                });
    }

    @Test
    void updateRating_withValidVetIdAndInvalidRatingId_shouldNotSucceed() {
        Publisher<Rating> setup1 = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup1)
                .expectNextCount(1)
                .verifyComplete();

        Publisher<Vet> setup2 = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup2)
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
                .jsonPath("$.message").isEqualTo("ratingId not found: "+invalidRatingId);
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
        Publisher<Vet> setup1 = vetRepository.deleteAll()
                .thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup1)
                .expectNextCount(1)
                .verifyComplete();

        Publisher<Rating> setup2 = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup2)
                .expectNextCount(1)
                .verifyComplete();

        client
                .delete()
                .uri("/vets/" + VET_ID + "/ratings/{ratingId}", rating1.getRatingId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteARatingForVet_WithInvalidVetId_ShouldNotSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll().
                thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String invalidVetId="123";

        client
                .delete()
                .uri("/vets/" + invalidVetId + "/ratings/{ratingId}", rating1.getRatingId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("vetId not found: "+invalidVetId);
    }

    @Test
    void deleteARatingForVet_WithInvalidRatingId_ShouldNotSucceed() {
        Publisher<Rating> setup1 = ratingRepository.deleteAll().
                thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup1)
                .expectNextCount(1)
                .verifyComplete();

        Publisher<Vet> setup2 = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

        StepVerifier
                .create(setup2)
                .expectNextCount(1)
                .verifyComplete();

        String invalidRatingId="123";

        client
                .delete()
                .uri("/vets/" + vet.getVetId() + "/ratings/{ratingId}", invalidRatingId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("ratingId not found: "+invalidRatingId);
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
    void getPercentageOfRatingsByVetId_ShouldSucceed(){
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1))
                .thenMany(ratingRepository.save(rating2));
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/vets/" + VET_ID + "/ratings" + "/percentages")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(String.class)
                .value(resp ->{
                    assertEquals("{\"1.0\":0.0,\"2.0\":0.0,\"3.0\":0.0,\"4.0\":0.5,\"5.0\":0.5}", resp);
                });
    }

    @Test
    void getPercentageOfRatingsByInvalidVetId_ShouldNotSucceed(){
        String invalidVetId="123";

        client.get()
                .uri("/vets/" + invalidVetId + "/ratings" + "/percentages")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("vetId not found: "+invalidVetId);

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
    void updateVet_withInvalidFirstName_shouldNotSucceed(){
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        VetDTO updatedVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementineeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("firstName length should be between 2 and 20 characters: "+updatedVet.getFirstName());
    }

    @Test
    void updateVet_withInvalidLastName_shouldNotSucceed(){
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        VetDTO updatedVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("lastName length should be between 2 and 20 characters: "+updatedVet.getLastName());
    }

    @Test
    void updateVet_withInvalidPhoneNumber_shouldNotSucceed() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        VetDTO updatedVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("phoneNumber length not equal to 20 characters: "+updatedVet.getPhoneNumber());
    }

    @Test
    void updateVet_withInvalidEmail_shouldNotSucceed() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String extensionNum="7654";
        VetDTO updatedVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.commmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("email length should be between 6 and 320 characters: "+updatedVet.getEmail());
    }

    @Test
    void updateVet_withInvalidResume_shouldNotSucceed() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String extensionNum="7920";
        VetDTO updatedVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Jo")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("resume length should be more than 10 characters: "+updatedVet.getResume());
    }

    @Test
    void updateVet_withInvalidSpecialties_shouldNotSucceed() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String extensionNum="7920";
        VetDTO updatedVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("I've been a vet ever since I was a kid.")
                .imageId("kjd")
                .workday("Monday")
                .specialties(null)
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("invalid specialties");
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
                .expectStatus().isCreated()
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
    void createVet_withInvalidPhoneNumber() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        VetDTO newVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("phoneNumber length not equal to 20 characters: "+newVet.getPhoneNumber());
    }

    @Test
    void createVet_withInvalidEmail() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        String extensionNum="4527";
        VetDTO newVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.commmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                        "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("email length should be between 6 and 320 characters: "+newVet.getEmail());

    }

    @Test
    void createVet_withInvalidFirstName() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        VetDTO newVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementineeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("firstName length should be between 2 and 30 characters: "+newVet.getFirstName());
    }

    @Test
    void createVet_withInvalidLastName() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        String extensionNum="0987";
        VetDTO newVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("lastName length should be between 2 and 30 characters: "+newVet.getLastName());
    }

    @Test
    void createVet_withInvalidResume() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        String extensionNum="4527";
        VetDTO newVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Jo")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("resume length should be more than 10 characters: "+newVet.getResume());

    }

    @Test
    void createVet_withInvalidSpecialties() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        String extensionNum="4527";
        VetDTO newVet=VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(null)
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), Vet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("invalid specialties");
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
                .expectStatus().isNoContent()
                .expectBody();
    }

    @Test
    void getAllEducationForAVet_WithValidId_ShouldSucceed(){
        Publisher<Education> setup = educationRepository.deleteAll()
                .thenMany(educationRepository.save(education1))
                .thenMany(educationRepository.save(education2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .get()
                .uri("/vets/" + vet.getVetId() + "/educations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(EducationResponseDTO.class)
                .value((list) -> {
                    assertEquals(2, list.size());
                    assertEquals(education1.getEducationId(), list.get(0).getEducationId());
                    assertEquals(education1.getVetId(), list.get(0).getVetId());
                    assertEquals(education1.getDegree(), list.get(0).getDegree());
                    assertEquals(education1.getFieldOfStudy(), list.get(0).getFieldOfStudy());
                    assertEquals(education1.getSchoolName(), list.get(0).getSchoolName());
                    assertEquals(education1.getStartDate(), list.get(0).getStartDate());
                    assertEquals(education1.getEndDate(), list.get(0).getEndDate());
                });
    }
    @Test
    void deleteAnEducationForVet_WithValidId_ShouldSucceed() {
        Publisher<Education> setup = educationRepository.deleteAll().
                thenMany(educationRepository.save(education1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client
                .delete()
                .uri("/vets/" + vet.getVetId() + "/educations/{educationId}", education1.getEducationId())
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

    //the extension number can only be 4 digits
    private Vet buildVet(String extensionNum) {
        return Vet.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .workday("Monday")
                .imageId("kjd")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }

    //the extension number can only be 4 digits
    private Vet buildVet2(String extensionNum) {
        return Vet.builder()
                .vetId("678910")
                .vetBillId("2")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
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

    private Education buildEducation(){
        return Education.builder()
                .educationId("1")
                .vetId("678910")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();
    }

    private Education buildEducation2(){
        return  Education.builder()
                .educationId("2")
                .vetId("678910")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Veterinary Sciences")
                .startDate("2008")
                .endDate("2013")
                .build();
    }

    //the extension number can only be 4 digits
    private VetDTO buildVetDTO(String extensionNum) {
        return VetDTO.builder()
                .vetId("678910")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }


}
