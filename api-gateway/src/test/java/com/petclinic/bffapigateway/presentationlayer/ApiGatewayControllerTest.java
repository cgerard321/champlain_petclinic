package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Pets.PetTypeResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.dtos.Visits.Status;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        GlobalExceptionHandler.class,
        BFFApiGatewayController.class,
        AuthServiceClient.class,
        CustomersServiceClient.class,
        VisitsServiceClient.class,
        VetsServiceClient.class,
        BillServiceClient.class,
        InventoryServiceClient.class
})
@WebFluxTest(controllers = BFFApiGatewayController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
        classes = {JwtTokenFilter.class,RoleFilter.class}),useDefaultFilters = false)
@AutoConfigureWebTestClient
class ApiGatewayControllerTest {
    @Autowired private WebTestClient client;
    @MockBean private CustomersServiceClient customersServiceClient;
    @MockBean private VisitsServiceClient visitsServiceClient;
    @MockBean private VetsServiceClient vetsServiceClient;
    @MockBean private AuthServiceClient authServiceClient;
    @MockBean private BillServiceClient billServiceClient;
    @MockBean private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private BFFApiGatewayController apiGatewayController;

    @Mock
    private CustomersServiceClient customersServiceClientMock;

    VetResponseDTO vetResponseDTO = buildVetResponseDTO();
    VetRequestDTO vetRequestDTO = buildVetRequestDTO();
    VetResponseDTO vetResponseDTO2 = buildVetResponseDTO2();
    VetRequestDTO vetRequestDTO2 = buildVetRequestDTO2();
    String VET_ID = buildVetResponseDTO().getVetId();
    String INVALID_VET_ID = "mjbedf";



    @Test
    void getAllRatingsForVet_ValidId() {
        RatingResponseDTO ratingResponseDTO = buildRatingResponseDTO();
        when(vetsServiceClient.getRatingsByVetId(anyString()))
                .thenReturn(Flux.just(ratingResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID + "/ratings")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(RatingResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertNotNull(responseDTO);
                    Assertions.assertNotNull(responseDTO.get(0).getRatingId());
                    assertThat(responseDTO.get(0).getRatingId()).isEqualTo(ratingResponseDTO.getRatingId());
                    assertThat(responseDTO.get(0).getVetId()).isEqualTo(ratingResponseDTO.getVetId());
                    assertThat(responseDTO.get(0).getRateScore()).isEqualTo(ratingResponseDTO.getRateScore());
                });
    }

    @Test
    void getTopThreeVetsWithHighestRating(){
        VetAverageRatingDTO vetAverageRatingDTO = buildVetAverageRatingDTO();
        when(vetsServiceClient.getTopThreeVetsWithHighestAverageRating())
                .thenReturn(Flux.just(vetAverageRatingDTO));

        client
                .get()
                .uri("/api/gateway/vets/topVets")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
//                .jsonPath("$[0].firstName").isEqualTo(vetAverageRatingDTO.getFirstName())
//                .jsonPath("$[0].lastName").isEqualTo(vetAverageRatingDTO.getLastName())
                .jsonPath("$[0].vetId").isEqualTo(vetAverageRatingDTO.getVetId())
                .jsonPath("$[0].averageRating").isEqualTo(vetAverageRatingDTO.getAverageRating());
    }
    @Test
    void deleteVetRating() {
        RatingResponseDTO ratingResponseDTO = buildRatingResponseDTO();
        when(vetsServiceClient.deleteRating(VET_ID, ratingResponseDTO.getRatingId()))
                .thenReturn((Mono.empty()));
        client
                .delete()
                .uri("/api/gateway/vets/" + VET_ID + "/ratings/{ratingsId}", ratingResponseDTO.getRatingId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(vetsServiceClient, times(1))
                .deleteRating(VET_ID, ratingResponseDTO.getRatingId());
    }

    @Test
    void getAllRatingsForVet_ByInvalidVetId() {
        RatingResponseDTO ratingResponseDTO = buildRatingResponseDTO();
        when(vetsServiceClient.getRatingsByVetId(anyString()))
                .thenThrow(new ExistingVetNotFoundException("This id is not valid", NOT_FOUND));

        client
                .get()
                .uri("/api/gateway/vets/" + INVALID_VET_ID + "/ratings")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
                //  .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");
    }

    @Test
    void getNumberOfRatingsPerVet_ByVetId() {
        when(vetsServiceClient.getNumberOfRatingsByVetId(anyString()))
                .thenReturn(Mono.just(1));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID + "/ratings/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo(1);
    }

    @Test
    void getAverageRatingsByVetId(){
        RatingResponseDTO ratingResponseDTO = buildRatingResponseDTO();

        RatingRequestDTO rating = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(4.0)
                .build();

        when(vetsServiceClient.getAverageRatingByVetId(anyString()))
                .thenReturn(Mono.just(ratingResponseDTO.getRateScore()));

        client
                .get()
                .uri("/api/gateway/vets/" + rating.getVetId() + "/ratings/average")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Double.class)
                .value(resp->
                        assertEquals(rating.getRateScore(), ratingResponseDTO.getRateScore()));

    }

