package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.PaymentRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.dtos.Visits.Status;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static com.petclinic.bffapigateway.dtos.Bills.BillStatus.PAID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    ClassPathResource cpr=new ClassPathResource("static/images/full_food_bowl.png");
    ClassPathResource cpr2=new ClassPathResource("static/images/vet_default.jpg");

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
                .cookie("Bearer", "valid-jwt-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(vetsServiceClient, times(1))
                .deleteRating(VET_ID, ratingResponseDTO.getRatingId());
    }

    @Test
    void deleteVetRatingByCustomer() {
        String customerName = "Test Customer";
        when(vetsServiceClient.deleteRatingByCustomerName(VET_ID, customerName))
                .thenReturn((Mono.empty()));
        when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(ResponseEntity.ok(TokenResponseDTO.builder()
                        .userId("userId123")
                        .roles(List.of("OWNER"))
                        .token("bearer-token")
                        .build())));
        when(customersServiceClient.getOwner("userId123"))
                .thenReturn(Mono.just(OwnerResponseDTO.builder()
                        .firstName("Test")
                        .lastName("Customer")
                        .build()));

        client
                .delete()
                .uri("/api/gateway/vets/" + VET_ID + "/ratings/customer")
                .cookie("Bearer", "valid-jwt-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(vetsServiceClient, times(1))
                .deleteRatingByCustomerName(VET_ID, customerName);
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
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY);
                //  .expectHeader().contentType(MediaType.APPLICATION_JSON)
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
    void getRatingsBasedOnDate(){
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("year", "2023");


        when(vetsServiceClient.getRatingsOfAVetBasedOnDate(vetResponseDTO.getVetId(), queryParams))
                .thenReturn(Flux.just(buildRatingResponseDTO(),buildRatingResponseDTO2()));
        client
                .get()
                .uri("/api/gateway/vets/"+VET_ID+"/ratings/date?year="+queryParams.get("year"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].date").isEqualTo("2023");
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
                    assertThat(responseDTO.get(0).getWorkHoursJson()).isEqualTo(vetResponseDTO.getWorkHoursJson());
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
                .jsonPath("$.active").isEqualTo(vetResponseDTO.isActive())
                .jsonPath("$.workHoursJson").isEqualTo(vetResponseDTO.getWorkHoursJson());

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
                    assertThat(responseDTO.get(0).getWorkHoursJson()).isEqualTo(vetResponseDTO2.getWorkHoursJson());
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
                    assertThat(responseDTO.get(0).getWorkHoursJson()).isEqualTo(vetResponseDTO.getWorkHoursJson());
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
        VetResponseDTO deactivatedVet = VetResponseDTO.builder()
                .vetId(VET_ID)
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

        when(vetsServiceClient.deleteVet(anyString()))
                .thenReturn((Mono.just(deactivatedVet)));

        client
                .delete()
                .uri("/api/gateway/vets/" + VET_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VetResponseDTO.class)
                .value(responseDto -> {
                    assertNotNull(responseDto);
                    assertFalse(responseDto.isActive());
                    assertEquals(VET_ID, responseDto.getVetId());
                });

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
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
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
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
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
                .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
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
    void getDefaultPhotoByVetId() throws IOException {
        PhotoResponseDTO photoResponseDTO = PhotoResponseDTO.builder()
                .vetId(VET_ID)
                .filename("vet_default.jpg")
                .imgType("image/jpeg")
                .resourceBase64(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(cpr2.getInputStream())))
                .build();

        when(vetsServiceClient.getDefaultPhotoByVetId(anyString()))
                .thenReturn(Mono.just(photoResponseDTO));

        client.get()
                .uri("/api/gateway/vets/{vetId}/default-photo", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(PhotoResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertEquals(photoResponseDTO.getFilename(), responseDTO.getFilename());
                    Assertions.assertEquals(photoResponseDTO.getImgType(), responseDTO.getImgType());
                    Assertions.assertEquals(photoResponseDTO.getVetId(), responseDTO.getVetId());
                    Assertions.assertEquals(photoResponseDTO.getResourceBase64(), responseDTO.getResourceBase64());
                });

        Mockito.verify(vetsServiceClient, times(1))
                .getDefaultPhotoByVetId(VET_ID);
    }

    @Test
    void addPhotoToVet_multipart_ok() {
        String VET_ID = "some-vet-id";
        String PHOTO_NAME = "vet_photo.jpg";

        byte[] bytes = new byte[]{123, 23, 75, 34};
        Resource returnedPhoto = new ByteArrayResource(bytes);

        when(vetsServiceClient.addPhotoToVet(
                eq(VET_ID), eq(PHOTO_NAME), any(FilePart.class)))
                .thenReturn(Mono.just(returnedPhoto));

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DataBuffer buffer = factory.wrap(bytes);

        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        mb.part("file", buffer)
                .filename("photo.jpg")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mb.part("photoName", PHOTO_NAME);

        client.post()
                .uri("/api/gateway/vets/{vetId}/photos", VET_ID)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(mb.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(byte[].class)
                .isEqualTo(bytes);

        verify(vetsServiceClient, times(1))
                .addPhotoToVet(eq(VET_ID), eq(PHOTO_NAME), any(FilePart.class));
    }

    @Test
    void updatePhotoToVet() {
        byte[] photo = {123, 23, 75, 34};
        Resource resource = new ByteArrayResource(photo);

        when(vetsServiceClient.updatePhotoOfVet(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(resource));

        client.put()
                .uri("/api/gateway/vets/{vetId}/photos/{photoName}", VET_ID, "vet_photo.jpg")
                .body(Mono.just(resource), Resource.class)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG_VALUE)
                .expectBody(Resource.class)
                .consumeWith(response -> {
                    assertEquals(resource, response.getResponseBody());
                });

        Mockito.verify(vetsServiceClient, times(1))
                .updatePhotoOfVet(anyString(), anyString(), any(Mono.class));
    }

    @Test
    void getBadgeByVetId() throws IOException {
        BadgeResponseDTO badgeResponseDTO = BadgeResponseDTO.builder()
                .vetId(VET_ID)
                .badgeTitle(BadgeTitle.HIGHLY_RESPECTED)
                .badgeDate("2017")
                .resourceBase64(Base64.getEncoder().encodeToString(StreamUtils.copyToByteArray(cpr.getInputStream())))
                .build();

        when(vetsServiceClient.getBadgeByVetId(anyString()))
                .thenReturn(Mono.just(badgeResponseDTO));

        client.get()
                .uri("/api/gateway/vets/{vetId}/badge", VET_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(BadgeResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertEquals(badgeResponseDTO.getBadgeTitle(), responseDTO.getBadgeTitle());
                    Assertions.assertEquals(badgeResponseDTO.getBadgeDate(), responseDTO.getBadgeDate());
                    Assertions.assertEquals(badgeResponseDTO.getVetId(), responseDTO.getVetId());
                    Assertions.assertEquals(badgeResponseDTO.getResourceBase64(), responseDTO.getResourceBase64());
                });

        Mockito.verify(vetsServiceClient, times(1))
                .getBadgeByVetId(VET_ID);
    }

    @Test
    void toStringBuilderVets() {
        System.out.println(VetResponseDTO.builder());
        System.out.println(VetRequestDTO.builder());
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
    void createUserInventoryManager_ShouldSucceed(){
        String uuid = UUID.randomUUID().toString();
        Role role = Role.builder()
                .name(Roles.INVENTORY_MANAGER.name())
                .build();
        UserPasswordLessDTO userResponse = UserPasswordLessDTO
                .builder()
                .userId(uuid)
                .email("email@email.com")
                .roles(Set.of(role))
                .build();

        when(authServiceClient.createInventoryMangerUser(any()))
                .thenReturn(Mono.just(userResponse));

        RegisterInventoryManager register = RegisterInventoryManager.builder()
                .userId(uuid)
                .username("Johnny123")
                .password("Password22##")
                .email("email@email.com")
                .build();

        client.post()
                .uri("/api/gateway/users/inventoryManager")
                .body(Mono.just(register), RegisterInventoryManager.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(UserPasswordLessDTO.class)
                .value(dto->{
                    assertEquals(dto.getUserId(),userResponse.getUserId());
                    assertEquals(dto.getEmail(),userResponse.getEmail());
                    assertEquals(dto.getRoles(),userResponse.getRoles());
                });
    }

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
    //private static final int BILL_ID = 1;

    @Test
    void payBill_Success() {
        BillResponseDTO successResponse = new BillResponseDTO();
        successResponse.setBillId("1");
        successResponse.setBillStatus(PAID);

        when(billServiceClient.payBill(anyString(), anyString(), any(PaymentRequestDTO.class), anyString()))
                .thenReturn(Mono.just(successResponse));

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        client.post()
                .uri("/api/gateway/bills/customer/1/bills/1/pay")
                .cookie("Bearer", "dummy-jwt-token")
                .body(BodyInserters.fromValue(paymentRequestDTO))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BillResponseDTO.class)
                .value(response -> {
                    assertEquals("1", response.getBillId());
                    assertEquals(PAID, response.getBillStatus());
                });
    }

    @Test
    void payBill_Failure() {
        when(billServiceClient.payBill(anyString(), anyString(), any(PaymentRequestDTO.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Invalid payment details")));

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        client.post()
                .uri("/api/gateway/bills/customer/1/bills/1/pay")
                .cookie("Bearer", "dummy-jwt-token")
                .body(BodyInserters.fromValue(paymentRequestDTO))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().isEmpty();
    }

    @Test
    void payBill_Failure_InvalidCustomerId() {
        when(billServiceClient.payBill(anyString(), anyString(), any(PaymentRequestDTO.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Invalid customer ID")));

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        client.post()
                .uri("/api/gateway/bills/customer/invalid-customer-id/bills/1/pay")
                .cookie("Bearer", "dummy-jwt-token")
                .body(BodyInserters.fromValue(paymentRequestDTO))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().isEmpty();
    }

    @Test
    void payBill_Failure_InvalidBillId() {
        when(billServiceClient.payBill(anyString(), anyString(), any(PaymentRequestDTO.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Invalid bill ID")));

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        client.post()
                .uri("/api/gateway/bills/customer/1/bills/invalid-bill-id/pay")
                .cookie("Bearer", "dummy-jwt-token")
                .body(BodyInserters.fromValue(paymentRequestDTO))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().isEmpty();
    }

    @Test
    void payBill_Failure_ExpiredCard() {
        when(billServiceClient.payBill(anyString(), anyString(), any(PaymentRequestDTO.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Card expired")));

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "01/20");

        client.post()
                .uri("/api/gateway/bills/customer/1/bills/1/pay")
                .cookie("Bearer", "dummy-jwt-token")
                .body(BodyInserters.fromValue(paymentRequestDTO))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().isEmpty();
    }



    @Test
    void payBill_MissingPaymentDetails_Failure() {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(null, null, null);

        client.post()
                .uri("/api/gateway/bills/customer/1/bills/1/pay")
                .cookie("Bearer", "dummy-jwt-token")
                .body(BodyInserters.fromValue(paymentRequestDTO))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> assertTrue(response.contains("Card number is required")));
    }

    @Test
    void payBill_InvalidCVV_Failure() {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "12", "12/23");

        client.post()
                .uri("/api/gateway/bills/customer/1/bills/1/pay")
                .cookie("Bearer", "dummy-jwt-token")
                .body(BodyInserters.fromValue(paymentRequestDTO))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> {
                    assertTrue(response.contains("CVV must be 3 digits"));
                });
    }

    @Test
    void getAllBillsByOwnerName() {
        // Arrange
        String ownerFirstName = "John";
        String ownerLastName = "Doe";

        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId("1");
        bill.setOwnerFirstName(ownerFirstName);
        bill.setOwnerLastName(ownerLastName);

        when(billServiceClient.getBillsByOwnerName(ownerFirstName, ownerLastName))
                .thenReturn(Flux.just(bill));

        // Act & Assert
        client.get()
                .uri("/api/gateway/bills/owner/" + ownerFirstName + "/" + ownerLastName)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    assertEquals(1, response.getResponseBody().size());
                });
    }

    @Test
    public void getBillsByOwnerId(){
        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId(UUID.randomUUID().toString());
        bill.setCustomerId("1");
        bill.setAmount(new BigDecimal("499"));
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


    /**
     * Visits Methods
     * **/

    String VISIT_ID = buildVisitResponseDTO().getVisitId();


//todo fix
    /*@Test
    void shouldCreateAVisitWithOwnerInfo(){
        String ownerId = "1";
        String cookie = "aCookie";
        OwnerResponseDTO owner = new OwnerResponseDTO();
        VisitRequestDTO visit = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2021-12-12T14:00"))
                .description("Charle's Richard cat has a paw infection.")
                .petId("1")
                .practitionerId("1")
                .status(Status.UPCOMING)
                .build();

        VisitResponseDTO visitResponseDTO =  VisitResponseDTO.builder()
                .visitId(VISIT_ID)
                .visitDate(LocalDateTime.parse("2021-12-12T14:00:00"))
                .petId("1")
                .description("Charle's Richard cat has a paw infection.")
                .practitionerId("1")
                .status(Status.UPCOMING)
                .build();


        when(visitsServiceClient.createVisitForPet(visit))
                .thenReturn(Mono.just(visitResponseDTO));


        client.post()
                .uri("/api/gateway/visit/owners/" + ownerId + "/pets/" + visitResponseDTO.getPetId() + "/visits", owner.getOwnerId(), visit.getPetId())
                .cookie("Bearer",cookie)
                .body(Mono.just(visit), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo("1")
                .jsonPath("$.visitDate").isEqualTo("2021-12-12 14:00")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo("UPCOMING")
                .jsonPath("$.practitionerId").isEqualTo(1);
    }*/

    //@Test
    public void addVisit_ShouldReturnCreatedStatus() {
        String ownerId = "owner1";
        String petId = "pet1";
        VisitRequestDTO visit = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2021-12-12T14:00"))
                .description("Charle's Richard cat has a paw infection.")
                .petId("1")
                .practitionerId("1")
                .status(Status.UPCOMING)
                .build();

        VisitResponseDTO visitResponseDTO =  VisitResponseDTO.builder()
                .visitId(VISIT_ID)
                .visitDate(LocalDateTime.parse("2021-12-12T14:00:00"))
                .petId("1")
                .description("Charle's Richard cat has a paw infection.")
                .practitionerId("1")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2021-12-12T15:00:00"))
                .build();

        when(visitsServiceClient.createVisitForPet(any(VisitRequestDTO.class)))
                .thenReturn(Mono.just(visitResponseDTO));

        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", ownerId, petId)
                .cookie("Bearer", "your-auth-token") // Assuming "Bearer" is the name of the cookie
                .body(Mono.just(visit), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // Validate the response
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo("1")
                .jsonPath("$.visitDate").isEqualTo("2021-12-12 14:00")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo("UPCOMING")
                .jsonPath("$.practitionerId").isEqualTo(1)
                .jsonPath("$.visitEndDate").isEqualTo("2021-12-12 15:00");
    }

    //@Test
    void shouldCreateAVisitWithOwnerAndPetInfo(){
        String ownerId = "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd";
        String petId = "9";
        VisitRequestDTO visit = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2021-12-12T14:00:00"))
                .description("Charle's Richard cat has a paw infection.")
                .petId(petId)
                .practitionerId("1")
                .status(Status.UPCOMING)
                .build();

        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId(VISIT_ID)
                .visitDate(LocalDateTime.parse("2021-12-12T14:00:00"))
                .petId(petId)
                .description("Charle's Richard cat has a paw infection.")
                .practitionerId("1")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2021-12-12T15:00:00"))
                .build();

        when(visitsServiceClient.createVisitForPet(visit))
                .thenReturn(Mono.just(visitResponseDTO));

        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", ownerId, petId)
                .body(Mono.just(visit), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo(petId)
                .jsonPath("$.visitDate").isEqualTo("2021-12-12 14:00")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo("UPCOMING")
                .jsonPath("$.practitionerId").isEqualTo("1")
                .jsonPath("$.visitEndDate").isEqualTo("2021-12-12 15:00");
    }

   /* @Test
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


        when(visitsServiceClient.createVisitForPet(visit))
                .thenReturn(Mono.just(visit));

        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", owner.getId(), visit.getPetId())
                .body(Mono.just(visit), VisitDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$.petId").isEqualTo(1)
                .jsonPath("$.date").isEqualTo("2021-12-12")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);

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

    //@Test
    void ShouldUpdateStatusForVisitByVisitId(){
        String status = "CANCELLED";
        VisitResponseDTO visit = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00"))
                .description("this is a dummy description")
                .practitionerId("2")
                .petId("2")
                .status(Status.CANCELLED)
                .visitEndDate(LocalDateTime.parse("2022-11-25T14:45:00"))
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
                .jsonPath("$.status").isEqualTo("CANCELLED")
                .jsonPath("$.visitEndDate").isEqualTo("2022-11-25 14:45");

        Mockito.verify(visitsServiceClient, times(1))
                .updateStatusForVisitByVisitId(anyString(), anyString());
    }
    //@Test
    void shouldGetAllVisits() {
        // Sample VisitResponseDTO objects
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

        VisitResponseDTO visitResponseDTO2 = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

        // Mocking the service call
        String description = "this is a dummy description";
        when(visitsServiceClient.getAllVisits(description)).thenReturn(Flux.just(visitResponseDTO, visitResponseDTO2));

        // Performing the request and asserting results
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/gateway/visits")
                        .queryParam("description", description)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(VisitResponseDTO.class)
                .value(list -> assertEquals(2, list.size()));  // Assert list size is 2

        // Verifying the method call with the right argument
        Mockito.verify(visitsServiceClient, times(1)).getAllVisits(description);
    }

    //@Test
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
    //@Test
    void shouldGetAVisit() {
        VisitResponseDTO visit = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

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
                    assertEquals(list.get(0).getVisitEndDate(),visit.getVisitEndDate());
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

    //@Test
    void getSingleVisit_Valid() {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
        when(visitsServiceClient.getVisitByVisitId(anyString())).thenReturn(Mono.just(visitResponseDTO));

        client.get()
                .uri("/api/gateway/visits/" + visitResponseDTO.getVisitId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo(visitResponseDTO.getPetId())
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 13:45")
                .jsonPath("$.description").isEqualTo(visitResponseDTO.getDescription())
                .jsonPath("$.practitionerId").isEqualTo(visitResponseDTO.getPractitionerId())
                .jsonPath("$.status").isEqualTo(visitResponseDTO.getStatus().toString())
                .jsonPath("$.visitEndDate").isEqualTo("2024-11-25 14:45");
    }
    //@Test
    void getVisitsByStatus_Valid() {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

        when(visitsServiceClient.getVisitsForStatus(visitResponseDTO.getStatus().toString())).thenReturn(Flux.just(visitResponseDTO));

        client.get()
                .uri("/api/gateway/visits/status/{status}", visitResponseDTO.getStatus())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .consumeWith(response -> {
                    Assertions.assertTrue(response.getResponseBody().size() > 0);
                    VisitResponseDTO responseBody = response.getResponseBody().get(0);
                    // Asserting that the values match what's expected
                    Assertions.assertEquals(visitResponseDTO.getVisitId(), responseBody.getVisitId());
                    Assertions.assertEquals(visitResponseDTO.getPetId(), responseBody.getPetId());
                    Assertions.assertEquals(visitResponseDTO.getVisitDate(), responseBody.getVisitDate());
                    Assertions.assertEquals(visitResponseDTO.getDescription(), responseBody.getDescription());
                    Assertions.assertEquals(visitResponseDTO.getPractitionerId(), responseBody.getPractitionerId());
                    Assertions.assertEquals(visitResponseDTO.getStatus(), responseBody.getStatus());
                    Assertions.assertEquals(visitResponseDTO.getVisitEndDate(), responseBody.getVisitEndDate());
                });
    }

    //@Test
    void getVisitsByPractitionerId_Valid() {
        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .petName("YourPetNameHere")
                .petBirthDate(new Date())
                .practitionerId("2")
                .vetFirstName("VetFirstNameHere")
                .vetLastName("VetLastNameHere")
                .vetEmail("vet@email.com")
                .vetPhoneNumber("123-456-7890")
                .status(Status.UPCOMING)
                .visitEndDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();

        when(visitsServiceClient.getVisitByPractitionerId(visitResponseDTO.getPractitionerId())).thenReturn(Flux.just(visitResponseDTO));

        client.get()
                .uri("/api/gateway/visits/vets/{practitionerId}", visitResponseDTO.getPractitionerId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VisitResponseDTO.class)
                .consumeWith(response -> {
                    Assertions.assertTrue(response.getResponseBody().size() > 0);
                    VisitResponseDTO responseBody = response.getResponseBody().get(0);

                    Assertions.assertEquals(visitResponseDTO.getVisitId(), responseBody.getVisitId());
                    Assertions.assertEquals(visitResponseDTO.getPetId(), responseBody.getPetId());
                    Assertions.assertEquals(visitResponseDTO.getVisitDate(), responseBody.getVisitDate());
                    Assertions.assertEquals(visitResponseDTO.getDescription(), responseBody.getDescription());
                    Assertions.assertEquals(visitResponseDTO.getPractitionerId(), responseBody.getPractitionerId());
                    Assertions.assertEquals(visitResponseDTO.getStatus(), responseBody.getStatus());
                    Assertions.assertEquals(visitResponseDTO.getVisitEndDate(), responseBody.getVisitEndDate());
                });
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




    //@Test
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

    //@Test
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

    //@Test
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

    //@Test
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


    @Test
    @DisplayName("Should Logout with a Valid Session, Clearing Bearer Cookie, and Returning 204")
    void logout_shouldClearBearerCookie() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.COOKIE, "Bearer=some.token.value; Path=/; HttpOnly; SameSite=Lax");
        when(authServiceClient.logout(any(ServerHttpRequest.class), any(ServerHttpResponse.class)))
                .thenReturn(Mono.just(ResponseEntity.noContent().build()));
        client.post()
                .uri("/api/gateway/users/logout")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .exchange()
                .expectStatus().isNoContent()
                .expectHeader().doesNotExist(HttpHeaders.SET_COOKIE);
    }

    @Test
    @DisplayName("Given Expired Session, Logout Should Return 401")
    void logout_shouldReturnUnauthorizedForExpiredSession() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        when(authServiceClient.logout(any(ServerHttpRequest.class), any(ServerHttpResponse.class)))
                .thenReturn(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
        client.post()
                .uri("/api/gateway/users/logout")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().doesNotExist(HttpHeaders.SET_COOKIE);
    }




//    @Test
//    void deleteAllProductInventory_shouldSucceed() {
//        // Assuming you want to test for a specific inventoryId
//        String inventoryId = "someInventoryId";
//
//        // Mock the service call to simulate the successful deletion of all product inventories for a specific inventoryId.
//        // Adjust the method name if `deleteAllProductInventoriesForInventory` is not the correct name.
//        when(inventoryServiceClient.deleteAllProductForInventory(eq(inventoryId)))
//                .thenReturn(Mono.empty());  // Using Mono.empty() to simulate a void return (successful deletion without a return value).
//
//        // Make the DELETE request to the API for a specific inventoryId.
//        client.delete()
//                .uri("/api/gateway/inventory/{inventoryId}/products", inventoryId)
//                .exchange()
//                .expectStatus().isNoContent()
//                .expectBody().isEmpty();
//
//        // Verify that the deleteAllProductInventoriesForInventory method on the service client was called exactly once with the specific inventoryId.
//        verify(inventoryServiceClient, times(1))
//                .deleteAllProductForInventory(eq(inventoryId));
//    }
    //inventory tests













    private ProductResponseDTO buildProductDTO(){
        return ProductResponseDTO.builder()
                .inventoryId("1")
                .productId(UUID.randomUUID().toString())
                .productName("Benzodiazepines")
                .productDescription("Sedative Medication")
                .productPrice(100.00)
                .productQuantity(10)
                .productSalePrice(15.99)
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
                .vetId("181faeb5-c024-425c-9f08-663600008f06")
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
                .date("2023")
                .build();
    }
    private RatingResponseDTO buildRatingResponseDTO2() {
        return RatingResponseDTO.builder()
                .ratingId("123456")
                .vetId("181faeb5-c024-425c-9f08-663600008f06")
                .rateScore(4.0)
                .date("2022")
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

    @Test
    public void getAllUsers_NoUsername_ShouldReturnAllUsers() {
        UserDetails user1 = UserDetails.builder()
                .userId("userId1")
                .username("username1")
                .email("email1")
                .build();

        UserDetails user2 = UserDetails.builder()
                .userId("userId2")
                .username("username2")
                .email("email2")
                .build();

        when(authServiceClient.getUsers(anyString()))
                .thenReturn(Flux.just(user1, user2));

        client.get()
                .uri("/api/gateway/users")
                .cookie("Bearer", "validToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDetails.class)
                .hasSize(2);
    }

    @Test
    public void getAllUsers_WithUsername_ShouldReturnUsersWithSpecificUsername() {
        UserDetails user = UserDetails.builder()
                .userId("userId")
                .username("specificUsername")
                .email("email")
                .build();

        UserDetails user2 = UserDetails.builder()
                .userId("userId2")
                .username("specificUsername2")
                .email("email2")
                .build();

        when(authServiceClient.getUsersByUsername(anyString(), anyString()))
                .thenReturn(Flux.just(user));

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/gateway/users")
                        .queryParam("username", "specificUsername")
                        .build())
                .cookie("Bearer", "validToken")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDetails.class)
                .hasSize(1);
    }


    @Test
    void deleteUserById_ValidUserId_ShouldDeleteUser() {
        UserDetails userDetails = UserDetails.builder()
                .userId("validUserId")
                .username("validUsername")
                .email("validEmail")
                .build();

        when(authServiceClient.deleteUser(anyString(), anyString()))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/users/validUserId")
                .cookie("Bearer", "validToken")
                .exchange()
                .expectStatus().isNoContent();
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
    @Test
    void addPhotoByVetId_LambdaBytes_201Created() {
        String vetId = "vet123";
        String photoName = "photo.jpg";
        byte[] photoData = "test photo data".getBytes();
        Resource expectedResource = new ByteArrayResource(photoData);

        when(vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData))
                .thenReturn(Mono.just(expectedResource));

        client.post()
                .uri("/api/gateway/vets/{vetId}/photos", vetId)
                .header("Photo-Name", photoName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Mono.just(photoData), byte[].class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(byte[].class)
                .isEqualTo(photoData);

        verify(vetsServiceClient).addPhotoToVetFromBytes(vetId, photoName, photoData);
    }

    @Test
    void addPhotoByVetId_LambdaBytes_400BadRequest_EmptyResponse() {
        String vetId = "vet123";
        String photoName = "photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        when(vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, photoData))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/gateway/vets/{vetId}/photos", vetId)
                .header("Photo-Name", photoName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Mono.just(photoData), byte[].class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().isEmpty();

        verify(vetsServiceClient).addPhotoToVetFromBytes(vetId, photoName, photoData);
    }

    @Test
    void addSpecialtiesByVetId_VetEndpoint_200Ok() {
        String vetId = "vet123";
        SpecialtyDTO specialtyDTO = SpecialtyDTO.builder()
                .specialtyId("specialty-1")
                .name("Surgery")
                .build();

        VetResponseDTO expectedVet = buildVetResponseDTO();

        when(vetsServiceClient.addSpecialtiesByVetId(eq(vetId), any(Mono.class)))
                .thenReturn(Mono.just(expectedVet));

        client.post()
                .uri("/api/gateway/vets/{vetId}/specialties", vetId) // note: include "vets" in path
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(specialtyDTO))
                .exchange()
                .expectStatus().isOk();

        verify(vetsServiceClient).addSpecialtiesByVetId(eq(vetId), any(Mono.class));
    }
}
