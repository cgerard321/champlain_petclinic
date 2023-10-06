package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.servicelayer.*;
import com.petclinic.vet.servicelayer.badges.BadgeResponseDTO;
import com.petclinic.vet.servicelayer.badges.BadgeService;
import com.petclinic.vet.servicelayer.education.EducationRequestDTO;
import com.petclinic.vet.servicelayer.education.EducationResponseDTO;
import com.petclinic.vet.servicelayer.education.EducationService;
import com.petclinic.vet.servicelayer.ratings.RatingRequestDTO;
import com.petclinic.vet.servicelayer.ratings.RatingResponseDTO;
import com.petclinic.vet.servicelayer.ratings.RatingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers=VetController.class)
@ContextConfiguration(classes = {VetController.class})
class VetControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    VetService vetService;
    @MockBean
    RatingService ratingService;
    @MockBean
    EducationService educationService;
    @MockBean
    PhotoService photoService;
    @MockBean
    BadgeService badgeService;
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;

    VetRequestDTO vetRequestDTO = buildVetRequestDTO();
    VetResponseDTO vetResponseDTO = buildVetResponseDTO();
    VetResponseDTO vetResponseDTO2 = buildVetResponseDTO2();


    VetAverageRatingDTO averageRatingDTO=buildVetAverageRatingDTO1();
    VetAverageRatingDTO averageRatingDTO2=buildVetAverageRatingDTO2();
    VetAverageRatingDTO averageRatingDTO3=buildVetAverageRatingDTO3();


    EducationResponseDTO educationResponseDTO1 = buildEducation();

    Photo photo = buildPhoto();


    RatingResponseDTO ratingDTO = buildRatingResponseDTO("Vet was super calming with my pet",5.0);

    Vet vet = buildVet();
    String VET_ID = vet.getVetId();
    String VET_BILL_ID = vet.getVetBillId();
    String INVALID_VET_ID = "mjbedf";

    ClassPathResource cpr=new ClassPathResource("images/full_food_bowl.png");

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
                .expectStatus().isNoContent();

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

        when(ratingService.addRatingToVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(rating));

        client.post()
                .uri("/vets/{vetId}/ratings", VET_ID)
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(ratingService, times(1))
                .addRatingToVet(anyString(), any(Mono.class));
    }

    @Test
    void updateRatingWithValidVetIdAndValidRatingId_withValidValues_shouldSucceed(){
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId(VET_ID)
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        RatingResponseDTO ratingResponse = RatingResponseDTO.builder()
                .ratingId("2")
                .rateScore(2.0)
                .vetId(VET_ID)
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        when(ratingService.updateRatingByVetIdAndRatingId(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponse));

        client.put()
                .uri("/vets/"+VET_ID+"/ratings/"+ratingResponse.getRatingId())
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
                    assertThat(ratingResponseDTO.getRatingId()).isEqualTo(ratingResponse.getRatingId());
                    assertThat(ratingResponseDTO.getVetId()).isEqualTo(updatedRating.getVetId());
                    assertThat(ratingResponseDTO.getRateScore()).isEqualTo(updatedRating.getRateScore());
                    assertThat(ratingResponseDTO.getRateDescription()).isEqualTo(updatedRating.getRateDescription());
                    assertThat(ratingResponseDTO.getRateDate()).isEqualTo(updatedRating.getRateDate());
                });

        Mockito.verify(ratingService, times(1))
                .updateRatingByVetIdAndRatingId(anyString(), anyString(), any(Mono.class));
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
        String vetId = "cf25e779-548b-4788-aefa-6d58621c2feb";
        Double averageRating = 5.0;

        when(vetService.getVetByVetId(vetId))
                .thenReturn(Mono.just(vetResponseDTO));

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
    void getTopThreeVetsWithTheHighestRating() {
        when(ratingService.getTopThreeVetsWithHighestAverageRating())
                .thenReturn(Flux.just(averageRatingDTO, averageRatingDTO2, averageRatingDTO3));

        client.get()
                .uri("/vets/topVets")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(VetAverageRatingDTO.class)
                .value(vetDTOs -> {
                    assertEquals(3, vetDTOs.size());
                    assertEquals(averageRatingDTO, vetDTOs.get(0));
                    assertEquals(averageRatingDTO2, vetDTOs.get(1));
                    assertEquals(averageRatingDTO3, vetDTOs.get(2));
                });

        Mockito.verify(ratingService, times(1))
                .getTopThreeVetsWithHighestAverageRating();

    }

    @Test
    void getPercentageOfRatingsByVetId_ShouldSucceed() {
        when(ratingService.getRatingPercentagesByVetId(anyString()))
                .thenReturn(Mono.just("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}"));
        client
                .get()
                .uri("/vets/" + VET_ID + "/ratings/percentages")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(string -> {
                    assertEquals("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}", string);
                });
        Mockito.verify(ratingService, times(1))
                .getRatingPercentagesByVetId(VET_ID);
    }

    @Test
    void getAllVets() {
        when(vetService.getAll())
                .thenReturn(Flux.just(vetResponseDTO));

        client
                .get()
                .uri("/vets")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetResponseDTO.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetResponseDTO.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetResponseDTO.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetResponseDTO.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetResponseDTO.getEmail())
                .jsonPath("$[0].active").isEqualTo(vetResponseDTO.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetResponseDTO.getWorkday());

        Mockito.verify(vetService, times(1))
                .getAll();
    }

    @Test
    void getVetByVetId() {
        when(vetService.getVetByVetId(anyString()))
                .thenReturn(Mono.just(vetResponseDTO));

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
                .jsonPath("$.workday").isEqualTo(vet.getWorkday());

        Mockito.verify(vetService, times(1))
                .getVetByVetId(VET_ID);
    }

    @Test
    void getVetByVetBillId() {
        when(vetService.getVetByVetBillId(anyString()))
                .thenReturn(Mono.just(vetResponseDTO));

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

        Mockito.verify(vetService, times(1))
                .getVetByVetBillId(VET_BILL_ID);
    }

    @Test
    void getActiveVets() {
        when(vetService.getVetByIsActive(anyBoolean()))
                .thenReturn(Flux.just(vetResponseDTO2));

        client
                .get()
                .uri("/vets/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetResponseDTO2.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetResponseDTO2.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetResponseDTO2.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetResponseDTO2.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetResponseDTO2.getEmail())
                .jsonPath("$[0].active").isEqualTo(vetResponseDTO2.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetResponseDTO2.getWorkday());

        Mockito.verify(vetService, times(1))
                .getVetByIsActive(vetResponseDTO2.isActive());
    }

    @Test
    void createVet() {
        when(vetService.insertVet(any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

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

        Mockito.verify(vetService, times(1))
                .insertVet(any(Mono.class));
    }

    @Test
    void updateVet() {
        when(vetService.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vetResponseDTO.getVetId())
                .jsonPath("$.resume").isEqualTo(vetResponseDTO.getResume())
                .jsonPath("$.lastName").isEqualTo(vetResponseDTO.getLastName())
                .jsonPath("$.firstName").isEqualTo(vetResponseDTO.getFirstName())
                .jsonPath("$.email").isEqualTo(vetResponseDTO.getEmail())
                .jsonPath("$.active").isEqualTo(vetResponseDTO.isActive())
                .jsonPath("$.workday").isEqualTo(vetResponseDTO.getWorkday());

        Mockito.verify(vetService, times(1))
                .updateVet(anyString(), any(Mono.class));
    }

    @Test
    void getInactiveVets() {
        when(vetService.getVetByIsActive(anyBoolean()))
                .thenReturn(Flux.just(vetResponseDTO));

        client
                .get()
                .uri("/vets/inactive")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetResponseDTO.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetResponseDTO.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetResponseDTO.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetResponseDTO.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetResponseDTO.getEmail())
                .jsonPath("$[0].active").isEqualTo(vetResponseDTO.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetResponseDTO.getWorkday());

        Mockito.verify(vetService, times(1))
                .getVetByIsActive(vetResponseDTO.isActive());
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
                .expectStatus().isNoContent();

        Mockito.verify(vetService, times(1))
                .deleteVetByVetId(VET_ID);
    }

    @Test
    void getByVetId_Invalid() {
        when(vetService.getVetByVetId(anyString()))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .get()
                .uri("/vets/" + INVALID_VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isNotEmpty();
    }

    @Test
    void updateByVetId_Invalid() {
        when(vetService.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .put()
                .uri("/vets/" + INVALID_VET_ID)
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isNotEmpty();

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
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isNotEmpty();

    }

    @Test
    void getAllEducationForVetByVetId_ShouldSucceed() {
        when(educationService.getAllEducationsByVetId(anyString()))
                .thenReturn(Flux.just(educationResponseDTO1));

        client
                .get()
                .uri("/vets/" + VET_ID + "/educations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].educationId").isEqualTo(educationResponseDTO1.getEducationId())
                .jsonPath("$[0].vetId").isEqualTo(educationResponseDTO1.getVetId())
                .jsonPath("$[0].degree").isEqualTo(educationResponseDTO1.getDegree())
                .jsonPath("$[0].fieldOfStudy").isEqualTo(educationResponseDTO1.getFieldOfStudy())
                .jsonPath("$[0].schoolName").isEqualTo(educationResponseDTO1.getSchoolName())
                .jsonPath("$[0].startDate").isEqualTo(educationResponseDTO1.getStartDate())
                .jsonPath("$[0].endDate").isEqualTo(educationResponseDTO1.getEndDate());

        Mockito.verify(educationService, times(1)).getAllEducationsByVetId(VET_ID);
    }

    @Test
    void deleteEducationForVetByEducationId_ShouldSucceed() {
        String educationId = "794ac37f-1e07-43c2-93bc-61839e61d989";
        String vetId = "694ac37f-1e07-43c2-93bc-61839e61d989";

        when(educationService.deleteEducationByEducationId(vetId,educationId))
                .thenReturn((Mono.empty()));

        client
                .delete()
                .uri("/vets/" + vetId + "/educations/{educationId}", educationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(educationService, times(1))
                .deleteEducationByEducationId(vetId,educationId);
    }
    @Test
    void updateEducationWithValidVetIdAndValidEducationId_shouldSucceed(){
        EducationRequestDTO updatedEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        EducationResponseDTO educationResponse = EducationResponseDTO.builder()
                .educationId("2")
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        when(educationService.updateEducationByVetIdAndEducationId(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(educationResponse));

        client.put()
                .uri("/vets/"+VET_ID+"/educations/"+educationResponse.getEducationId())
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
                    assertThat(educationResponseDTO.getEducationId()).isEqualTo(educationResponse.getEducationId());
                    assertThat(educationResponseDTO.getVetId()).isEqualTo(updatedEducation.getVetId());
                    assertThat(educationResponseDTO.getSchoolName()).isEqualTo(updatedEducation.getSchoolName());
                    assertThat(educationResponseDTO.getDegree()).isEqualTo(updatedEducation.getDegree());
                    assertThat(educationResponseDTO.getFieldOfStudy()).isEqualTo(updatedEducation.getFieldOfStudy());
                    assertThat(educationResponseDTO.getStartDate()).isEqualTo(updatedEducation.getStartDate());
                    assertThat(educationResponseDTO.getEndDate()).isEqualTo(updatedEducation.getEndDate());
                });

        Mockito.verify(educationService, times(1))
                .updateEducationByVetIdAndEducationId(anyString(), anyString(), any(Mono.class));
    }


    @Test
    void addEducationWithVetId_ValidValues_ShouldSucceed(){
        EducationRequestDTO educationRequestDTO = EducationRequestDTO.builder()
                .vetId(VET_ID)
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();

        EducationResponseDTO educationResponseDTO = EducationResponseDTO.builder()
                .educationId("3")
                .vetId(VET_ID)
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();

        when(educationService.addEducationToVet(VET_ID, Mono.just(educationRequestDTO))).thenReturn(Mono.just(educationResponseDTO));

        client.post()
                .uri("/vets/{vetId}/educations", VET_ID)
                .bodyValue(educationRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(educationService, times(1)).addEducationToVet(anyString(), any(Mono.class));
    }

    @Test
    void getPhotoByVetId() {
        when(photoService.getPhotoByVetId(anyString()))
                .thenReturn(Mono.just(buildPhotoData()));

        client.get()
                .uri("/vets/{vetId}/photo", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE)
                .expectBody(Resource.class)
                .consumeWith(response -> {
                    assertEquals(buildPhotoData(), response.getResponseBody());
                });

        Mockito.verify(photoService, times(1))
                .getPhotoByVetId(VET_ID);
    }

    // test add photo
    @Test
    void addPhotoByVetId() {
        Photo photo = buildPhoto();
        Resource photoResource = buildPhotoData(photo);

        when(photoService.insertPhotoOfVet(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(photoResource));

        client.post()
                .uri("/vets/{vetId}/photos/{photoName}", VET_ID, photo.getFilename())
                .bodyValue(photoResource) // Use the Resource here
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(photoService, times(1))
                .insertPhotoOfVet(anyString(), anyString(), any(Mono.class));
    }

    @Test
    void getBadgeByVetId_shouldSucceed() throws IOException {
        BadgeResponseDTO badgeResponseDTO = buildBadgeResponseDTO();

        when(badgeService.getBadgeByVetId(anyString()))
                .thenReturn(Mono.just(badgeResponseDTO));

        client.get()
                .uri("/vets/{vetId}/badge", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BadgeResponseDTO.class)
                .value(responseDTO -> {
                    assertEquals(badgeResponseDTO.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badgeResponseDTO.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badgeResponseDTO.getVetId(), responseDTO.getVetId());
                    assertEquals(badgeResponseDTO.getResourceBase64(), responseDTO.getResourceBase64());
                });
    }

    private Resource buildPhotoData(Photo photo) {
        ByteArrayResource resource = new ByteArrayResource(photo.getData());
        return resource;
    }

    private BadgeResponseDTO buildBadgeResponseDTO() throws IOException {
        return BadgeResponseDTO.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2017")
                .resourceBase64(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(cpr.getInputStream())))
                .build();
    }

    private Vet buildVet() {
        return Vet.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .vetBillId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }

    private VetRequestDTO buildVetRequestDTO() {
        return VetRequestDTO.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .vetBillId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private VetResponseDTO buildVetResponseDTO() {
        return VetResponseDTO.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .vetBillId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }
    private VetResponseDTO buildVetResponseDTO2() {
        return VetResponseDTO.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .vetBillId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }

    private EducationResponseDTO buildEducation(){
        return EducationResponseDTO.builder()
                .educationId("1")
                .vetId(VET_ID)
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();
    }

    private RatingResponseDTO buildRatingResponseDTO(String description, double score) {
        return RatingResponseDTO.builder()
                .ratingId("2")
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .rateScore(score)
                .rateDescription(description)
                .rateDate("16/09/2023")
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

    private Resource buildPhotoData(){
        ByteArrayResource resource = new ByteArrayResource(photo.getData());
        return resource;
    }

    private VetAverageRatingDTO buildVetAverageRatingDTO1(){
        return  VetAverageRatingDTO.builder()
                .averageRating(2.0)
                .vetId("20381")
                .build();
    }

    private VetAverageRatingDTO buildVetAverageRatingDTO2(){
        return  VetAverageRatingDTO.builder()
                .averageRating(4.0)
                .vetId("28179")
                .build();
    }

    private VetAverageRatingDTO buildVetAverageRatingDTO3(){
        return  VetAverageRatingDTO.builder()
                .averageRating(1.0)
                .vetId("92739")
                .build();
    }
}