    @Test
    void getPercentageOfRatingsPerVet_ByVetId() {
        when(vetsServiceClient.getPercentageOfRatingsByVetId(anyString()))
                .thenReturn(Mono.just("1"));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID + "/ratings/percentages")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("1");
    }



    @Test
    void addRatingToAVet_withRateDescriptionAndPredefinedDesc_ShouldSetRateDescriptionToPredefinedDesc() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("GOOD")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        when(vetsServiceClient.addRatingToVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.post()
                .uri("/api/gateway/vets/{vetId}/ratings", VET_ID)
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    void addRatingToVet_withPredefinedDescriptionOnly_ShouldSetRateDescriptionToPredefinedDesc() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("GOOD")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        when(vetsServiceClient.addRatingToVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.post()
                .uri("/api/gateway/vets/{vetId}/ratings", VET_ID)
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    void addRatingToVet_withRateDescriptionOnly_ShouldSetPredefinedDescriptionToNull() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(null)
                .rateDate("16/09/2023")
                .build();
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(null)
                .rateDate("16/09/2023")
                .build();
        when(vetsServiceClient.addRatingToVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.post()
                .uri("/api/gateway/vets/{vetId}/ratings", VET_ID)
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    void updateRatingForVet_withRateDescriptionAndPredefinedDesc_ShouldSetRateDescriptionToPredefinedDesc() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("GOOD")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        when(vetsServiceClient.updateRatingByVetIdAndByRatingId(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.put()
                .uri("/api/gateway/vets/{vetId}/ratings/{ratingId}", VET_ID, ratingResponseDTO.getRatingId())
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }
    @Test
    void updateRatingForVet_withPredefinedDescriptionOnly_ShouldSetRateDescriptionToPredefinedDesc() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("GOOD")
                .predefinedDescription(PredefinedDescription.GOOD)
                .rateDate("16/09/2023")
                .build();
        when(vetsServiceClient.updateRatingByVetIdAndByRatingId(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.put()
                .uri("/api/gateway/vets/{vetId}/ratings/{ratingId}", VET_ID, ratingResponseDTO.getRatingId())
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    void updateRatingForVet_withRateDescriptionOnly_ShouldSetRateDescriptionToItsValue() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(null)
                .rateDate("16/09/2023")
                .build();
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .predefinedDescription(null)
                .rateDate("16/09/2023")
                .build();
        when(vetsServiceClient.updateRatingByVetIdAndByRatingId(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.put()
                .uri("/api/gateway/vets/{vetId}/ratings/{ratingId}", VET_ID, ratingResponseDTO.getRatingId())
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }


    @Test
    void getAllEducationsByVetId_WithValidId_ShouldSucceed(){
        EducationResponseDTO educationResponseDTO = buildEducation();
        when(vetsServiceClient.getEducationsByVetId(anyString()))
                .thenReturn(Flux.just(educationResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID + "/educations")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(EducationResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertNotNull(responseDTO);
                    Assertions.assertNotNull(responseDTO.get(0).getEducationId());
                    assertThat(responseDTO.get(0).getEducationId()).isEqualTo(educationResponseDTO.getEducationId());
                    assertThat(responseDTO.get(0).getVetId()).isEqualTo(educationResponseDTO.getVetId());
                    assertThat(responseDTO.get(0).getDegree()).isEqualTo(educationResponseDTO.getDegree());
                    assertThat(responseDTO.get(0).getFieldOfStudy()).isEqualTo(educationResponseDTO.getFieldOfStudy());
                    assertThat(responseDTO.get(0).getSchoolName()).isEqualTo(educationResponseDTO.getSchoolName());
                    assertThat(responseDTO.get(0).getStartDate()).isEqualTo(educationResponseDTO.getStartDate());
                    assertThat(responseDTO.get(0).getEndDate()).isEqualTo(educationResponseDTO.getEndDate());
                });
    }

    @Test
    void deleteVetEducation() {
        EducationResponseDTO educationResponseDTO = buildEducation();
        when(vetsServiceClient.deleteEducation(VET_ID, educationResponseDTO.getEducationId()))
                .thenReturn((Mono.empty()));

        client
                .delete()
                .uri("/api/gateway/vets/" + VET_ID + "/educations/{educationId}", educationResponseDTO.getEducationId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(vetsServiceClient, times(1))
                .deleteEducation(VET_ID, educationResponseDTO.getEducationId());
    }
    @Test
    void updateEducationForVet(){
        EducationRequestDTO updatedEducation = EducationRequestDTO.builder()
                .schoolName("McGill")
                .vetId("678910")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        EducationResponseDTO educationResponseDTO = EducationResponseDTO.builder()
                .educationId("12356789")
                .vetId("678910")
                .schoolName("McGill")
                .degree("Bachelor of Medicine")
                .fieldOfStudy("Medicine")
                .startDate("2010")
                .endDate("2015")
                .build();

        when(vetsServiceClient.updateEducationByVetIdAndByEducationId(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(educationResponseDTO));

        client.put()
                .uri("/api/gateway/vets/"+VET_ID+"/educations/"+educationResponseDTO.getEducationId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedEducation)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EducationResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertNotNull(responseDTO);
                    Assertions.assertNotNull(responseDTO.getEducationId());
                    assertThat(responseDTO.getEducationId()).isEqualTo(educationResponseDTO.getEducationId());
                    assertThat(responseDTO.getVetId()).isEqualTo(updatedEducation.getVetId());
                    assertThat(responseDTO.getSchoolName()).isEqualTo(updatedEducation.getSchoolName());
                    assertThat(responseDTO.getDegree()).isEqualTo(updatedEducation.getDegree());
                    assertThat(responseDTO.getFieldOfStudy()).isEqualTo(updatedEducation.getFieldOfStudy());
                    assertThat(responseDTO.getStartDate()).isEqualTo(updatedEducation.getStartDate());
                    assertThat(responseDTO.getEndDate()).isEqualTo(updatedEducation.getEndDate());
                });
    }

    @Test
    void addEducationToAVet() {
        EducationRequestDTO educationRequestDTO = EducationRequestDTO.builder()
                .vetId(VET_ID)
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();
        EducationResponseDTO educationResponseDTO = EducationResponseDTO.builder()
                .educationId("12356789")
                .vetId(VET_ID)
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();
        when(vetsServiceClient.addEducationToAVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(educationResponseDTO));

        client.post()
                .uri("/api/gateway/vets/{vetId}/educations", VET_ID)
                .bodyValue(educationRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    @Test
    void getAllVets() {
        when(vetsServiceClient.getVets())
                .thenReturn(Flux.just(vetResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(VetResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertNotNull(responseDTO);
                    Assertions.assertNotNull(responseDTO.get(0).getVetId());
                    assertThat(responseDTO.get(0).getVetId()).isEqualTo(vetResponseDTO.getVetId());
                    assertThat(responseDTO.get(0).getResume()).isEqualTo(vetResponseDTO.getResume());
                    assertThat(responseDTO.get(0).getLastName()).isEqualTo(vetResponseDTO.getLastName());
                    assertThat(responseDTO.get(0).getFirstName()).isEqualTo(vetResponseDTO.getFirstName());
                    assertThat(responseDTO.get(0).getEmail()).isEqualTo(vetResponseDTO.getEmail());
                    assertThat(responseDTO.get(0).isActive()).isEqualTo(vetResponseDTO.isActive());
                    assertThat(responseDTO.get(0).getWorkday()).isEqualTo(vetResponseDTO.getWorkday());
                });
        Mockito.verify(vetsServiceClient, times(1))
                .getVets();
    }

    @Test
    void getVetByVetId() {
        when(vetsServiceClient.getVetByVetId(anyString()))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID)
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
                .jsonPath("$.active").isEqualTo(vetResponseDTO.isActive());

        Mockito.verify(vetsServiceClient, times(1))
                .getVetByVetId(VET_ID);
    }

    @Test
    void getActiveVets() {
        when(vetsServiceClient.getActiveVets())
                .thenReturn(Flux.just(vetResponseDTO2));

        client
                .get()
                .uri("/api/gateway/vets/active")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(VetResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertNotNull(responseDTO);
                    Assertions.assertNotNull(responseDTO.get(0).getVetId());
                    assertThat(responseDTO.get(0).getVetId()).isEqualTo(vetResponseDTO2.getVetId());
                    assertThat(responseDTO.get(0).getResume()).isEqualTo(vetResponseDTO2.getResume());
                    assertThat(responseDTO.get(0).getLastName()).isEqualTo(vetResponseDTO2.getLastName());
                    assertThat(responseDTO.get(0).getFirstName()).isEqualTo(vetResponseDTO2.getFirstName());
                    assertThat(responseDTO.get(0).getEmail()).isEqualTo(vetResponseDTO2.getEmail());
                    assertThat(responseDTO.get(0).isActive()).isEqualTo(vetResponseDTO2.isActive());
                    assertThat(responseDTO.get(0).getWorkday()).isEqualTo(vetResponseDTO2.getWorkday());
                });
        Mockito.verify(vetsServiceClient, times(1))
                .getActiveVets();
    }

    @Test
    void getInactiveVets() {
        when(vetsServiceClient.getInactiveVets())
                .thenReturn(Flux.just(vetResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets/inactive")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(VetResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertNotNull(responseDTO);
                    Assertions.assertNotNull(responseDTO.get(0).getVetId());
                    assertThat(responseDTO.get(0).getVetId()).isEqualTo(vetResponseDTO.getVetId());
                    assertThat(responseDTO.get(0).getResume()).isEqualTo(vetResponseDTO.getResume());
                    assertThat(responseDTO.get(0).getLastName()).isEqualTo(vetResponseDTO.getLastName());
                    assertThat(responseDTO.get(0).getFirstName()).isEqualTo(vetResponseDTO.getFirstName());
                    assertThat(responseDTO.get(0).getEmail()).isEqualTo(vetResponseDTO.getEmail());
                    assertThat(responseDTO.get(0).isActive()).isEqualTo(vetResponseDTO.isActive());
                    assertThat(responseDTO.get(0).getWorkday()).isEqualTo(vetResponseDTO.getWorkday());
                });
        Mockito.verify(vetsServiceClient, times(1))
                .getInactiveVets();
    }

    @Test
    void createVet() {

        RegisterVet registerVet = RegisterVet.builder()
                .userId(VET_ID)
                .username("vet")
                .email("vet@email.com")
                .password("pwd")
                .vet(vetRequestDTO).build();


        Role role = Role.builder()
                .id(1)
                .name(Roles.ADMIN.name())
                .build();

        UserDetails userDetails = UserDetails.builder()
                .userId(VET_ID)
                .username("vet")
                .email("email@vet.com")
                .roles(Set.of(role))
                .build();

        Mono<RegisterVet> dto = Mono.just(registerVet);

        when(authServiceClient.createVetUser(any(Mono.class)))
                .thenReturn((Mono.just(vetResponseDTO)));




        client
                .post()
                .uri("/api/gateway/users/vets")
                .body(dto, RegisterVet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

    }

    @Test
    void updateVet() {
        when(vetsServiceClient.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO2));

        client
                .put()
                .uri("/api/gateway/vets/" + VET_ID)
                .body(Mono.just(vetResponseDTO2), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vetResponseDTO2.getVetId())
                .jsonPath("$.resume").isEqualTo(vetResponseDTO2.getResume())
                .jsonPath("$.lastName").isEqualTo(vetResponseDTO2.getLastName())
                .jsonPath("$.firstName").isEqualTo(vetResponseDTO2.getFirstName())
                .jsonPath("$.email").isEqualTo(vetResponseDTO2.getEmail())
                .jsonPath("$.active").isEqualTo(vetResponseDTO2.isActive());

        Mockito.verify(vetsServiceClient, times(1))
                .updateVet(anyString(), any(Mono.class));
    }

    @Test
    void deleteVet() {
        when(vetsServiceClient.deleteVet(anyString()))
                .thenReturn((Mono.empty()));

        client
                .delete()
                .uri("/api/gateway/vets/" + VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(vetsServiceClient, times(1))
                .deleteVet(VET_ID);
    }

    @Test
    void getByVetId_Invalid() {
        when(vetsServiceClient.getVetByVetId(anyString()))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets/" + INVALID_VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");
    }

    @Test
    void updateByVetId_Invalid() {
        when(vetsServiceClient.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

        client
                .put()
                .uri("/api/gateway/vets/" + INVALID_VET_ID)
                .body(Mono.just(vetRequestDTO), VetRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");
    }

    @Test
    void deleteByVetId_Invalid() {
        when(vetsServiceClient.deleteVet(anyString()))
                .thenReturn((Mono.empty()));

        client
                .delete()
                .uri("/api/gateway/vets/" + INVALID_VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("This id is not valid");
    }

    @Test
    void getPhotoByVetId() {
        byte[] photo = {123, 23, 75, 34};
        Resource resource = new ByteArrayResource(photo);

        when(vetsServiceClient.getPhotoByVetId(anyString()))
                .thenReturn(Mono.just(resource));

        client.get()
                .uri("/api/gateway/vets/{vetId}/photo", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE)
                .expectBody(Resource.class)
                .consumeWith(response -> {
                    assertEquals(resource, response.getResponseBody());
                });

        Mockito.verify(vetsServiceClient, times(1))
                .getPhotoByVetId(VET_ID);
    }

    @Test
    void addPhotoToVet() {
        byte[] photo = {123, 23, 75, 34};
        Resource resource = new ByteArrayResource(photo);

        when(vetsServiceClient.addPhotoToVet(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(resource));

        client.post()
                .uri("/api/gateway/vets/{vetId}/photos/{photoName}", VET_ID, "vet_photo.jpg")
                .body(Mono.just(resource), Resource.class)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(Resource.class)
                .consumeWith(response -> {
                    assertEquals(resource, response.getResponseBody());
                });

        Mockito.verify(vetsServiceClient, times(1))
                .addPhotoToVet(anyString(), anyString(), any(Mono.class));
    }
    @Test
    void toStringBuilderVets() {
        System.out.println(VetRequestDTO.builder());
        System.out.println(VetResponseDTO.builder());
    }




//    @Test
//    void getOwnerDetails_withAvailableVisitsService() {
//        OwnerResponseDTO owner = new OwnerResponseDTO();
//        PetDetails cat = new PetDetails();
//        cat.setId(20);
//        cat.setName("Garfield");
//        owner.getPets().add(cat);
//        when(customersServiceClient.getOwner(1))
//                .thenReturn(Mono.just(owner));
//
//        Visits visits = new Visits();
//        VisitDetails visit = new VisitDetails();
//        visit.setVisitId(UUID.randomUUID().toString());
//        visit.setDescription("First visit");
//        visit.setPetId(cat.getId());
//        visits.getItems().add(visit);
//        when(visitsServiceClient.getVisitsForPets(Collections.singletonList(cat.getId())))
//                .thenReturn(Mono.just(visits));
//        // java.lang.IllegalStateException at Assert.java:97
//
//        client.get()
//                .uri("/api/gateway/owners/1")
//                .exchange()
//                .expectStatus().isOk()
//                //.expectBody(String.class)
//                //.consumeWith(response ->
//                //    Assertions.assertThat(response.getResponseBody()).isEqualTo("Garfield"));
//                .expectBody()
//                .jsonPath("$.pets[0].name").isEqualTo("Garfield")
//                .jsonPath("$.pets[0].visits[0].description").isEqualTo("First visit");
//    }

    //    @Test
//    void getUserDetails() {
//        UserDetails user = new UserDetails();
//        user.setId(1);
//        user.setUsername("roger675");
//        user.setPassword("secretnooneknows");
//        user.setEmail("RogerBrown@gmail.com");
//
//        when(authServiceClient.getUser(1))
//                .thenReturn(Mono.just(user));
//
//        client.get()
//
//                .uri("/api/gateway/users/1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.username").isEqualTo("roger675")
//                .jsonPath("$.password").isEqualTo("secretnooneknows")
//                .jsonPath("$.email").isEqualTo("RogerBrown@gmail.com");
//
//        assertEquals(user.getId(), 1);
//    }
//
    @Test
    void createUser(){
        String uuid = UUID.randomUUID().toString();
        OwnerRequestDTO owner = OwnerRequestDTO
                .builder()
                .ownerId(uuid)
                .firstName("John")
                .lastName("Johnny")
                .address("111 John St")
                .city("Johnston")
                .province("Quebec")
                .telephone("51451545144")
                .build();


        OwnerResponseDTO owner_response = OwnerResponseDTO
                .builder()
                .ownerId(uuid)
                .firstName("John")
                .lastName("Johnny")
                .address("111 John St")
                .city("Johnston")
                .province("Quebec")
                .telephone("51451545144")
                .build();



        when(authServiceClient.createUser(any()))
                .thenReturn(Mono.just(owner_response));

        Register register = Register.builder()
                .username("Johnny123")
                .password("Password22##")
                .email("email@email.com")
                .owner(owner)
                .build();

        client.post()
                .uri("/api/gateway/users")
                .body(Mono.just(register), UserDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(dto->{
                    assertNotNull(dto.getOwnerId());
                    assertEquals(dto.getFirstName(),owner.getFirstName());
                    assertEquals(dto.getLastName(),owner.getLastName());
                    assertEquals(dto.getAddress(),owner.getAddress());
                    assertEquals(dto.getCity(),owner.getCity());
                    assertEquals(dto.getProvince(),owner.getProvince());
                    assertEquals(dto.getTelephone(),owner.getTelephone());
                });



    }


    /*@Test
    void getOwnerDetails_withAvailableVisitsService() {
        OwnerResponseDTO owner = new OwnerResponseDTO();
        PetResponseDTO cat = new PetResponseDTO();
        cat.setId(20);
        cat.setName("Garfield");
        owner.getPets().add(cat);
        when(customersServiceClient.getOwner("ownerId-123"))
                .thenReturn(Mono.just(owner));

        Visits visits = new Visits();
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setDescription("First visit");
        visit.setPetId(cat.getId());
        visits.getItems().add(visit);
        when(visitsServiceClient.getVisitsForPets(Collections.singletonList(cat.getId())))
                .thenReturn(Mono.just(visits));
        // java.lang.IllegalStateException at Assert.java:97

        client.get()
                .uri("/api/gateway/owners/1")
                .exchange()
                .expectStatus().isOk()
                //.expectBody(String.class)
                //.consumeWith(response ->
                //    Assertions.assertThat(response.getResponseBody()).isEqualTo("Garfield"));
                .expectBody()
                .jsonPath("$.pets[0].name").isEqualTo("Garfield")
                .jsonPath("$.pets[0].visits[0].description").isEqualTo("First visit");
<<<<<<< HEAD
=======
<<<<<<< HEAD
    }
=======
    }
>>>>>>> ceef8eff (VetDTO split in apigateway)
>>>>>>> 0ee1ef7f (Merge conflicts part 3)

    }*/
//
//    @Test
//    void getUserDetails() {
//        UserDetails user = new UserDetails();
//        user.setId(1);
//        user.setUsername("roger675");
//        user.setPassword("secretnooneknows");
//        user.setEmail("RogerBrown@gmail.com");
//
//        when(authServiceClient.getUser(1))
//                .thenReturn(Mono.just(user));
//
//        client.get()
//
//                .uri("/api/gateway/users/1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.username").isEqualTo("roger675")
//                .jsonPath("$.password").isEqualTo("secretnooneknows")
//                .jsonPath("$.email").isEqualTo("RogerBrown@gmail.com");
//
//        assertEquals(user.getId(), 1);
//    }
//
//    @Test
//    void createUser(){
//        UserDetails user = new UserDetails();
//        user.setId(1);
//        user.setUsername("Johnny123");
//        user.setPassword("password");
//        user.setEmail("email@email.com");
//        when(authServiceClient.createUser(argThat(
//                n -> user.getEmail().equals(n.getEmail())
//        ))).thenReturn(Mono.just(user));
//
//        client.post()
//                .uri("/api/gateway/users")
//                .body(Mono.just(user), UserDetails.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody();
//
//        assertEquals(user.getId(), 1);
//        assertEquals(user.getUsername(), "Johnny123");
//        assertEquals(user.getPassword(), "password");
//        assertEquals(user.getEmail(), "email@email.com");
//
//    }

    @Test
    void getAllOwners_shouldSucceed(){
        OwnerResponseDTO owner1 = new OwnerResponseDTO();
        owner1.setOwnerId("ownerId-90");
        owner1.setFirstName("John");
        owner1.setLastName("Johnny");
        owner1.setAddress("111 John St");
        owner1.setCity("Johnston");
        owner1.setProvince("Quebec");
        owner1.setTelephone("51451545144");

        Flux<OwnerResponseDTO> ownerResponseDTOFlux = Flux.just(owner1);

        when(customersServiceClient.getAllOwners()).thenReturn(ownerResponseDTOFlux);

        client.get()
                .uri("/api/gateway/owners")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type","text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(1,list.size());
                    assertEquals(list.get(0).getOwnerId(),owner1.getOwnerId());
                    assertEquals(list.get(0).getFirstName(),owner1.getFirstName());
                });

    }

    @Test
    void getOwnersByPagination(){

        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-09");
        owner.setFirstName("Test");
        owner.setLastName("Test");
        owner.setAddress("Test");
        owner.setCity("Test");
        owner.setProvince("Test");
        owner.setTelephone("Test");

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size =  Optional.of(1);


        Flux<OwnerResponseDTO> ownerResponseDTOFlux = Flux.just(owner);

        when(customersServiceClient.getOwnersByPagination(page,size)).thenReturn(ownerResponseDTOFlux);

        client.get()
                .uri("/api/gateway/owners-pagination?page="+page.get()+"&size="+size.get())
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange().expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type","text/event-stream;charset=UTF-8")
                .expectBodyList(OwnerResponseDTO.class)
                .value((list) -> {
                    Assertions.assertNotNull(list);
                    Assertions.assertEquals(size.get(),list.size());
                });
    }

    @Test
    void getTotalNumberOfOwners(){
        long expectedCount = 0;

        when(customersServiceClient.getTotalNumberOfOwners()).thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/owners-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class) // Expecting a Long response
                .consumeWith(response -> {
                    Long responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(expectedCount, responseBody.longValue());
                });

    }

    @Test
    void getOwnerByOwnerId_shouldSucceed(){
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-123");
        owner.setFirstName("John");
        owner.setLastName("Johnny");
        owner.setAddress("111 John St");
        owner.setCity("Johnston");
        owner.setProvince("Quebec");
        owner.setTelephone("51451545144");
        when(customersServiceClient.getOwner("ownerId-123"))
                .thenReturn(Mono.just(owner));

        client.get()
                .uri("/api/gateway/owners/{ownerId}", owner.getOwnerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(ownerResponseDTO -> {
                    assertNotNull(ownerResponseDTO);
                    assertEquals(ownerResponseDTO.getOwnerId(),owner.getOwnerId());
                });


    }






//    @Test
//    void createOwner(){
//        OwnerResponseDTO owner = new OwnerResponseDTO();
//        owner.setOwnerId("ownerId-123");
//        owner.setFirstName("John");
//        owner.setLastName("Johnny");
//        owner.setAddress("111 John St");
//        owner.setCity("Johnston");
//        owner.setTelephone("51451545144");
//        when(customersServiceClient.createOwner(owner))
//                .thenReturn(Mono.just(owner));
//
//
//        client.post()
//                .uri("/api/gateway/owners")
//                .body(Mono.just(owner), OwnerResponseDTO.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody();
//
//
//
//        assertEquals(owner.getOwnerId(),owner.getOwnerId());
//        assertEquals(owner.getFirstName(),"John");
//        assertEquals(owner.getLastName(),"Johnny");
//        assertEquals(owner.getAddress(),"111 John St");
//        assertEquals(owner.getCity(),"Johnston");
//        assertEquals(owner.getTelephone(),"51451545144");
//    }







    Date date = new Date(20221010);

    @Test
    void shouldCreatePet(){

        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-12345");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("30-30-30-30");
        pet.setOwnerId("ownerId-12345");
        pet.setName("Fluffy");
        pet.setBirthDate(date);
        pet.setPetTypeId("5");
        pet.setIsActive("true");

        when(customersServiceClient.createPet(pet,od.getOwnerId()))

                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())

                .body(Mono.just(pet), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)

                .expectBody()
                .jsonPath("$.petId").isEqualTo(pet.getPetId())
                .jsonPath("$.name").isEqualTo(pet.getName())
                .jsonPath("$.petTypeId").isEqualTo(pet.getPetTypeId())
                .jsonPath("$.isActive").isEqualTo(pet.getIsActive());

    }

    @Test
    void shouldUpdatePet() {
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-12345");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("30-30-30-30");
        pet.setOwnerId("ownerId-12345");
        pet.setName("Fluffy");
        pet.setBirthDate(date);
        pet.setPetTypeId("5");
        pet.setIsActive("true");


        when(customersServiceClient.updatePet(any(PetResponseDTO.class), any(String.class)))
                .thenReturn(Mono.just(pet));

        client.put()
                .uri("/api/gateway/pets/{petId}", od.getOwnerId(), pet.getPetId())
                .body(fromValue(pet))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(pet.getPetId())
                .jsonPath("$.name").isEqualTo(pet.getName())
                .jsonPath("$.petTypeId").isEqualTo(pet.getPetTypeId())
                .jsonPath("$.isActive").isEqualTo(pet.getIsActive());

    }
    @Test
    void shouldDeletePet() {
        // Create a pet id that will be used in the test
        String petId = "petId-123";

        // Mock the deletePetByPetId method in the customersServiceClient
        when(customersServiceClientMock.deletePetByPetId(petId))
                .thenReturn(Mono.empty());

        // Call the deletePetByPetId method in the ApiGatewayController
        Mono<ResponseEntity<PetResponseDTO>> responseMono = apiGatewayController.deletePetByPetId(petId);

        // Verify that the deletePetByPetId method in the customersServiceClient was called with the correct pet id
        verify(customersServiceClientMock, times(1)).deletePetByPetId(petId);

        // Verify that the response is as expected
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                    assertNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void shouldPatchPet() {
        // Create a pet id and a pet request DTO that will be used in the test
        String petId = "petId-123";
        PetRequestDTO petRequestDTO = new PetRequestDTO();
        petRequestDTO.setPetId(petId);
        petRequestDTO.setIsActive("true");

        // Mock the patchPet method in the customersServiceClient
        PetResponseDTO expectedPetResponse = new PetResponseDTO();
        expectedPetResponse.setPetId(petId);
        expectedPetResponse.setIsActive("true");
        when(customersServiceClientMock.patchPet(petRequestDTO, petId))
                .thenReturn(Mono.just(expectedPetResponse));

        // Call the patchPet method in the ApiGatewayController
        Mono<ResponseEntity<PetResponseDTO>> responseMono = apiGatewayController.patchPet(petRequestDTO, petId);

        // Verify that the patchPet method in the customersServiceClient was called with the correct pet request DTO and pet id
        verify(customersServiceClientMock, times(1)).patchPet(petRequestDTO, petId);

        // Verify that the response is as expected
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals(expectedPetResponse, response.getBody());
                })
                .verifyComplete();
    }


    //
//
////TODO
//    @Test
//    void createOwnerPhoto(){
//        OwnerResponseDTO owner = new OwnerResponseDTO();
//        owner.setId(1);
//        owner.setFirstName("John");
//        owner.setLastName("Smith");
//        owner.setAddress("456 Elm");
//        owner.setCity("Montreal");
//        owner.setTelephone("5553334444");
//        owner.setImageId(1);
//
//        final String test = "Test photo";
//        final byte[] testBytes = test.getBytes();
//
//        PhotoDetails photo = new PhotoDetails();
//        photo.setId(2);
//        photo.setName("photo");
//        photo.setType("jpeg");
//        photo.setPhoto("testBytes");
//
//        when(customersServiceClient.setOwnerPhoto(photo, owner.getId()))
//                .thenReturn(Mono.just("Image uploaded successfully: " + photo.getName()));
//
//
//        client.post()
//                .uri("/api/gateway/owners/photo/1")
//                .body(Mono.just(photo), PhotoDetails.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
//                .expectBody();
//
//    }
//    @Test
//    void getOwnerPhoto(){
//
//        OwnerResponseDTO owner = new OwnerResponseDTO();
//        owner.setId(1);
//        owner.setFirstName("John");
//        owner.setLastName("Smith");
//        owner.setAddress("456 Elm");
//        owner.setCity("Montreal");
//        owner.setTelephone("5553334444");
//        owner.setImageId(1);
//
//        final String test = "Test photo";
//        final byte[] testBytes = test.getBytes();
//
//        PhotoDetails photo = new PhotoDetails();
//        photo.setId(2);
//        photo.setName("photo");
//        photo.setType("jpeg");
//        photo.setPhoto("testBytes");
//
//        when(customersServiceClient.getOwnerPhoto(owner.getId()))
//                .thenReturn(Mono.just(photo));
//
//        client.get()
//                .uri("/api/gateway/owners/photo/1")
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo("photo")
//                .jsonPath("$.type").isEqualTo("jpeg");
////                .jsonPath("$.photo").isEqualTo(testBytes); --> need to fix
//
//
////        assertEquals(photo.getId(), 2);
//
//
//    }
//    @Test
//    void createPetPhoto(){
//
//        OwnerResponseDTO owner = new OwnerResponseDTO();
//        owner.setOwnerId("ownerId-123");
//        PetResponseDTO pet = new PetResponseDTO();
//        PetType type = new PetType();
//        type.setName("Cat");
//        pet.setPetId("petId-123");
//        pet.setName("Bonkers");
//        pet.setBirthDate("2015-03-03");
//        pet.setType(type);
//        pet.setImageId(2);
//
//        final String test = "Test photo";
//        final byte[] testBytes = test.getBytes();
//
//        PhotoDetails photo = new PhotoDetails();
//        photo.setId(2);
//        photo.setName("photo");
//        photo.setType("jpeg");
//        photo.setPhoto("testBytes");
//
//        when(customersServiceClient.setPetPhoto(owner.getOwnerId(), photo, pet.getPetId()))
//                .thenReturn(Mono.just("Image uploaded successfully: " + photo.getName()));
//
//        client.post()
//                .uri("/api/gateway/owners/1/pet/photo/1")
//                .body(Mono.just(photo), PhotoDetails.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
//                .expectBody();
//    }
//    //
//    @Test
//    void getPetPhoto(){
//
//        OwnerResponseDTO owner = new OwnerResponseDTO();
//        owner.setOwnerId("ownerId-1234");
//        PetResponseDTO pet = new PetResponseDTO();
//        PetType type = new PetType();
//        type.setName("Cat");
//        pet.setPetId("petId-123");
//        pet.setName("Bonkers");
//        pet.setBirthDate("2015-03-03");
//        pet.setType(type);
//        pet.setImageId(2);
//
//        final String test = "Test photo";
//        final byte[] testBytes = test.getBytes();
//
//        PhotoDetails photo = new PhotoDetails();
//        photo.setId(2);
//        photo.setName("photo");
//        photo.setType("jpeg");
//        photo.setPhoto("testBytes");
//
//        when(customersServiceClient.getPetPhoto(owner.getOwnerId(), pet.getPetId()))
//                .thenReturn(Mono.just(photo));
//
//        client.get()
//                .uri("/api/gateway/owners/"+ owner.getOwnerId() +"/pet/photo/" + pet.getPetId() )
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.name").isEqualTo("photo")
//                .jsonPath("$.type").isEqualTo("jpeg");
//    }
//    //
////
//    @Test
//    void shouldThrowUnsupportedMediaTypeIfBodyDoesNotExist(){
//        OwnerResponseDTO od = new OwnerResponseDTO();
//        od.setOwnerId("ownerId-21");
//        PetResponseDTO pet = new PetResponseDTO();
//        PetType type = new PetType();
//        type.setName("Dog");
//        pet.setPetId("petId-123");
//        pet.setName("Fluffy");
//        pet.setBirthDate("2000-01-01");
//        pet.setType(type);
//
//        when(customersServiceClient.createPet(pet,od.getOwnerId()))
//                .thenReturn(Mono.just(pet));
//
//        client.post()
//                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(UNSUPPORTED_MEDIA_TYPE)
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.message").isEqualTo("Content type '' not supported");
//
//
//    }
    //
    @Test
    void ifOwnerIdIsNotSpecifiedInUrlThrowNotAllowed(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("30-30-30-30");
        pet.setOwnerId("ownerId-12345");
        pet.setName("Fluffy");
        pet.setBirthDate(date);
        pet.setPetTypeId("5");
        pet.setIsActive("true");

        when(customersServiceClient.createPet(pet,od.getOwnerId()))
                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/pets")
                .body(Mono.just(pet), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(METHOD_NOT_ALLOWED)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Request method 'POST' is not supported.");
    }
    //

    /*
    @Test
    void shouldCreateThenDeletePet(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-65");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("petId-123");
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getOwnerId()))

                .thenReturn(Mono.just(pet));


        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())
                .body(Mono.just(pet), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}",od.getOwnerId(), pet.getPetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();


    }

    */
    //
    @Test
    void shouldThrowNotFoundWhenOwnerIdIsNotSpecifiedOnDeletePets(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-24");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("30-30-30-30");
        pet.setOwnerId("ownerId-12345");
        pet.setName("Fluffy");

        pet.setBirthDate(date);
        pet.setPetTypeId("5");
        pet.setIsActive("true");


        when(customersServiceClient.createPet(pet,od.getOwnerId()))

                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())
                .body(Mono.just(pet), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        client.delete()
                .uri("/api/gateway/owners/pets/{petId}", pet.getPetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody();
    }
    //
    @Test
    void shouldThrowMethodNotAllowedWhenDeletePetsIsMissingPetId(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-20");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("30-30-30-30");
        pet.setOwnerId("ownerId-12345");
        pet.setName("Fluffy");
        pet.setBirthDate(date);
        pet.setPetTypeId("5");
        pet.setIsActive("true");


        when(customersServiceClient.createPet(pet,od.getOwnerId()))
                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())
                .body(Mono.just(pet), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(METHOD_NOT_ALLOWED)
                .expectBody();
    }

//
//    @Test
//    void deleteUser() {
//        UserDetails user = new UserDetails();
//        user.setId(1);
//        user.setUsername("johndoe");
//        user.setPassword("pass");
//        user.setEmail("johndoe2@gmail.com");
//
//        when(authServiceClient.createUser(argThat(
//                n -> user.getEmail().equals(n.getEmail())
//        )))
//                .thenReturn(Mono.just(user));
//
//        client.post()
//                .uri("/api/gateway/users")
//                .body(Mono.just(user), UserDetails.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody();
//
//        assertEquals(1, user.getId());
//
//        client.delete()
//                .uri("/api/gateway/users/1")
//                .header("Authorization", "Bearer token")
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody();
//
//        assertEquals(null, authServiceClient.getUser(user.getId()));
//    }





    //private static final int BILL_ID = 1;

    @Test
    void shouldGetAllBills() {
        BillResponseDTO billResponseDTO = new BillResponseDTO("BillUUID","1","Test type","1",null,25.00, BillStatus.PAID);


        BillResponseDTO billResponseDTO2 = new BillResponseDTO("BillUUID2","2","Test type","2",null,27.00, BillStatus.UNPAID);
        when(billServiceClient.getAllBilling()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllBilling();
    }

    @Test
    void shouldGetAllPaidBills() {
        BillResponseDTO billResponseDTO = new BillResponseDTO("BillUUID","1","Test type","1",null,25.00, BillStatus.PAID);

        BillResponseDTO billResponseDTO2 = new BillResponseDTO("BillUUID2","2","Test type","2",null,27.00, BillStatus.PAID);
        when(billServiceClient.getAllPaidBilling()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills/paid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllPaidBilling();
    }

    @Test
    void shouldGetAllUnpaidBills() {
        BillResponseDTO billResponseDTO = new BillResponseDTO("BillUUID","1","Test type","1",null,25.00, BillStatus.UNPAID);

        BillResponseDTO billResponseDTO2 = new BillResponseDTO("BillUUID2","2","Test type","2",null,27.00, BillStatus.UNPAID);
        when(billServiceClient.getAllUnpaidBilling()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills/unpaid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllUnpaidBilling();
    }

    @Test
    void shouldGetAllOverdueBills() {
        BillResponseDTO billResponseDTO = new BillResponseDTO("BillUUID","1","Test type","1",null,25.00, BillStatus.OVERDUE);

        BillResponseDTO billResponseDTO2 = new BillResponseDTO("BillUUID2","2","Test type","2",null,27.00, BillStatus.OVERDUE);
        when(billServiceClient.getAllOverdueBilling()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills/overdue")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllOverdueBilling();
    }

    @Test
    public void getBillById(){

        //int expectedLength = 1;

        BillResponseDTO entity = new BillResponseDTO();

        entity.setBillId("9");

        entity.setAmount(599);

        entity.setCustomerId("2");

        entity.setVisitType("Consultation");

        when(billServiceClient.getBilling("9"))
                .thenReturn(Mono.just(entity));

        client.get()
                //check the URI
                .uri("/api/gateway/bills/9")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.billId").isEqualTo("9")
                .jsonPath("$.customerId").isEqualTo(entity.getCustomerId())
                .jsonPath("$.visitType").isEqualTo(entity.getVisitType())
                .jsonPath("$.amount").isEqualTo(entity.getAmount());




        assertEquals(entity.getBillId(), "9");


    }

    @Test
    public void getBillsByOwnerId(){
        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId(UUID.randomUUID().toString());
        bill.setCustomerId("1");
        bill.setAmount(499);
        bill.setVisitType("Test");

        when(billServiceClient.getBillsByOwnerId(bill.getCustomerId()))
                .thenReturn(Flux.just(bill));

        client.get()
                .uri("/api/gateway/bills/customer/{customerId}", bill.getCustomerId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });
    }

    @Test
    public void getBillsByVetId(){
        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId(UUID.randomUUID().toString());
        bill.setVetId("1");
        bill.setAmount(499);
        bill.setVisitType("Test");

        when(billServiceClient.getBillsByVetId(bill.getVetId()))
                .thenReturn(Flux.just(bill));

        client.get()
                .uri("/api/gateway/bills/vet/{vetId}", bill.getVetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });

    }
    @Test
    void getBillingByRequestMissingPath(){
        client.get()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void getBillNotFound(){
        client.get()
                .uri("/bills/{billId}", 100)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills/100")
                .jsonPath("$.message").isEqualTo(null);
    }



    @Test
    void getPutRequestNotFound(){
        client.put()
                .uri("/owners/{ownerId}", 100)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/owners/100")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void getPutRequestMissingPath(){
        client.put()
                .uri("/owners")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/owners")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void createBill(){
        BillResponseDTO billResponseDTO = new BillResponseDTO();
        billResponseDTO.setBillId("9");
        billResponseDTO.setDate(null);
        billResponseDTO.setAmount(600);
        billResponseDTO.setVisitType("Adoption");

        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setDate(null);
        billRequestDTO.setAmount(600);
        billRequestDTO.setVisitType("Adoption");
        when(billServiceClient.createBill(billRequestDTO))
                .thenReturn(Mono.just(billResponseDTO));

        client.post()
                .uri("/api/gateway/bills")
                .body(Mono.just(billRequestDTO), BillRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(billResponseDTO.getBillId(),"9");
    }

    @Test
    void getPutBillingRequestNotFound(){
        client.put()
                .uri("/bills/{billId}", 100)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills/100")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void getPutBillingMissingPath(){
        client.put()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills")
                .jsonPath("$.message").isEqualTo(null);
    }


    @Test
    void shouldDeleteBillById(){
        when(billServiceClient.deleteBill("9"))
                .thenReturn(Mono.empty());
        client.delete()
                .uri("/api/gateway/bills/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(billServiceClient, times(1)).deleteBill("9");
    }

    @Test
    void shouldDeleteBillsByVetId() {
        when(billServiceClient.deleteBillsByVetId("9"))
                .thenReturn(Flux.empty());
        client.delete()
                .uri("/api/gateway/bills/vet/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }


    /**
     * Visits Methods
     * **/
    String VISIT_ID = buildVisitResponseDTO().getVisitId();


//todo fix
    /*@Test
    void shouldCreateAVisitWithOwnerInfo(){
        OwnerResponseDTO owner = new OwnerResponseDTO();
        VisitRequestDTO visit = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2021-12-12T14:00:00"))
                .description("Charle's Richard cat has a paw infection.")
                .petId("1")
                .practitionerId("1")
                .status(false)
                .build();

        VisitResponseDTO visitResponseDTO =  VisitResponseDTO.builder()
                .visitId(VISIT_ID)
                .visitDate(LocalDateTime.parse("2021-12-12T14:00:00"))
                .petId("1")
                .description("Charle's Richard cat has a paw infection.")
                .practitionerId("1")
                .status(false)
                .build();


        when(visitsServiceClient.createVisitForPet(visit))
                .thenReturn(Mono.just(visitResponseDTO));


        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", owner.getOwnerId(), visit.getPetId())
                .body(Mono.just(visit), VisitDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo("1")
                .jsonPath("$.visitDate").isEqualTo("2021-12-12T14:00:00")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);
<<<<<<< HEAD
    }
=======
    }
>>>>>>> ceef8eff (VetDTO split in apigateway)


    @Test
    void shouldDeleteAVisit() {
        VisitDetails visit = new VisitDetails();
        OwnerDetails owner = new OwnerDetails();
        owner.setId(1);
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);


<<<<<<< HEAD
<<<<<<< HEAD
        when(visitsServiceClient.createVisitForPet(visit))
                .thenReturn(Mono.just(visit));

        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", owner.getId(), visit.getPetId())
                .body(Mono.just(visit), VisitDetails.class)
                .accept(MediaType.APPLICATION_JSON)
=======
=======
>>>>>>> 0ee1ef7f (Merge conflicts part 3)
 @Test
=======
 @Test
>>>>>>> ceef8eff (VetDTO split in apigateway)
    void shouldUpdateAVisitsById() {
        VisitDetails visitDetailsToUpdate = VisitDetails.builder()
                .visitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Charle's Richard dog has a paw infection.")
                .petId("1")
                .visitId("1")
                .practitionerId(2)
                .status(Status.UPCOMING)
                .build();

        when(visitsServiceClient.updateVisitForPet(visitDetailsToUpdate))
                .thenReturn(Mono.just(visitDetailsToUpdate));

        client.put()
<<<<<<< HEAD
                 .uri("/api/gateway/owners/*
pets/{petId}/visits/{visitId}", "1", "1")
 .accept(MediaType.APPLICATION_JSON)
=======
                 .uri("/api/gateway/owners/*
pets/{petId}/visits/{visitId}", "1", "1")
               */
/* .accept(MediaType.APPLICATION_JSON)
>>>>>>> ceef8eff (VetDTO split in apigateway)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(visitDetailsToUpdate)
>>>>>>> e83dff95 (Merge conflicts part 2)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
<<<<<<< HEAD
                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$.petId").isEqualTo(1)
                .jsonPath("$.date").isEqualTo("2021-12-12")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);
=======
                .jsonPath("$.visitId").isEqualTo("1")
                .jsonPath("$.petId").isEqualTo("1")
                .jsonPath("$.description").isEqualTo("Charle's Richard dog has a paw infection.")
                .jsonPath("$.status").isEqualTo(Status.UPCOMING.toString())
                .jsonPath("$.practitionerId").isEqualTo(2);
        Mockito.verify(visitsServiceClient,times(1)).updateVisitForPet(visitDetailsToUpdate);
<<<<<<< HEAD
    }
<<<<<<< HEAD
>>>>>>> e83dff95 (Merge conflicts part 2)
=======
=======
    }
>>>>>>> ceef8eff (VetDTO split in apigateway)
>>>>>>> 0ee1ef7f (Merge conflicts part 3)

        client.delete()
                .uri("/api/gateway/visits/{visitId}", visit.getVisitId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, visitsServiceClient.getVisitsForPet(visit.getPetId()));
    }

     */

//    @Test
//    void shouldUpdateAVisitsById() {
//        VisitDetails visitDetailsToUpdate = VisitDetails.builder()
//                .visitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
//                .description("Charle's Richard dog has a paw infection.")
//                .petId("1")
//                .visitId("1")
//                .practitionerId(2)
//                .status(Status.UPCOMING)
//                .build();
//
//        when(visitsServiceClient.updateVisitForPet(visitDetailsToUpdate))
//                .thenReturn(Mono.just(visitDetailsToUpdate));
//
//        client.put()
//                .uri("/api/gateway/owners/*/pets/{petId}/visits/{visitId}", "1", "1")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(visitDetailsToUpdate)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.visitId").isEqualTo("1")
//                .jsonPath("$.petId").isEqualTo("1")
//                .jsonPath("$.description").isEqualTo("Charle's Richard dog has a paw infection.")
//                .jsonPath("$.status").isEqualTo(Status.UPCOMING.toString())
//                .jsonPath("$.practitionerId").isEqualTo(2);
//        Mockito.verify(visitsServiceClient,times(1)).updateVisitForPet(visitDetailsToUpdate);
//    }

    @Test
    void ShouldUpdateStatusForVisitByVisitId(){
        String status = "CANCELLED";
        VisitResponseDTO visit = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00"))
                .description("this is a dummy description")
                .practitionerId("2")
                .petId("2")
                .status(Status.CANCELLED)
                .build();
        String visitId = visit.getVisitId();
        when(visitsServiceClient.updateStatusForVisitByVisitId(anyString(), anyString()))
                .thenReturn(Mono.just(visit));

        client.put()
                .uri("/api/gateway/visits/"+visitId+"/status/"+status)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$.practitionerId").isEqualTo(visit.getPractitionerId())
                .jsonPath("$.petId").isEqualTo(visit.getPetId())
                .jsonPath("$.description").isEqualTo(visit.getDescription())
                .jsonPath("$.visitDate").isEqualTo("2022-11-25 13:45")
                .jsonPath("$.status").isEqualTo("CANCELLED");

        Mockito.verify(visitsServiceClient, times(1))
                .updateStatusForVisitByVisitId(anyString(), anyString());
    }
    @Test
    void shouldGetAllVisits() {
        VisitResponseDTO visitResponseDTO = new VisitResponseDTO("73b5c112-5703-4fb7-b7bc-ac8186811ae1", LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "this is a dummy description", "2", "2", Status.UPCOMING);
        VisitResponseDTO visitResponseDTO2 = new VisitResponseDTO("73b5c112-5703-4fb7-b7bc-ac8186811ae1", LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "this is a dummy description", "2", "2", Status.UPCOMING);
        when(visitsServiceClient.getAllVisits()).thenReturn(Flux.just(visitResponseDTO,visitResponseDTO2));

        client.get()
                .uri("/api/gateway/visits")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(visitsServiceClient,times(1)).getAllVisits();
    }
    @Test
    void getVisitsByOwnerId_shouldReturnOk(){
        //arrange
        final String ownerId = "ownerId";
        PetResponseDTO petResponseDTO1 = PetResponseDTO.builder().petId("petId1").build();
        PetResponseDTO petResponseDTO2 = PetResponseDTO.builder().petId("petId2").build();
        VisitResponseDTO visitResponseDTO1 = VisitResponseDTO.builder().visitId("visitId1").petId("petId1").build();
        VisitResponseDTO visitResponseDTO2 = VisitResponseDTO.builder().visitId("visitId2").petId("petId1").build();
        VisitResponseDTO visitResponseDTO3 = VisitResponseDTO.builder().visitId("visitId3").petId("petId2").build();
        VisitResponseDTO visitResponseDTO4 = VisitResponseDTO.builder().visitId("visitId4").petId("petId2").build();
        VisitResponseDTO visitResponseDTO5 = VisitResponseDTO.builder().visitId("visitId5").petId("petId1").build();

        Mockito.when(customersServiceClient.getPetsByOwnerId(anyString())).thenReturn(Flux.just(petResponseDTO1, petResponseDTO2));

        Mockito.when(visitsServiceClient.getVisitsForPet(petResponseDTO1.getPetId())).thenReturn(Flux.just(visitResponseDTO1, visitResponseDTO2, visitResponseDTO5));
        Mockito.when(visitsServiceClient.getVisitsForPet(petResponseDTO2.getPetId())).thenReturn(Flux.just(visitResponseDTO3, visitResponseDTO4));

        //act and assert
        client
                .get()
                .uri("/api/gateway/visits/owners/{ownerId}",ownerId)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list) -> {
                    Assertions.assertNotNull(list);
                    Assertions.assertEquals(5, list.size());
                });
    }
    @Test
    void shouldGetAVisit() {
        VisitResponseDTO visit = new VisitResponseDTO("73b5c112-5703-4fb7-b7bc-ac8186811ae1", LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "this is a dummy description", "2", "2", Status.UPCOMING);

        when(visitsServiceClient.getVisitsForPet(visit.getPetId()))
                .thenReturn(Flux.just(visit));

        client.get()
                .uri("/api/gateway/visits/pets/{petId}", visit.getPetId())
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value((list)-> {
                    assertEquals(list.size(),1);
                    assertEquals(list.get(0).getVisitId(),visit.getVisitId());
                    assertEquals(list.get(0).getPetId(),visit.getPetId());
                    assertEquals(list.get(0).getVisitDate(),visit.getVisitDate());
                    assertEquals(list.get(0).getDescription(),visit.getDescription());
                    assertEquals(list.get(0).getStatus(),visit.getStatus());
                    assertEquals(list.get(0).getPractitionerId(),visit.getPractitionerId());
                });
    }
/*
    @Test
    void shouldGetAVisitForPractitioner(){
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId("1");
        visit.setVisitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(Status.UPCOMING);
        visit.setPractitionerId(1);

        when(visitsServiceClient.getVisitForPractitioner(Integer.parseInt(visit.getPetId())))
                .thenReturn(Flux.just(visit));

        client.get()
                .uri("/api/gateway/visits/vets/{practitionerId}", visit.getPractitionerId())
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(VisitDetails.class)
                .value((list)-> {
                    assertEquals(list.size(),1);
                    assertEquals(list.get(0).getVisitId(),visit.getVisitId());
                    assertEquals(list.get(0).getPetId(),visit.getPetId());
                    assertEquals(list.get(0).getVisitDate(),visit.getVisitDate());
                    assertEquals(list.get(0).getDescription(),visit.getDescription());
                    assertEquals(list.get(0).getStatus(),visit.getStatus());
                    assertEquals(list.get(0).getPractitionerId(),visit.getPractitionerId());
                });
    }
<<<<<<< HEAD
<<<<<<< HEAD

=======
>>>>>>> e83dff95 (Merge conflicts part 2)
=======
=======
>>>>>>> ceef8eff (VetDTO split in apigateway)
>>>>>>> 0ee1ef7f (Merge conflicts part 3)
    @Test
    void shouldGetAVisitByPractitionerIdAndMonth(){
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        when(visitsServiceClient.getVisitsByPractitionerIdAndMonth(visit.getPractitionerId(), "start", "end"))
                .thenReturn(Flux.just(visit));

        client.get()
                .uri("/api/gateway/visits/calendar/{practitionerId}?dates={startDate},{endDate}", visit.getPractitionerId(), "start", "end")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$[0].petId").isEqualTo(1)
                .jsonPath("$[0].date").isEqualTo("2021-12-12")
                .jsonPath("$[0].description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$[0].practitionerId").isEqualTo(1);
    }

     */

    @Test
    void getSingleVisit_Valid() {
        VisitResponseDTO visitResponseDTO = new VisitResponseDTO("73b5c112-5703-4fb7-b7bc-ac8186811ae1", LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "this is a dummy description", "2", "2", Status.UPCOMING);
        when(visitsServiceClient.getVisitByVisitId(anyString())).thenReturn(Mono.just(visitResponseDTO));

        client.get()
                .uri("/api/gateway/visits/{visitId}", visitResponseDTO.getVisitId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo(visitResponseDTO.getPetId())
                .jsonPath("$.visitDate").isEqualTo("2022-11-25 13:45")
                .jsonPath("$.description").isEqualTo(visitResponseDTO.getDescription())
                .jsonPath("$.practitionerId").isEqualTo(visitResponseDTO.getPractitionerId());
    }

//    @Test
    //    void getSingleVisit_Invalid() {
    //        final String invalidVisitId = "invalid";
    //        final String expectedErrorMessage = "error message";
    //
    //        when(visitsServiceClient.getVisitByVisitId(invalidVisitId))
    //                .thenThrow(new GenericHttpException(expectedErrorMessage, BAD_REQUEST));
    //
    //        client.get()
    //                .uri("/api/gateway/visit/{visitId}", invalidVisitId)
    //                .exchange()
    //                .expectStatus().isBadRequest()
    //                .expectBody()
    //                .jsonPath("$.statusCode").isEqualTo(BAD_REQUEST.value())
    //                .jsonPath("$.timestamp").exists()
    //                .jsonPath("$.message").isEqualTo(expectedErrorMessage);
    //    }

    /*@Test
    @DisplayName("Should get the previous visits of a pet")
    void shouldGetPreviousVisitsOfAPet() {
        VisitDetails visit1 = new VisitDetails();
        VisitDetails visit2 = new VisitDetails();
        visit1.setVisitId(UUID.randomUUID().toString());
        visit1.setPetId("21");
        visit1.setVisitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit1.setDescription("John Smith's cat has a paw infection.");
        visit1.setStatus(Status.COMPLETED);
        visit1.setPractitionerId(2);
        visit2.setVisitId(UUID.randomUUID().toString());
        visit2.setPetId("21");
        visit2.setVisitDate(LocalDateTime.parse("2022-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit2.setDescription("John Smith's dog has a paw infection.");
        visit2.setStatus(Status.COMPLETED);
        visit2.setPractitionerId(2);

        List<VisitDetails> previousVisitsList = new ArrayList<>();
        previousVisitsList.add(visit1);
        previousVisitsList.add(visit2);

        Flux<VisitDetails> previousVisits = Flux.fromIterable(previousVisitsList);

        when(visitsServiceClient.getPreviousVisitsForPet("21"))
                .thenReturn(previousVisits);

        client.get()
                .uri("/api/gateway/visits/previous/{petId}", 21)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitDetails.class)
                .value((list) -> {
                    assertEquals(list.size(), 2);
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getStatus(), visit1.getStatus());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(1).getVisitId(), visit2.getVisitId());
                    assertEquals(list.get(1).getPetId(), visit2.getPetId());
                    assertEquals(list.get(1).getVisitDate(), visit2.getVisitDate());
                    assertEquals(list.get(1).getDescription(), visit2.getDescription());
                    assertEquals(list.get(1).getStatus(), visit2.getStatus());
                    assertEquals(list.get(1).getPractitionerId(), visit2.getPractitionerId());
                });
    }*/

    /*@Test
    @DisplayName("Should return a bad request if the petId is invalid when trying to get the previous visits of a pet")
    void shouldGetBadRequestWhenInvalidPetIdToRetrievePreviousVisits() {
        final String invalidPetId = "-1";
        final String expectedErrorMessage = "error message";

        when(visitsServiceClient.getPreviousVisitsForPet(invalidPetId))
                .thenThrow(new GenericHttpException(expectedErrorMessage, BAD_REQUEST));

        client.get()
                .uri("/api/gateway/visits/previous/{petId}", invalidPetId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.message").isEqualTo(expectedErrorMessage);
    }

    @Test
    void shouldGetScheduledVisitsOfAPet() {
        VisitDetails visit1 = new VisitDetails();
        VisitDetails visit2 = new VisitDetails();
        visit1.setVisitId(UUID.randomUUID().toString());
        visit1.setPetId("21");
        visit1.setVisitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit1.setDescription("John Smith's cat has a paw infection.");
        visit1.setStatus(Status.UPCOMING);
        visit1.setPractitionerId(2);
        visit2.setVisitId(UUID.randomUUID().toString());
        visit2.setPetId("21");
        visit2.setVisitDate(LocalDateTime.parse("2022-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit2.setDescription("John Smith's dog has a paw infection.");
        visit2.setStatus(Status.UPCOMING);
        visit2.setPractitionerId(2);

        List<VisitDetails> scheduledVisitsList = new ArrayList<>();
        scheduledVisitsList.add(visit1);
        scheduledVisitsList.add(visit2);

        Flux<VisitDetails> scheduledVisits = Flux.fromIterable(scheduledVisitsList);

        when(visitsServiceClient.getScheduledVisitsForPet("21"))
                .thenReturn(scheduledVisits);

        client.get()
                .uri("/api/gateway/visits/scheduled/{petId}", 21)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitDetails.class)
                .value((list) -> {
                    assertEquals(list.size(), 2);
                    assertEquals(list.get(0).getVisitId(), visit1.getVisitId());
                    assertEquals(list.get(0).getPetId(), visit1.getPetId());
                    assertEquals(list.get(0).getVisitDate(), visit1.getVisitDate());
                    assertEquals(list.get(0).getDescription(), visit1.getDescription());
                    assertEquals(list.get(0).getStatus(), visit1.getStatus());
                    assertEquals(list.get(0).getPractitionerId(), visit1.getPractitionerId());
                    assertEquals(list.get(1).getVisitId(), visit2.getVisitId());
                    assertEquals(list.get(1).getPetId(), visit2.getPetId());
                    assertEquals(list.get(1).getVisitDate(), visit2.getVisitDate());
                    assertEquals(list.get(1).getDescription(), visit2.getDescription());
                    assertEquals(list.get(1).getStatus(), visit2.getStatus());
                    assertEquals(list.get(1).getPractitionerId(), visit2.getPractitionerId());
                });

                @Test
    @DisplayName("Should return a bad request if the petId is invalid when trying to get the scheduled visits of a pet")
    void shouldGetBadRequestWhenInvalidPetIdToRetrieveScheduledVisits() {
        final String invalidPetId = "0";
        final String expectedErrorMessage = "error message";

        when(visitsServiceClient.getScheduledVisitsForPet(invalidPetId))
                .thenThrow(new GenericHttpException(expectedErrorMessage, BAD_REQUEST));

        client.get()
                .uri("/api/gateway/visits/scheduled/{petId}", invalidPetId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.message").isEqualTo(expectedErrorMessage);
    }
    }*/

    @Test
    public void updateOwner_shouldSucceed() {
        // Define the owner ID and updated owner data
        String ownerId = "ownerId-123";
        OwnerRequestDTO updatedOwnerData = new OwnerRequestDTO();
        updatedOwnerData.setFirstName("UpdatedFirstName");
        updatedOwnerData.setLastName("UpdatedLastName");

        // Mock the behavior of customersServiceClient.updateOwner
        OwnerResponseDTO updatedOwner = new OwnerResponseDTO();
        updatedOwner.setOwnerId(ownerId);
        updatedOwner.setFirstName(updatedOwnerData.getFirstName());
        updatedOwner.setLastName(updatedOwnerData.getLastName());
        when(customersServiceClient.updateOwner(eq(ownerId), any(Mono.class)))
                .thenReturn(Mono.just(updatedOwner));

        // Perform the PUT request to update the owner
        client.put()
                .uri("/api/gateway/owners/{ownerId}", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedOwnerData))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(updatedOwnerResponseDTO -> {
                    // Assertions
                    assertNotNull(updatedOwnerResponseDTO);
                    assertEquals(updatedOwnerResponseDTO.getOwnerId(), ownerId);
                    assertEquals(updatedOwnerResponseDTO.getFirstName(), updatedOwnerData.getFirstName());
                    assertEquals(updatedOwnerResponseDTO.getLastName(), updatedOwnerData.getLastName());
                    // Add more assertions if needed
                });
    }




    @Test
    void deleteVisitById_visitId_shouldSucceed(){
        when(visitsServiceClient.deleteVisitByVisitId(VISIT_ID)).thenReturn(Mono.empty());
        client.delete()
                .uri("/api/gateway/visits/" + VISIT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(visitsServiceClient, times(1))
                .deleteVisitByVisitId(VISIT_ID);

    }

    @Test
    void deleteVisitById_visitId_shouldFailWithNotFoundException(){
        // Mocking visitsServiceClient to throw a NotFoundException
        String invalidId = "fakeId";
        when(visitsServiceClient.deleteVisitByVisitId(invalidId)).thenReturn(Mono.error(new NotFoundException("Visit not found")));

        client.delete()
                .uri("/api/gateway/visits/" + invalidId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // Expecting a 404 status code

        Mockito.verify(visitsServiceClient, times(1))
                .deleteVisitByVisitId(invalidId);
    }

    @Test
    void deleteAllCancelledVisits_shouldSucceed(){

        when(visitsServiceClient.deleteAllCancelledVisits()).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/visits/cancelled")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
        Mockito.verify(visitsServiceClient, times(1))
                .deleteAllCancelledVisits();

    }

    @Test
    void deleteAllCancelledVisits_shouldThrowRuntimeException(){

        when(visitsServiceClient.deleteAllCancelledVisits())
                .thenReturn(Mono.error(new RuntimeException("Failed to delete cancelled visits")));

        client.delete()
                .uri("/api/gateway/visits/cancelled")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        Mockito.verify(visitsServiceClient, times(1)).deleteAllCancelledVisits();

    }

    private VisitResponseDTO buildVisitResponseDTO(){
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(UUID.randomUUID().toString())
                .status(Status.UPCOMING)
                .build();
    }


    /**
     * End of Visits Methods
     * **/



    @Test
    @DisplayName("Given valid JWT, verify user with redirection")
    void verify_user_with_redirection_shouldSucceed(){
        final String validToken = "some.valid.token";

        // Mocking the behavior of authServiceClient.verifyUser to return a successful response
        UserDetails user = UserDetails.builder()
                .userId("22222")
                .email("e@mail.com")
                .username("user")
                .roles(Collections.emptySet())
                .build();

        ResponseEntity<UserDetails> responseEntity = ResponseEntity.ok(user);

        when(authServiceClient.verifyUser(validToken))
                .thenReturn(Mono.just(responseEntity));

        client.get()
                .uri("/api/gateway/verification/{token}", validToken)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "http://localhost:8080/#!/login");
    }

//    @Test
//    @DisplayName("Given invalid JWT, expect 400")
//    void verify_user_bad_token() {
//
//        final String errorMessage = "some error message";
//        final String invalidToken = "some.invalid.token";
//
//        when(authServiceClient.verifyUser(invalidToken))
//                .thenThrow(new GenericHttpException(errorMessage, BAD_REQUEST));
//
//        client.get()
//                .uri("/api/gateway/verification/{token}", invalidToken)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody()
//                .jsonPath("$.statusCode").isEqualTo(BAD_REQUEST.value())
//                .jsonPath("$.timestamp").exists()
//                .jsonPath("$.message").isEqualTo(errorMessage);
//    }

    @Test
    @DisplayName("Given valid Login, return JWT and user details")
    void login_valid() throws Exception {
        final String validToken = "some.valid.token";
        final UserDetails user = UserDetails.builder()
                .email("e@mail.com")
                .username("user")
                .roles(Collections.emptySet())
                .build();

        UserPasswordLessDTO userPasswordLessDTO = UserPasswordLessDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();

        headers.put(HttpHeaders.COOKIE, Collections.singletonList("Bearer=" + validToken + "; Path=/; HttpOnly; SameSite=Lax"));

        Mono<ResponseEntity<UserPasswordLessDTO>> httpResponse = Mono.just(ResponseEntity.ok().headers(HttpHeaders.readOnlyHttpHeaders(headers)).body(userPasswordLessDTO));

        when(authServiceClient.login(any()))
                .thenReturn(httpResponse);

        when(authServiceClient.login(any()))
                .thenReturn(httpResponse);


        final Login login = Login.builder()
                .password("valid")
                .email(user.getEmail())
                .build();
        when(authServiceClient.login(any()))
                .thenReturn(
                        httpResponse
                );

        client.post()
                .uri("/api/gateway/users/login")
                .accept(APPLICATION_JSON)
                .body(Mono.just(login), Login.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserPasswordLessDTO.class)
                .value((res ->
                {
                    assertEquals(res.getEmail(),userPasswordLessDTO.getEmail());

                }));
    }

    @Test
    @DisplayName("Given invalid Login, throw 401")
    void login_invalid() throws Exception {
        final UserDetails user = UserDetails.builder()
                .email("e@mail.com")
                .username("user")
                .roles(Collections.emptySet())
                .build();

        final Login login = Login.builder()
                .password("valid")
                .email(user.getEmail())
                .build();
        final String message = "I live in unending agony. I spent 6 hours and ended up with nothing";
        when(authServiceClient.login(any()))
                .thenThrow(new GenericHttpException(message, UNAUTHORIZED));

        client.post()
                .uri("/api/gateway/users/login")
                .accept(APPLICATION_JSON)
                .body(Mono.just(login), Login.class)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(UNAUTHORIZED.value())
                .jsonPath("$.message").isEqualTo(message)
                .jsonPath("$.timestamp").exists();
    }



    private InventoryResponseDTO buildInventoryDTO(){
        return InventoryResponseDTO.builder()
                .inventoryId("1")
                .inventoryName("invt1")
                .inventoryType("Internal")
                .inventoryDescription("invtone")
                .build();
    }
    @Test
    void addInventory_withValidValue_shouldSucceed() {

        InventoryRequestDTO requestDTO = new InventoryRequestDTO("internal", "Internal", "invt1");

        InventoryResponseDTO inventoryResponseDTO = buildInventoryDTO();

        when(inventoryServiceClient.addInventory(any()))
                .thenReturn(Mono.just(inventoryResponseDTO));

        client.post()
                .uri("/api/gateway/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.inventoryId").isEqualTo(inventoryResponseDTO.getInventoryId())
                .jsonPath("$.inventoryName").isEqualTo(inventoryResponseDTO.getInventoryName())
                .jsonPath("$.inventoryType").isEqualTo("Internal")
                .jsonPath("$.inventoryDescription").isEqualTo("invtone");


        verify(inventoryServiceClient, times(1))
                .addInventory(any());
    }



    @Test
    void updateInventory_withValidValue_shouldSucceed() {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO("internal", "Internal", "newDescription");

        InventoryResponseDTO expectedResponse = InventoryResponseDTO.builder()
                .inventoryId("1")
                .inventoryName("newName")
                .inventoryType("Internal")
                .inventoryDescription("newDescription")
                .build();

        when(inventoryServiceClient.updateInventory(any(), eq(buildInventoryDTO().getInventoryId())))
                .thenReturn(Mono.just(expectedResponse));


        client.put()
                .uri("/api/gateway/inventory/{inventoryId}", buildInventoryDTO().getInventoryId()) // Use the appropriate URI
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.inventoryId").isEqualTo(expectedResponse.getInventoryId())
                .jsonPath("$.inventoryName").isEqualTo(expectedResponse.getInventoryName())
                .jsonPath("$.inventoryType").isEqualTo("Internal")
                .jsonPath("$.inventoryDescription").isEqualTo(expectedResponse.getInventoryDescription());

        verify(inventoryServiceClient, times(1))
                .updateInventory(any(), eq(buildInventoryDTO().getInventoryId()));
    }


    @Test
    void getInventoryByInventoryId_ValidId_shouldSucceed() {

        String validInventoryId = "inventoryId_1";
        InventoryResponseDTO inventoryResponseDTO = InventoryResponseDTO.builder()
                .inventoryId(validInventoryId)
                .inventoryName("Pet food")
                .inventoryType("Internal")
                .inventoryDescription("pet")
                .build();

        when(inventoryServiceClient.getInventoryById(validInventoryId))
                .thenReturn(Mono.just(inventoryResponseDTO));


        client.get()
                .uri("/api/gateway/inventory/{inventoryId}", validInventoryId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(InventoryResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(inventoryResponseDTO.getInventoryId(), dto.getInventoryId());
                    assertEquals(inventoryResponseDTO.getInventoryName(), dto.getInventoryName());
                    assertEquals(inventoryResponseDTO.getInventoryType(), dto.getInventoryType());
                    assertEquals(inventoryResponseDTO.getInventoryDescription(), dto.getInventoryDescription());
                });


        verify(inventoryServiceClient, times(1))
                .getInventoryById(validInventoryId);
    }


    //delete all product inventory and delete all inventory
    @Test
    void deleteAllInventory_shouldSucceed() {
        // Mock the service call to simulate the successful deletion of all inventories.
        // Assuming your service client has a method called `deleteAllInventories`.
        when(inventoryServiceClient.deleteAllInventories())
                .thenReturn(Mono.empty());  // Using Mono.empty() to simulate a void return (successful deletion without a return value).

        // Make the DELETE request to the API.
        client.delete()
                .uri("/api/gateway/inventory")  // Assuming the endpoint for deleting all inventories is the same without an ID.
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Verify that the deleteAllInventories method on the service client was called exactly once.
        verify(inventoryServiceClient, times(1))
                .deleteAllInventories();
    }

    @Test
    void deleteAllProductInventory_shouldSucceed() {
        // Assuming you want to test for a specific inventoryId
        String inventoryId = "someInventoryId";

        // Mock the service call to simulate the successful deletion of all product inventories for a specific inventoryId.
        // Adjust the method name if `deleteAllProductInventoriesForInventory` is not the correct name.
        when(inventoryServiceClient.deleteAllProductForInventory(eq(inventoryId)))
                .thenReturn(Mono.empty());  // Using Mono.empty() to simulate a void return (successful deletion without a return value).

        // Make the DELETE request to the API for a specific inventoryId.
        client.delete()
                .uri("/api/gateway/inventory/{inventoryId}/products", inventoryId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Verify that the deleteAllProductInventoriesForInventory method on the service client was called exactly once with the specific inventoryId.
        verify(inventoryServiceClient, times(1))
                .deleteAllProductForInventory(eq(inventoryId));
    }
    //inventory tests

    @Test
    void testUpdateProductInInventory() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100, 15.99);

        // Define the expected response
        ProductResponseDTO expectedResponse = ProductResponseDTO.builder()
                .id("sampleId")
                .productId("sampleProductId")
                .inventoryId("sampleInventoryId")
                .productName(requestDTO.getProductName())
                .productDescription(requestDTO.getProductDescription())
                .productPrice(requestDTO.getProductPrice())
                .productQuantity(requestDTO.getProductQuantity())
                .build();

        // Mock the behavior of the inventoryServiceClient
        when(inventoryServiceClient.updateProductInInventory(any(), anyString(), anyString()))
                .thenReturn(Mono.just(expectedResponse));

        // Perform the PUT request
        client.put()
                .uri("/api/gateway/inventory/{inventoryId}/products/{productId}", "sampleInventoryId", "sampleProductId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(dto ->{
                    assertNotNull(dto);
                    assertEquals(requestDTO.getProductName(),dto.getProductName());
                    assertEquals(requestDTO.getProductDescription(),dto.getProductDescription());
                    assertEquals(requestDTO.getProductPrice(),dto.getProductPrice());
                    assertEquals(requestDTO.getProductQuantity(),dto.getProductQuantity());
                });

        // Verify that the inventoryServiceClient method was called
        verify(inventoryServiceClient, times(1))
                .updateProductInInventory(eq(requestDTO), eq("sampleInventoryId"), eq("sampleProductId"));
    }
    public void deleteProductById_insideInventory(){
        ProductResponseDTO productResponseDTO = buildProductDTO();
        when(inventoryServiceClient.deleteProductInInventory(productResponseDTO.getInventoryId(), productResponseDTO.getProductId()))
                .thenReturn((Mono.empty()));

        client.delete()
                .uri("/api/gateway/inventory/{inventoryId}/products/{productId}",productResponseDTO.getInventoryId()  ,productResponseDTO.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(inventoryServiceClient, times(1))
                .deleteProductInInventory(productResponseDTO.getInventoryId(), productResponseDTO.getProductId());

    }

    @Test
    @DisplayName("Given valid inventoryId and valid productRequest Post and return productResponse")
    void testAddProductToInventory_ShouldSucceed() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100, 15.99);

        // Define the expected response
        ProductResponseDTO expectedResponse = ProductResponseDTO.builder()
                .id("sampleId")
                .productId("sampleProductId")
                .inventoryId("sampleInventoryId")
                .productName(requestDTO.getProductName())
                .productDescription(requestDTO.getProductDescription())
                .productPrice(requestDTO.getProductPrice())
                .productQuantity(requestDTO.getProductQuantity())
                .productSalePrice(requestDTO.getProductSalePrice())
                .build();

        // Mock the behavior of the inventoryServiceClient
        when(inventoryServiceClient.addProductToInventory(any(), anyString()))
                .thenReturn(Mono.just(expectedResponse));

        // Perform the POST request
        client.post()
                .uri("/api/gateway/inventory/{inventoryId}/products", "sampleInventoryId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseDTO.class)
                .value(dto -> {
                    assertNotNull(dto);
                    assertEquals(requestDTO.getProductName(), dto.getProductName());
                    assertEquals(requestDTO.getProductDescription(), dto.getProductDescription());
                    assertEquals(requestDTO.getProductPrice(), dto.getProductPrice());
                    assertEquals(requestDTO.getProductQuantity(), dto.getProductQuantity());
                    assertEquals(requestDTO.getProductSalePrice(), dto.getProductSalePrice());
                });

        // Verify that the inventoryServiceClient method was called
        verify(inventoryServiceClient, times(1))
                .addProductToInventory(eq(requestDTO), eq("sampleInventoryId"));
    }

    @Test
    @DisplayName("Given invalid inventoryId and valid productRequest Post and return NotFoundException")
    void testAddProductToInventory_InvalidInventoryId_ShouldReturnNotFoundException() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100, 15.99);

        // Define the expected response
        ProductResponseDTO expectedResponse = ProductResponseDTO.builder()
                .id("sampleId")
                .productId("sampleProductId")
                .inventoryId("sampleInventoryId")
                .productName(requestDTO.getProductName())
                .productDescription(requestDTO.getProductDescription())
                .productPrice(requestDTO.getProductPrice())
                .productQuantity(requestDTO.getProductQuantity())
                .build();

        // Mock the behavior of the inventoryServiceClient
        when(inventoryServiceClient.addProductToInventory(any(), anyString()))
                .thenThrow(new NotFoundException("Inventory not found"));

        // Perform the POST request
        client.post()
                .uri("/api/gateway/inventory/{inventoryId}/products", "invalidInventoryId")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(NOT_FOUND.value())
                .jsonPath("$.message").isEqualTo("Inventory not found")
                .jsonPath("$.timestamp").exists();

        // Verify that the inventoryServiceClient method was called
        verify(inventoryServiceClient, times(1))
                .addProductToInventory(eq(requestDTO), eq("invalidInventoryId"));
    }

    private ProductResponseDTO buildProductDTO(){
        return ProductResponseDTO.builder()
                .id("1")
                .inventoryId("1")
                .productId(UUID.randomUUID().toString())
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .build();
    }

    private VetResponseDTO buildVetResponseDTO() {
        return VetResponseDTO.builder()
                .vetId("181faeb5-c024-425c-9f08-663600008f06")
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
                .vetId("181faeb5-c024-425c-9f08-663600008f06")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(true)
                .build();
    }
    private VetRequestDTO buildVetRequestDTO() {
        return VetRequestDTO.builder()
                .vetId("181faeb5-c024-425c-9f08-663600008f06")
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
    private VetRequestDTO buildVetRequestDTO2() {
        return VetRequestDTO.builder()
                .vetId("181faeb5-c024-425c-9f08-663600008f06")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(true)
                .build();
    }

    private RatingResponseDTO buildRatingResponseDTO() {
        return RatingResponseDTO.builder()
                .ratingId("123456")
                .vetId("181faeb5-c024-425c-9f08-663600008f06")
                .rateScore(4.0)
                .build();
    }
    private VetAverageRatingDTO buildVetAverageRatingDTO(){
        return VetAverageRatingDTO.builder()
                .vetId("678910")
                .averageRating(2.0)
                .build();
    }
    @Test
    void sendForgottenEmail_ShouldSucceed(){
        final UserEmailRequestDTO dto = UserEmailRequestDTO.builder()
                .email("email")
                .build();

        ServerHttpRequest request = MockServerHttpRequest.post("http://localhost:8080").build();



        when(authServiceClient.sendForgottenEmail(Mono.just(dto)))
                .thenReturn(Mono.just(ResponseEntity.ok().build()));


        client.post()
                .uri("/api/gateway/users/forgot_password")
                .body(Mono.just(dto), UserEmailRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        verify(authServiceClient, times(1)).sendForgottenEmail(any());
    }


    @Test
    void sendForgottenEmail_ShouldFail(){
        final UserEmailRequestDTO dto = UserEmailRequestDTO.builder()
                .email("email")
                .build();

        ServerHttpRequest request = MockServerHttpRequest.post("http://localhost:8080").build();

        when(authServiceClient.sendForgottenEmail(any()))
                .thenThrow(new GenericHttpException("error",BAD_REQUEST));



        client.post()
                .uri("/api/gateway/users/forgot_password")
                .body(Mono.just(dto), UserEmailRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody();

        verify(authServiceClient, times(1)).sendForgottenEmail(any());
    }


    @Test
    void processResetPassword_ShouldSucceed(){
        final UserPasswordAndTokenRequestModel dto = UserPasswordAndTokenRequestModel.builder()
                .password("password")
                .token("Valid token")
                .build();


        when(authServiceClient.changePassword(any()))
                .thenReturn(Mono.just(ResponseEntity.ok().build()));


        client.post()
                .uri("/api/gateway/users/reset_password")
                .body(Mono.just(dto), UserPasswordAndTokenRequestModel.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        verify(authServiceClient, times(1)).changePassword(any());
    }

    @Test
    void getAllPetTypes_shouldSucceed(){
        PetTypeResponseDTO petType1 = new PetTypeResponseDTO();
        petType1.setPetTypeId("petTypeId-90");
        petType1.setName("Dog");
        petType1.setPetTypeDescription("Mammal");

        Flux<PetTypeResponseDTO> petTypeResponseDTOFlux = Flux.just(petType1);

        when(customersServiceClient.getAllPetTypes()).thenReturn(petTypeResponseDTOFlux);

        client.get()
                .uri("/api/gateway/owners/petTypes")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "application/json") // Change content type expectation
                .expectBodyList(PetTypeResponseDTO.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals(list.get(0).getPetTypeId(), petType1.getPetTypeId());
                    assertEquals(list.get(0).getName(), petType1.getName());
                });
    }



    @Test
    void createUser_withValidModel_shouldSucceed() {
        // Define a valid Register model here
        OwnerRequestDTO ownerRequestDTO = OwnerRequestDTO.builder()
                .ownerId("1")
                .firstName("Ric")
                .lastName("Danon")
                .build();

        OwnerResponseDTO ownerResponseDTO = OwnerResponseDTO.builder()
                .ownerId("1")
                .firstName("Ric")
                .lastName("Danon")
                .build();

        Register validUser = Register.builder()
                .email("richard200danon@gmail.com")
                .password("pwd%jfjfjDkkkk8")
                .username("Ric")
                .owner(ownerRequestDTO)
                .build();

        UserPasswordLessDTO userLess = UserPasswordLessDTO.builder()
                .username(validUser.getUsername())
                .email(validUser.getEmail())
                .build();

        when(authServiceClient.createUser(any()))
                .thenReturn(Mono.just(ownerResponseDTO));

        client.post()
                .uri("/api/gateway/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUser)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(dto ->{
                    assertNotNull(dto);
                    assertEquals(ownerRequestDTO.getFirstName(),dto.getFirstName());
                    assertEquals(ownerRequestDTO.getLastName(),dto.getLastName());
                    assertEquals(ownerRequestDTO.getOwnerId(),dto.getOwnerId());
                });
    }


    @Test
    void getAllUsers_ShouldReturn2(){
        UserDetails user1 = UserDetails.builder()
                .username("user1")
                .userId("jkbjbhjbllb")
                .email("email1")
                .build();

        UserDetails user2 = UserDetails.builder()
                .username("user2")
                .email("email2")
                .userId("hhvhvhvhuvul")
                .build();
        String validToken = "IamValidTrustMe";

        when(authServiceClient.getUsers(validToken))
                .thenReturn(Flux.just(user1,user2));

        client.get()
                .uri("/api/gateway/users")
                .cookie("Bearer",validToken)
                .exchange()
                .expectBodyList(UserDetails.class)
                .hasSize(2);
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

}
