package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.photos.Photo;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.presentationlayer.vets.SpecialtyDTO;
import com.petclinic.vet.presentationlayer.vets.VetAverageRatingDTO;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.presentationlayer.vets.VetResponseDTO;
import com.petclinic.vet.presentationlayer.education.EducationRequestDTO;
import com.petclinic.vet.presentationlayer.education.EducationResponseDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingRequestDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingResponseDTO;
import com.petclinic.vet.utils.EntityDtoUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port=0"})
@ActiveProfiles("test")
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

    //To counter missing bean error
    @Autowired
    PhotoRepository photoRepository;
    @Autowired
    BadgeRepository badgeRepository;
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    Education education1 = buildEducation();
    Education education2 = buildEducation2();
    Vet vet = buildVet("1234");
    Vet vet2 = buildVet2("2345");

    Rating rating1 = buildRating("12345", "db0c8f13-89d2-4ef7-bcd5-3776a3734150", 5.0,"2023");
    Rating rating2 = buildRating("12346", "db0c8f13-89d2-4ef7-bcd5-3776a3734150", 4.0,"2022");
    Rating rating3 = buildRating("12347", "db0c8f13-89d2-4ef7-bcd5-3776a3734150", 3.0,"2024");


    VetResponseDTO vetResponseDTO = buildVetResponseDTO("3456");
    VetRequestDTO vetRequestDTO = buildVetRequestDTO("3456");

    String VET_ID = "db0c8f13-89d2-4ef7-bcd5-3776a3734150";
    String VET_BILL_ID = vet.getVetBillId();
    String INVALID_VET_ID = "mjbedf";
    String NON_EXISTING_VET_ID = "ab1u0l25-90a3-5hj1-asd9-8695h4157881";

    RatingRequestDTO updatedRating = RatingRequestDTO.builder()
            .rateScore(2.0)
            .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
            .rateDescription("Vet cancelled last minute.")
            .rateDate("20/09/2023")
            .build();
    EducationRequestDTO updatedEducation = EducationRequestDTO.builder()
            .schoolName("McGill")
            .vetId("678910")
            .degree("Bachelor of Medicine")
            .fieldOfStudy("Medicine")
            .startDate("2010")
            .endDate("2015")
            .build();

    //badge image
    ClassPathResource cpr = new ClassPathResource("images/full_food_bowl.png");


    @BeforeEach
    public void setup() {
        Mono<Void> clean = badgeRepository.deleteAll()
                .then(photoRepository.deleteAll())
                .then(ratingRepository.deleteAll())
                .then(educationRepository.deleteAll())
                .then(vetRepository.deleteAll());

        clean.block();
    }

    @Test
    void getAllRatingsForAVet_WithValidVetId_ShouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .then(vetRepository.save(vet))
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
        String invalidVetId="ac90fcca-a79c-411d-93f2-b70a80da0c3a";
        client
                .get()
                .uri("/vets/" + invalidVetId + "/ratings")
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
                .then(vetRepository.save(vet))
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
        Publisher<Vet> setup = vetRepository.deleteAll()
                .then(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

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
    void addRatingWithWrittenDescriptionAndPredefinedValue_ShouldKeepWrittenDescription(){
        Publisher<Vet> setup = vetRepository.deleteAll()
                .then(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

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
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(ratingRequestDTO.getRateDescription());
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
        Publisher<Vet> setup = vetRepository.deleteAll()
                .then(vetRepository.save(vet));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

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
    void updateRating_withValidVetIdAndValidRatingId_shouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .then(vetRepository.save(vet))
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
                .then(vetRepository.save(vet))
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
    void updateRating_withWrittenDescriptionAndPredefinedValue_ShouldKeepWrittenDescription() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .then(vetRepository.save(vet))
                .thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String existingRatingId = rating1.getRatingId();

        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(5.0)
                .rateDescription("Follow-up visit was outstanding.")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("22/09/2023")
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
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(ratingRequestDTO.getRateDescription());
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
                .then(vetRepository.save(vet))
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
    void getAverageRatingByVetId_withInvalidVetId_ShouldThrowNumberZero() {


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
    void getRatingBasedOnYearDate_ShouldSucceed() {
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .thenMany(ratingRepository.save(rating1))
                .thenMany(ratingRepository.save(rating2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String existingDate = "2023";



        RatingResponseDTO ratingWithDate = RatingResponseDTO.builder()
                .date(rating1.getDate())
                .ratingId(rating1.getRatingId())
                .vetId(VET_ID)
                .date(existingDate)
                .rateScore(rating1.getRateScore())
                .rateDescription(rating1.getRateDescription())
                .predefinedDescription(rating1.getPredefinedDescription())
                .build();

        client
                .get()
                .uri("/vets/"+VET_ID+"/ratings/date?year={year}",existingDate)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(RatingResponseDTO.class)
                .value((list) -> {
                    assertEquals(2, list.size());
                    assertEquals(rating1.getRatingId(),list.get(0).getRatingId());
                    assertEquals(rating1.getVetId(),list.get(0).getVetId());
                    assertEquals(rating1.getRateScore(),list.get(0).getRateScore());
                    assertEquals(rating1.getRateDate(),list.get(0).getRateDate());
                    assertEquals(rating1.getRateDescription(),list.get(0).getRateDescription());
                    assertEquals(existingDate, list.get(0).getDate());
                });
    }

    @Test
    void getRatingBasedOnYearDateWithInvalidVetID_ShouldFail() {

        Publisher<Rating> setup = ratingRepository.deleteAll().
                thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String invalidVetId="123";
        String existingDate = "2023";

        client
                .get()
                .uri("/vets/"+invalidVetId+"/ratings/date?year={year}",existingDate)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("No valid ratings were found for "+invalidVetId);

    }


    @Test
    void getRatingBasedOnYearDateWithInvalidYear_ShouldFail() {

        Publisher<Rating> setup = ratingRepository.deleteAll().
                thenMany(ratingRepository.save(rating1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String invalidYear="203q";

        client
                .get()
                .uri("/vets/"+VET_ID+"/ratings/date?year="+ invalidYear)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid year format. Please enter a valid year.");

    }


    @Test
    void getTopThreeVetWithTheHighestRating_ShouldSucceed(){

        VetAverageRatingDTO vetAverageRatingDTO1 = VetAverageRatingDTO.builder()
                .averageRating(rating1.getRateScore())
                .vetId(vet.getVetId())
                .build();

        VetAverageRatingDTO vetAverageRatingDTO2 = VetAverageRatingDTO.builder()
                .averageRating(rating2.getRateScore())
                .vetId(vet.getVetId())
                .build();

        VetAverageRatingDTO vetAverageRatingDTO3 = VetAverageRatingDTO.builder()
                .averageRating(rating3.getRateScore())
                .vetId(vet.getVetId())
                .build();

        client
                .get()
                .uri("/vets/" + "topVets")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(VetAverageRatingDTO.class)
                .value(resp -> {
                    assertEquals(rating1.getVetId(), vetAverageRatingDTO1.getVetId());
                    assertEquals(rating2.getVetId(), vetAverageRatingDTO2.getVetId());
                    assertEquals(rating3.getVetId(), vetAverageRatingDTO3.getVetId());
                    assertEquals(rating1.getRateScore(), vetAverageRatingDTO1.getAverageRating());
                    assertEquals(rating2.getRateScore(), vetAverageRatingDTO2.getAverageRating());
                    assertEquals(rating3.getRateScore(), vetAverageRatingDTO3.getAverageRating());


                });
    }


    @Test
    void getPercentageOfRatingsByVetId_ShouldSucceed(){
        Publisher<Rating> setup = ratingRepository.deleteAll()
                .then(vetRepository.save(vet))
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
        String invalidVetId="ac90fcca-a79c-411d-93f2-b70a80da0c3a";

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
                .uri("/vets")
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
                .jsonPath("$[0].active").isEqualTo(vet.isActive())
                .jsonPath("$[0].workHoursJson").isEqualTo(vet.getWorkHoursJson());
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
                .jsonPath("$.workHoursJson").isEqualTo(vet.getWorkHoursJson());

    }

    @Test
    void getVetByVetBillId() {
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));

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
                .jsonPath("$.active").isEqualTo(vet.isActive());

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
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vetRequestDTO.getVetId())
                .jsonPath("$.resume").isEqualTo(vetRequestDTO.getResume())
                .jsonPath("$.lastName").isEqualTo(vetRequestDTO.getLastName())
                .jsonPath("$.firstName").isEqualTo(vetRequestDTO.getFirstName())
                .jsonPath("$.email").isEqualTo(vetRequestDTO.getEmail())
                .jsonPath("$.active").isEqualTo(vetRequestDTO.isActive());

    }

    @Test
    void updateVet_withInvalidFirstName_shouldNotSucceed(){
        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet2));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        VetRequestDTO updatedVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementineeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetRequestDTO.class)
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

        VetRequestDTO updatedVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetRequestDTO.class)
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

        VetRequestDTO updatedVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetRequestDTO.class)
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
        VetRequestDTO updatedVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
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
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetRequestDTO.class)
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
        VetRequestDTO updatedVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Jo")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetRequestDTO.class)
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
        VetRequestDTO updatedVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("I've been a vet ever since I was a kid.")
                .workday(new HashSet<>())
                .specialties(null)
                .active(false)
                .build();

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(updatedVet), VetRequestDTO.class)
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
                .jsonPath("$[0].active").isEqualTo(vet2.isActive())
                .jsonPath("$[0].workHoursJson").isEqualTo(vet2.getWorkHoursJson());
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
                .jsonPath("$[0].active").isEqualTo(vet.isActive())
                .jsonPath("$[0].workHoursJson").isEqualTo(vet.getWorkHoursJson());
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
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VetResponseDTO.class)
                .value((dto) -> {
                    assertThat(dto.getFirstName()).isEqualTo(vetResponseDTO.getFirstName());
                    assertThat(dto.getLastName()).isEqualTo(vetResponseDTO.getLastName());
                    assertThat(dto.getPhoneNumber()).isEqualTo(vetResponseDTO.getPhoneNumber());
                    assertThat(dto.getResume()).isEqualTo(vetResponseDTO.getResume());
                    assertThat(dto.getEmail()).isEqualTo(vetResponseDTO.getEmail());
                    assertThat(dto.getWorkday()).isEqualTo(vetResponseDTO.getWorkday());
                    assertThat(dto.isActive()).isEqualTo(vetResponseDTO.isActive());
                    assertThat(dto.getSpecialties()).isEqualTo(vetResponseDTO.getSpecialties());
                });
    }

    @Test
    void createVet_withInvalidPhoneNumber() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        VetRequestDTO newVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), VetRequestDTO.class)
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
        VetRequestDTO newVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
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
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("email length should be between 6 and 320 characters: "+newVet.getEmail());

    }

    /*
    @Test
    void createVet_withInvalidFirstName() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        VetRequestDTO newVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementineeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-28479")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("firstName length should be between 2 and 30 characters: "+newVet.getFirstName());
    }*/

    /*
    @Test
    void createVet_withInvalidLastName() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        String extensionNum="0987";
        VetRequestDTO newVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("lastName length should be between 2 and 30 characters: "+newVet.getLastName());
    }*/

    @Test
    void createVet_withInvalidResume() {
        Publisher<Void> setup = vetRepository.deleteAll();

        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();

        String extensionNum="4527";
        VetRequestDTO newVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Jo")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), VetRequestDTO.class)
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
        VetRequestDTO newVet = VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(null)
                .active(false)
                .build();

        client
                .post()
                .uri("/vets")
                .body(Mono.just(newVet), VetRequestDTO.class)
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
                .expectStatus().isOk()
                .expectBody();
    }

    @Test
    void deleteVetById_WithNonExistingValidId_ShouldReturnNotFound() {
        client.delete()
                .uri("/vets/" + NON_EXISTING_VET_ID)
                .exchange()
                // Then the response should have a 404 Not Found status
                .expectStatus().isNotFound()
                // And the body should contain the expected error message
                .expectBody()
                .jsonPath("$.message").isEqualTo("No vet with this vetId was found: " + NON_EXISTING_VET_ID);
    }

    @Test
    void deleteVetById_WithInvalidId_ShouldReturnUnprocessableEntity() {
        client.delete()
                .uri("/vets/" + INVALID_VET_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");
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
        // Setup: Clear repository and save a new education
        Publisher<Education> setup = educationRepository.deleteAll()
                .thenMany(educationRepository.save(education1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        // Execute: Send DELETE request
        client
                .delete()
                .uri("/vets/" + vet.getVetId() + "/educations/{educationId}", education1.getEducationId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent() // Expect 204 No Content
                .expectBody().isEmpty(); // Expect empty body
    }

    @Test
    void updateEducation_withValidVetIdAndValidEducationId_shouldSucceed() {
        Publisher<Education> setup = educationRepository.deleteAll()
                .then(vetRepository.save(vet))
                .thenMany(educationRepository.save(education1));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        String existingEducationId = education1.getEducationId();

        client.put()
                .uri("/vets/" + VET_ID + "/educations/" + existingEducationId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedEducation)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EducationResponseDTO.class)
                .value(educationResponseDTO -> {
                    assertNotNull(educationResponseDTO);
                    assertNotNull(educationResponseDTO.getEducationId());
                    assertThat(educationResponseDTO.getEducationId()).isEqualTo(existingEducationId);
                    assertThat(educationResponseDTO.getVetId()).isEqualTo(updatedEducation.getVetId());
                    assertThat(educationResponseDTO.getSchoolName()).isEqualTo(updatedEducation.getSchoolName());
                    assertThat(educationResponseDTO.getDegree()).isEqualTo(updatedEducation.getDegree());
                    assertThat(educationResponseDTO.getFieldOfStudy()).isEqualTo(updatedEducation.getFieldOfStudy());
                    assertThat(educationResponseDTO.getStartDate()).isEqualTo(updatedEducation.getStartDate());
                    assertThat(educationResponseDTO.getEndDate()).isEqualTo(updatedEducation.getEndDate());
                });
    }

//    @Test
//    void addEducationToAVet_WithValidValues_shouldSucceed(){
//        Publisher<Education> setup = educationRepository.deleteAll()
//                .thenMany(educationRepository.save(education1));
//
//        StepVerifier
//                .create(setup)
//                .expectNextCount(1)
//                .verifyComplete();
//
//        client.post()
//                .uri("/vets/" + vet.getVetId() + "/educations")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(education2)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody(EducationResponseDTO.class)
//                .value(dto -> {
//                    assertNotNull(dto);
//                    assertNotNull(dto.getEducationId());
//                    assertThat(dto.getVetId()).isEqualTo(education2.getVetId());
//                    assertThat(dto.getDegree()).isEqualTo(education2.getDegree());
//                    assertThat(dto.getFieldOfStudy()).isEqualTo(education2.getFieldOfStudy());
//                    assertThat(dto.getSchoolName()).isEqualTo(education2.getSchoolName());
//                    assertThat(dto.getStartDate()).isEqualTo(education2.getStartDate());
//                    assertThat(dto.getEndDate()).isEqualTo(education2.getEndDate());
//                });
//    }

    //Spring Boot version incompatibility issue with postgresql r2dbc
    /*@Test
    void getPhotoByVetId() {
        Publisher<Photo> setup = photoRepository.deleteAll()
                .thenMany(photoRepository.save(buildPhoto()));
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        byte[] photo = {123, 23, 75, 34};
        Resource resource = new ByteArrayResource(photo);

        client.get()
                .uri("/api/gateway/vets/{vetId}/photo", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE)
                .expectBody(Resource.class)
                .consumeWith(response -> {
                    assertEquals(resource, response.getResponseBody());
                });
    }*/

    @Test
    void getPhotoByVetId_NoExistingPhoto_ShouldReturnNotFound() {
        String emptyVetId = "1234567";
        client.get()
                .uri("/api/gateway/vets/{vetId}/photo", emptyVetId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/api/gateway/vets/" + emptyVetId + "/photo");
    }

    //Spring Boot version incompatibility issue with postgresql r2dbc
    /*@Test
    void getBadgeByVetId_shouldSucceed() throws IOException {
        Badge badge=buildBadge();

        Publisher<Badge> setup=badgeRepository.deleteAll()
                .thenMany(badgeRepository.save(badge));

        StepVerifier
                .create(badgeRepository.deleteAll()
                        .then(badgeRepository.save(badge)))
                .expectNext(badge)  // Expect the saved badge
                .verifyComplete();

        client.get()
                .uri("/api/gateway/vets/{vetId}/badge", VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BadgeResponseDTO.class)
                .value(responseDTO -> {
                    assertEquals(badge.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badge.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badge.getVetId(), responseDTO.getVetId());
                    assertEquals(Base64.getEncoder().encodeToString(badge.getData()), responseDTO.getResourceBase64());
                });
    }*/

    @Test
    void getBadgeByInvalidVetId_shouldReturnNotFoundException(){
        String invalidVetId = "1234567";
        client.get()
                .uri("/vets/{vetId}/badge", invalidVetId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("vetId not found: "+invalidVetId);
    }

    @Test
    void generateVetId(){
        String vetId = EntityDtoUtil.generateVetId();
        assertEquals(vetId.length(), 36);
    }

    @Test
    void toStringBuilders() {
        System.out.println(Vet.builder());
        System.out.println(VetResponseDTO.builder());
        System.out.println(VetRequestDTO.builder());
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
                .jsonPath("$.message").isEqualTo("Provided vet id is invalid:mjbedf"); // Update this line to match the actual message
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
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
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
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("ac90fcca-a79c-411d-93f2-b70a80da0c3a")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }

    //the extension number can only be 4 digits
    private Vet buildVet2(String extensionNum) {
        return Vet.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("2")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
                .specialties(new HashSet<>())
                .active(true)
                .build();
    }

    private Rating buildRating(String ratingId, String vetId, Double rateScore,String date) {
        return Rating.builder()
                .ratingId(ratingId)
                .vetId(vetId)
                .rateScore(rateScore)
                .date("2023")
                .build();
    }

    private Education buildEducation(){
        return Education.builder()
                .educationId("1")
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
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
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Veterinary Sciences")
                .startDate("2008")
                .endDate("2013")
                .build();
    }

    //the extension number can only be 4 digits
    private VetResponseDTO buildVetResponseDTO(String extensionNum) {
        return VetResponseDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("ac90fcca-a79c-411d-93f2-b70a80da0c3a")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private VetRequestDTO buildVetRequestDTO(String extensionNum) {
        return VetRequestDTO.builder()
                .vetId("db0c8f13-89d2-4ef7-bcd5-3776a3734150")
                .vetBillId("ac90fcca-a79c-411d-93f2-b70a80da0c3a")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("(514)-634-8276 #"+extensionNum)
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }

    private Photo buildPhoto(){
        byte[] photo = {123, 23, 75, 34};
        return Photo.builder()
                .vetId(VET_ID)
                .filename("vet_default.jpg")
                .imgType("image/jpeg")
                .data(photo)
                .build();
    }

    private Badge buildBadge() throws IOException {
        return Badge.builder()
                .vetId(VET_ID)
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2017")
                .data(StreamUtils.copyToByteArray(cpr.getInputStream()))
                .build();
    }

    SpecialtyDTO specialtyDTO = SpecialtyDTO.builder()
            .specialtyId("specialty123")
            .name("Cardiology")
            .build();

    SpecialtyDTO invalidSpecialtyDTO = SpecialtyDTO.builder()
            .specialtyId(null) // Invalid specialty ID
            .name(null)        // Invalid specialty name
            .build();

    @Test
    void addSpecialtyToVet_WithValidVetId_ShouldSucceed() {
        Vet vet = buildVet("1234");

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));
        StepVerifier.create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.post()
                .uri("/vets/" + vet.getVetId() + "/specialties")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(specialtyDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VetResponseDTO.class)
                .value(responseDTO -> {
                    assertNotNull(responseDTO);
                    assertEquals(1, responseDTO.getSpecialties().size());
                    assertEquals("Cardiology",specialtyDTO.getName());
                });
    }

    @Test
    void addSpecialtyToVet_WithInvalidVetId_ShouldReturnNotFound() {
        String invalidVetId = "invalid-vet-id";

        client.post()
                .uri("/vets/" + invalidVetId + "/specialties")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(specialtyDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Vet not found with id: " + invalidVetId);
    }

    @Test
    void deleteSpecialtyFromVet_WithValidVetIdAndSpecialtyId_ShouldSucceed() {
        Vet vet = buildVet("1234");

        Publisher<Vet> setup = vetRepository.deleteAll().thenMany(vetRepository.save(vet));
        StepVerifier.create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.delete()
                .uri("/vets/" + vet.getVetId() + "/specialties/" + specialtyDTO.getSpecialtyId())
                .exchange()
                .expectStatus().isNoContent() // Expecting a 204 No Content status
                .expectBody().isEmpty(); // No content expected in the response body
    }

    @Test
    void deleteSpecialtyFromVet_WithInvalidVetId_ShouldReturnNotFound() {
        String invalidVetId = "invalid-vet";

        client.delete()
                .uri("/vets/" + invalidVetId + "/specialties/" + specialtyDTO.getSpecialtyId())
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("No vet found with vetId: " + invalidVetId);
    }

}
