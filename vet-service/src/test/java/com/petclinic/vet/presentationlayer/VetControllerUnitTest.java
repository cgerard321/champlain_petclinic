package com.petclinic.vet.presentationlayer;

import com.petclinic.vet.businesslayer.albums.AlbumService;
import com.petclinic.vet.businesslayer.badges.BadgeService;
import com.petclinic.vet.businesslayer.education.EducationService;
import com.petclinic.vet.businesslayer.photos.PhotoService;
import com.petclinic.vet.businesslayer.ratings.RatingService;
import com.petclinic.vet.businesslayer.vets.VetService;
import com.petclinic.vet.dataaccesslayer.albums.Album;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.photos.Photo;
import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.presentationlayer.badges.BadgeResponseDTO;
import com.petclinic.vet.presentationlayer.photos.PhotoRequestDTO;
import com.petclinic.vet.presentationlayer.photos.PhotoResponseDTO;
import com.petclinic.vet.presentationlayer.vets.VetAverageRatingDTO;
import com.petclinic.vet.presentationlayer.vets.VetController;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.presentationlayer.vets.VetResponseDTO;
import com.petclinic.vet.presentationlayer.education.EducationRequestDTO;
import com.petclinic.vet.presentationlayer.education.EducationResponseDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingRequestDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingResponseDTO;
import com.petclinic.vet.utils.exceptions.GlobalControllerExceptionHandler;
import com.petclinic.vet.utils.exceptions.NotFoundException;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(controllers=VetController.class)
@ContextConfiguration(classes = {VetController.class, GlobalControllerExceptionHandler.class})
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
    @MockBean
    AlbumService albumService;

    VetRequestDTO vetRequestDTO = buildVetRequestDTO();
    VetResponseDTO vetResponseDTO = buildVetResponseDTO();
    VetResponseDTO vetResponseDTO2 = buildVetResponseDTO2();



    VetAverageRatingDTO averageRatingDTO=buildVetAverageRatingDTO1();
    VetAverageRatingDTO averageRatingDTO2=buildVetAverageRatingDTO2();
    VetAverageRatingDTO averageRatingDTO3=buildVetAverageRatingDTO3();


    EducationResponseDTO educationResponseDTO1 = buildEducation();

    Photo photo = buildPhoto();


    RatingResponseDTO ratingDTO = buildRatingResponseDTO("Vet was super calming with my pet",5.0);

    RatingResponseDTO ratingDateResponseDTO=buildRatingResponseWithDate();
    RatingResponseDTO ratingDateResponseDTO2=buildRatingResponseWithDate2();

    Rating ratingWithDate=buildNewRatingWithDate();
    Rating ratingWithDate2=buildNewRatingWithDate2();

    Vet vet = buildVet();
    String VET_ID = vet.getVetId();
    String VET_BILL_ID = vet.getVetBillId();
    String INVALID_VET_ID = "mjbedf";

    ClassPathResource cpr=new ClassPathResource("images/full_food_bowl.png");
    ClassPathResource cpr2=new ClassPathResource("images/vet_default.jpg");

    Integer ALBUM_ID = 1;

    private static final String PHOTO_NAME = "test.jpg";

    @Test
    void getAllRatingsForVet_WithValidVetId_ShouldReturnRatings() {

        RatingResponseDTO rating1 = RatingResponseDTO.builder()
                .ratingId("1")
                .vetId(VET_ID)
                .rateScore(5.0)
                .customerName("John Doe")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .rateDate("2023-09-16")
                .build();

        RatingResponseDTO rating2 = RatingResponseDTO.builder()
                .ratingId("2")
                .vetId(VET_ID)
                .rateScore(4.0)
                .customerName("Jane Doe")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("2022-09-16")
                .build();

        when(ratingService.getAllRatingsByVetId(anyString()))
                .thenReturn(Flux.just(rating1, rating2));

        client.get()
                .uri("/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(RatingResponseDTO.class)
                .value(ratingList -> {
                    assertEquals(2, ratingList.size());
                    assertEquals(rating1.getRatingId(), ratingList.get(0).getRatingId());
                    assertEquals(rating1.getRateDate(), ratingList.get(0).getRateDate());
                    assertEquals(rating2.getRatingId(), ratingList.get(1).getRatingId());
                    assertEquals(rating2.getRateDate(), ratingList.get(1).getRateDate());
                });

        Mockito.verify(ratingService, times(1)).getAllRatingsByVetId(VET_ID);
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(ratingService, times(1))
                .deleteRatingByRatingId(vetId,ratingId);
    }

    @Test
    void deleteRatingForVetByCustomerName_ShouldSucceed() {
        String customerName = "Test Customer";
        String vetId = "694ac37f-1e07-43c2-93bc-61839e61d989";

        when(ratingService.deleteRatingByVetIdAndCustomerName(vetId, customerName))
                .thenReturn((Mono.empty()));

        client
                .delete()
                .uri("/vets/" + vetId + "/ratings/customer/{customerName}", customerName)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(ratingService, times(1))
                .deleteRatingByVetIdAndCustomerName(vetId, customerName);
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .bodyValue(updatedRating)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
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
    void getRatingBasedOnDate() {


        String exisingYearDate="2023";

        Map<String, String> ratingQueryParams = new HashMap<>();
        ratingQueryParams.put("year", exisingYearDate);

        when(ratingService.getRatingsOfAVetBasedOnDate(vet.getVetId(),ratingQueryParams))
                .thenReturn(Flux.just(ratingDateResponseDTO,ratingDateResponseDTO2));
        client.get()
                .uri("/vets/"+VET_ID+"/ratings/date?year={year}",exisingYearDate)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(RatingResponseDTO.class)
                .value(ratingDTOs ->{
                            assertEquals(2, ratingDTOs.size());
                        }
                );

        Mockito.verify(ratingService, times(1))
                .getRatingsOfAVetBasedOnDate(vet.getVetId(),ratingQueryParams);


    }

    @Test
    void getPercentageOfRatingsByVetId_ShouldSucceed() {
        when(ratingService.getRatingPercentagesByVetId(anyString()))
                .thenReturn(Mono.just("{\"1.0\":0.0,\"2.0\":0.0,\"4.0\":0.0,\"5.0\":1.0,\"3.0\":0.0}"));
        client
                .get()
                .uri("/vets/" + VET_ID + "/ratings/percentages")
                .accept(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetResponseDTO.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetResponseDTO.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetResponseDTO.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetResponseDTO.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetResponseDTO.getEmail())
                .jsonPath("$[0].active").isEqualTo(vetResponseDTO.isActive())
                .jsonPath("$[0].workHoursJson").isEqualTo(vetResponseDTO.getWorkHoursJson());

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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vet.getVetId())
                .jsonPath("$.vetBillId").isEqualTo(vet.getVetBillId())
                .jsonPath("$.resume").isEqualTo(vet.getResume())
                .jsonPath("$.lastName").isEqualTo(vet.getLastName())
                .jsonPath("$.firstName").isEqualTo(vet.getFirstName())
                .jsonPath("$.email").isEqualTo(vet.getEmail())
                .jsonPath("$.active").isEqualTo(vet.isActive())
                .jsonPath("$.workHoursJson").isEqualTo(vet.getWorkHoursJson());


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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetResponseDTO2.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetResponseDTO2.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetResponseDTO2.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetResponseDTO2.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetResponseDTO2.getEmail())
                .jsonPath("$[0].active").isEqualTo(vetResponseDTO2.isActive())
                .jsonPath("$[0].workHoursJson").isEqualTo(vetResponseDTO2.getWorkHoursJson());

        Mockito.verify(vetService, times(1))
                .getVetByIsActive(vetResponseDTO2.isActive());
    }

    @Test
    void createVet() {
        when(vetService.addVet(any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .post()
                .uri("/vets")
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
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
                .addVet(any(Mono.class));
    }

    @Test
    void updateVet() {
        when(vetService.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .put()
                .uri("/vets/" + VET_ID)
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vetResponseDTO.getVetId())
                .jsonPath("$.resume").isEqualTo(vetResponseDTO.getResume())
                .jsonPath("$.lastName").isEqualTo(vetResponseDTO.getLastName())
                .jsonPath("$.firstName").isEqualTo(vetResponseDTO.getFirstName())
                .jsonPath("$.email").isEqualTo(vetResponseDTO.getEmail())
                .jsonPath("$.active").isEqualTo(vetResponseDTO.isActive());

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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetResponseDTO.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetResponseDTO.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetResponseDTO.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetResponseDTO.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetResponseDTO.getEmail())
                .jsonPath("$[0].active").isEqualTo(vetResponseDTO.isActive())
                .jsonPath("$[0].workHoursJson").isEqualTo(vetResponseDTO.getWorkHoursJson());

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
                .accept(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(educationService, times(1))
                .deleteEducationByEducationId(vetId, educationId);
    }

    @Test
    void deleteEducationForVetByEducationId_EducationNotFound() {
        String educationId = "non-existent-id";
        String vetId = "694ac37f-1e07-43c2-93bc-61839e61d989";

        when(educationService.deleteEducationByEducationId(vetId, educationId))
                .thenReturn(Mono.error(new NotFoundException("Education not found")));

        client
                .delete()
                .uri("/vets/" + vetId + "/educations/{educationId}", educationId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

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
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .bodyValue(updatedEducation)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
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
    void addEducationWithVetId_ValidValues_ShouldSucceed() {
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

        when(educationService.addEducationToVet(eq(VET_ID), any(Mono.class)))
                .thenReturn(Mono.just(educationResponseDTO));

        client.post()
                .uri("/vets/{vetId}/educations", VET_ID)
                .bodyValue(educationRequestDTO)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(EducationResponseDTO.class)
                .consumeWith(response -> {
                    EducationResponseDTO responseBody = response.getResponseBody();
                    assert responseBody != null; 
                    assertEquals("3", responseBody.getEducationId());
                    assertEquals(VET_ID, responseBody.getVetId());
                });

        Mockito.verify(educationService, times(1)).addEducationToVet(eq(VET_ID), any(Mono.class));
    }


    @Test
    void getPhotoByVetId() {
        PhotoResponseDTO photoResponse = PhotoResponseDTO.builder()
                .vetId(VET_ID)
                .filename("vet_photo.jpg")
                .imgType("image/jpeg")
                .resource(photo.getData())
                .build();
                
        when(photoService.getPhotoByVetId(anyString()))
                .thenReturn(Mono.just(photoResponse));

        client.get()
                .uri("/vets/{vetId}/photo", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhotoResponseDTO.class)
                .consumeWith(response -> {
                    PhotoResponseDTO result = response.getResponseBody();
                    assertNotNull(result);
                    assertEquals(VET_ID, result.getVetId());
                    assertEquals("vet_photo.jpg", result.getFilename());
                });

        Mockito.verify(photoService, times(1))
                .getPhotoByVetId(VET_ID);
    }

    // test add photo
 /*   @Test
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
*/
    @Test
    void updatePhotoByVetId() {
        PhotoRequestDTO photoRequest = PhotoRequestDTO.builder()
                .vetId(VET_ID)
                .filename("vet_photo.jpg")
                .imgType("image/jpeg")
                .data(photo.getData())
                .build();
                
        PhotoResponseDTO photoResponse = PhotoResponseDTO.builder()
                .vetId(VET_ID)
                .filename("vet_photo.jpg")
                .imgType("image/jpeg")
                .resource(photo.getData())
                .build();

        when(photoService.updatePhotoByVetId(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(photoResponse));

        client.put()
                .uri("/vets/{vetId}/photo", VET_ID)
                .bodyValue(photoRequest)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PhotoResponseDTO.class)
                .consumeWith(response -> {
                    PhotoResponseDTO result = response.getResponseBody();
                    assertNotNull(result);
                    assertEquals(VET_ID, result.getVetId());
                    assertEquals("vet_photo.jpg", result.getFilename());
                });

        Mockito.verify(photoService, times(1))
                .updatePhotoByVetId(anyString(), any(Mono.class));
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
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(BadgeResponseDTO.class)
                .value(responseDTO -> {
                    assertEquals(badgeResponseDTO.getBadgeTitle(), responseDTO.getBadgeTitle());
                    assertEquals(badgeResponseDTO.getBadgeDate(), responseDTO.getBadgeDate());
                    assertEquals(badgeResponseDTO.getVetId(), responseDTO.getVetId());
                    assertEquals(badgeResponseDTO.getResourceBase64(), responseDTO.getResourceBase64());
                });
    }
    @Test
    void getDefaultPhotoByVetId_shouldSucceed() throws IOException {
        PhotoResponseDTO photoResponseDTO = buildPhotoResponseDTO();

        when(photoService.getDefaultPhotoByVetId(anyString()))
                .thenReturn(Mono.just(photoResponseDTO));

        client.get()
                .uri("/vets/{vetId}/default-photo", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(PhotoResponseDTO.class)
                .value(responseDTO -> {
                    assertEquals(photoResponseDTO.getFilename(), responseDTO.getFilename());
                    assertEquals(photoResponseDTO.getImgType(), responseDTO.getImgType());
                    assertEquals(photoResponseDTO.getVetId(), responseDTO.getVetId());
                    assertEquals(photoResponseDTO.getResourceBase64(), responseDTO.getResourceBase64());
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
    private PhotoResponseDTO buildPhotoResponseDTO() throws IOException {
        String defaultPhotoName = "vet_default.jpg";
        return PhotoResponseDTO.builder()
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .filename(defaultPhotoName)
                .imgType("image/jpeg")
                .resourceBase64(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(cpr2.getInputStream())))
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
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
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
                .photoDefault(true)
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
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
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
                .workHoursJson("{\n" +
                        "            \"Monday\": [\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],\n" +
                        "            \"Wednesday\": [\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"],\n" +
                        "            \"Thursday\": [\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"]\n" +
                        "        }")
                .specialties(new HashSet<>())
                .active(true)
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
    private RatingResponseDTO buildRatingResponseDTOWithDate(String description, double score) {
        return RatingResponseDTO.builder()
                .ratingId("2")
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .rateScore(score)
                .rateDescription(description)
                .rateDate("16/09/2023")
                .date("2023")
                .build();
    }

    private RatingResponseDTO buildRatingResponseDTOWithDate2(String description, double score) {
        return RatingResponseDTO.builder()
                .ratingId("2")
                .vetId("cf25e779-548b-4788-aefa-6d58621c2feb")
                .rateScore(score)
                .rateDescription(description)
                .rateDate("16/09/2023")
                .date("2022")
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

    private RatingResponseDTO buildRatingResponseWithDate(){
        return RatingResponseDTO .builder()
                .vetId("1232")
                .date("2023")
                .predefinedDescription(PredefinedDescription.GOOD)
                .ratingId("1312")
                .rateScore(2.0)
                .rateDescription("This is a bad vet")
                .build();
    }
    private RatingResponseDTO buildRatingResponseWithDate2(){
        return RatingResponseDTO .builder()
                .vetId("1232")
                .date("2022")
                .predefinedDescription(PredefinedDescription.POOR)
                .ratingId("1234")
                .rateScore(2.0)
                .rateDescription("This is a bad vet")
                .build();
    }

    private Rating buildNewRatingWithDate(){
        return Rating.builder()
                .vetId("1232")
                .date("2023")
                .id("1")
                .rateScore(3.0)
                .predefinedDescription(PredefinedDescription.POOR)
                .rateDate("21")
                .rateDescription("This is a medium vet")
                .build();
    }

    private Rating buildNewRatingWithDate2(){
        return Rating.builder()
                .vetId("1281")
                .date("2022")
                .id("2")
                .rateScore(1.0)
                .predefinedDescription(PredefinedDescription.POOR)
                .rateDate("10")
                .rateDescription("This is a bad vet")
                .build();
    }

 /*   @Test
    void addPhoto_ShouldReturnBadRequest_WhenRequestIsMalformed() throws IOException {
        // Create MultiValueMap for multipart/form-data body
        MultiValueMap<String, HttpEntity<?>> body = new LinkedMultiValueMap<>();

        // Add an empty file (simulating malformed input)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        // Create an HttpEntity representing a multipart file part with no content
        HttpEntity<byte[]> emptyFilePart = new HttpEntity<>(null, headers);
        body.add("file", emptyFilePart);

        // Perform the POST request
        client.post()
                .uri("/vets/{vetId}/photos/{photoName}", VET_ID, PHOTO_NAME)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body)) // Proper multipart data insertion
                .exchange()
                .expectStatus().isBadRequest()   // Expect 400 Bad Request
                .expectBody()
                .consumeWith(response -> {
                    // Optional: Check the error message in the response
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Bad Request"),
                            "Expected 'bad request' error message");
                });

        // Ensure the service is not called due to invalid request
        Mockito.verify(photoService, times(0))
                .insertPhotoOfVet(anyString(), anyString(), any(Mono.class));
    }

*/

    @Test
    void whenGetAllAlbumsByVetId_thenReturnAlbums() {

        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";
        Album album1 = new Album(1, vetId, "album1.jpg", "image/jpeg", "mockImageData1".getBytes());
        Album album2 = new Album(2, vetId, "album2.jpg", "image/jpeg", "mockImageData2".getBytes());

        when(albumService.getAllAlbumsByVetId(vetId))
                .thenReturn(Flux.just(album1, album2));

        client.get()
                .uri("/vets/" + vetId + "/albums")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(Album.class)
                .hasSize(2)
                .contains(album1, album2);
    }


    @Test
    void whenGetAllAlbumsByVetId_withError_thenLogError() {

        String vetId = "ac9adeb8-625b-11ee-8c99-0242ac120002";

        when(albumService.getAllAlbumsByVetId(vetId))
                .thenReturn(Flux.error(new RuntimeException("Test error")));

        client.get()
                .uri("/vets/" + vetId + "/albums")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

    }
    @Test
    void whenDeleteAlbumPhotoById_thenReturnNoContent() {
        when(albumService.deleteAlbumPhotoById(VET_ID, ALBUM_ID))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/vets/" + VET_ID + "/albums/" + ALBUM_ID)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent(); // Expecting 204 No Content
    }

    @Test
    void whenDeleteAlbumPhotoById_withNonExistentId_thenReturnNotFound() {
        when(albumService.deleteAlbumPhotoById(VET_ID, ALBUM_ID))
                .thenReturn(Mono.error(new NotFoundException("Album photo not found: " + ALBUM_ID)));

        client.delete()
                .uri("/vets/" + VET_ID + "/albums/" + ALBUM_ID)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // Expecting 404 Not Found
    }


    @Test
    void whenDeleteAlbumPhotoById_withError_thenReturnServerError() {
        when(albumService.deleteAlbumPhotoById(VET_ID, ALBUM_ID))
                .thenReturn(Mono.error(new RuntimeException("Test error")));

        client.delete()
                .uri("/vets/" + VET_ID + "/albums/" + ALBUM_ID)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError(); // Expecting 500 Internal Server Error
    }


}
