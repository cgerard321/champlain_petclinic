package com.petclinic.bffapigateway.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.*;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.*;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryRequestDTO;
import com.petclinic.bffapigateway.dtos.Inventory.InventoryResponseDTO;
import com.petclinic.bffapigateway.dtos.Inventory.ProductResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.dtos.Visits.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits.VisitRequestDTO;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static com.petclinic.bffapigateway.dtos.Inventory.InventoryType.internal;

import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WebTestClient client;
    @MockBean private CustomersServiceClient customersServiceClient;
    @MockBean private VisitsServiceClient visitsServiceClient;
    @MockBean private VetsServiceClient vetsServiceClient;
    @MockBean private AuthServiceClient authServiceClient;
    @MockBean private BillServiceClient billServiceClient;
    @MockBean private InventoryServiceClient inventoryServiceClient;

    VetDTO vetDTO = buildVetDTO();
    VetDTO vetDTO2 = buildVetDTO2();
    String VET_ID = buildVetDTO().getVetId();
    String INVALID_VET_ID = "mjbedf";



    @Test
    void getAllRatingsForVet_ValidId() {
        RatingResponseDTO ratingResponseDTO = buildRatingResponseDTO();
        when(vetsServiceClient.getRatingsByVetId(anyString()))
                .thenReturn(Flux.just(ratingResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].ratingId").isEqualTo(ratingResponseDTO.getRatingId())
                .jsonPath("$[0].vetId").isEqualTo(ratingResponseDTO.getVetId())
                .jsonPath("$[0].rateScore").isEqualTo(ratingResponseDTO.getRateScore());
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
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
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
    void addRatingToAVet() {
        RatingRequestDTO ratingRequestDTO = RatingRequestDTO.builder()
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
                .rateDate("16/09/2023")
                .build();
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(3.5)
                .rateDescription("The vet was decent but lacked table manners.")
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
    void updateRatingForVet(){
        RatingRequestDTO updatedRating = RatingRequestDTO.builder()
                .rateScore(2.0)
                .vetId(VET_ID)
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("12356789")
                .vetId(VET_ID)
                .rateScore(2.0)
                .rateDescription("Vet cancelled last minute.")
                .rateDate("20/09/2023")
                .build();

        when(vetsServiceClient.updateRatingByVetIdAndByRatingId(anyString(), anyString(), any(Mono.class)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.put()
                .uri("/api/gateway/vets/"+VET_ID+"/ratings/"+ratingResponseDTO.getRatingId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRating)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RatingResponseDTO.class)
                .value(responseDTO -> {
                    Assertions.assertNotNull(responseDTO);
                    Assertions.assertNotNull(responseDTO.getRatingId());
                    assertThat(responseDTO.getRatingId()).isEqualTo(ratingResponseDTO.getRatingId());
                    assertThat(responseDTO.getVetId()).isEqualTo(updatedRating.getVetId());
                    assertThat(responseDTO.getRateScore()).isEqualTo(updatedRating.getRateScore());
                    assertThat(responseDTO.getRateDescription()).isEqualTo(updatedRating.getRateDescription());
                    assertThat(responseDTO.getRateDate()).isEqualTo(updatedRating.getRateDate());
                });
    }
    @Test
    void getAllEducationsByVetId_WithValidId_ShouldSucceed(){
        EducationResponseDTO educationResponseDTO = buildEducation();
        when(vetsServiceClient.getEducationsByVetId(anyString()))
                .thenReturn(Flux.just(educationResponseDTO));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID + "/educations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].educationId").isEqualTo(educationResponseDTO.getEducationId())
                .jsonPath("$[0].vetId").isEqualTo(educationResponseDTO.getVetId())
                .jsonPath("$[0].degree").isEqualTo(educationResponseDTO.getDegree())
                .jsonPath("$[0].fieldOfStudy").isEqualTo(educationResponseDTO.getFieldOfStudy())
                .jsonPath("$[0].schoolName").isEqualTo(educationResponseDTO.getSchoolName())
                .jsonPath("$[0].startDate").isEqualTo(educationResponseDTO.getStartDate())
                .jsonPath("$[0].endDate").isEqualTo(educationResponseDTO.getEndDate());

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
                .expectStatus().isOk();

        Mockito.verify(vetsServiceClient, times(1))
                .deleteEducation(VET_ID, educationResponseDTO.getEducationId());
    }

    @Test
    void getAllVets() {
        when(vetsServiceClient.getVets())
                .thenReturn(Flux.just(vetDTO));

        client
                .get()
                .uri("/api/gateway/vets")
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
                .jsonPath("$[0].image").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vetDTO.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetDTO.getWorkday());

        Mockito.verify(vetsServiceClient, times(1))
                .getVets();
    }

    @Test
    void getVetByVetId() {
        when(vetsServiceClient.getVetByVetId(anyString()))
                .thenReturn(Mono.just(vetDTO));

        client
                .get()
                .uri("/api/gateway/vets/" + VET_ID)
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
                .jsonPath("$.image").isNotEmpty()
                .jsonPath("$.active").isEqualTo(vetDTO.isActive())
                .jsonPath("$.workday").isEqualTo(vetDTO.getWorkday());

        Mockito.verify(vetsServiceClient, times(1))
                .getVetByVetId(VET_ID);
    }

    @Test
    void getActiveVets() {
        when(vetsServiceClient.getActiveVets())
                .thenReturn(Flux.just(vetDTO2));

        client
                .get()
                .uri("/api/gateway/vets/active")
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].vetId").isEqualTo(vetDTO2.getVetId())
                .jsonPath("$[0].resume").isEqualTo(vetDTO2.getResume())
                .jsonPath("$[0].lastName").isEqualTo(vetDTO2.getLastName())
                .jsonPath("$[0].firstName").isEqualTo(vetDTO2.getFirstName())
                .jsonPath("$[0].email").isEqualTo(vetDTO2.getEmail())
                .jsonPath("$[0].image").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vetDTO2.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetDTO2.getWorkday());

        Mockito.verify(vetsServiceClient, times(1))
                .getActiveVets();
    }

    @Test
    void getInactiveVets() {
        when(vetsServiceClient.getInactiveVets())
                .thenReturn(Flux.just(vetDTO));

        client
                .get()
                .uri("/api/gateway/vets/inactive")
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
                .jsonPath("$[0].image").isNotEmpty()
                .jsonPath("$[0].active").isEqualTo(vetDTO.isActive())
                .jsonPath("$[0].workday").isEqualTo(vetDTO.getWorkday());

        Mockito.verify(vetsServiceClient, times(1))
                .getInactiveVets();
    }

    @Test
    void createVet() {
        Mono<VetDTO> dto = Mono.just(vetDTO);
        when(vetsServiceClient.createVet(any(Mono.class)))
                .thenReturn(dto);

        client
                .post()
                .uri("/api/gateway/vets")
                .body(dto, VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(vetsServiceClient, times(1))
                .createVet(any(Mono.class));
    }

    @Test
    void updateVet() {
        when(vetsServiceClient.updateVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(vetDTO2));

        client
                .put()
                .uri("/api/gateway/vets/" + VET_ID)
                .body(Mono.just(vetDTO2), VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.vetId").isEqualTo(vetDTO2.getVetId())
                .jsonPath("$.resume").isEqualTo(vetDTO2.getResume())
                .jsonPath("$.lastName").isEqualTo(vetDTO2.getLastName())
                .jsonPath("$.firstName").isEqualTo(vetDTO2.getFirstName())
                .jsonPath("$.email").isEqualTo(vetDTO2.getEmail())
                .jsonPath("$.image").isNotEmpty()
                .jsonPath("$.active").isEqualTo(vetDTO2.isActive())
                .jsonPath("$.workday").isEqualTo(vetDTO2.getWorkday());

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
                .thenReturn(Mono.just(vetDTO));

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
                .thenReturn(Mono.just(vetDTO));

        client
                .put()
                .uri("/api/gateway/vets/" + INVALID_VET_ID)
                .body(Mono.just(vetDTO), VetDTO.class)
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
    void toStringBuilderVets() {
        System.out.println(VetDTO.builder());
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
                .telephone("51451545144")
                .build();


        OwnerResponseDTO owner_response = OwnerResponseDTO
                .builder()
                .ownerId(uuid)
                .firstName("John")
                .lastName("Johnny")
                .address("111 John St")
                .city("Johnston")
                .telephone("51451545144")
                .build();



        when(authServiceClient.createUser(any(Register.class)))
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
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OwnerResponseDTO.class)
                .value(dto->{
                    assertNotNull(dto.getOwnerId());
                    assertEquals(dto.getFirstName(),owner.getFirstName());
                    assertEquals(dto.getLastName(),owner.getLastName());
                    assertEquals(dto.getAddress(),owner.getAddress());
                    assertEquals(dto.getCity(),owner.getCity());
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

    @Test
    void getAllOwners_shouldSucceed(){
        OwnerResponseDTO owner1 = new OwnerResponseDTO();
        owner1.setOwnerId("ownerId-90");
        owner1.setFirstName("John");
        owner1.setLastName("Johnny");
        owner1.setAddress("111 John St");
        owner1.setCity("Johnston");
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








    @Test

    void shouldCreatePet(){

        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-12345");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("30-30-30-30");
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);
        pet.setIsActive("true");

        when(customersServiceClient.createPet(pet,od.getOwnerId()))

                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())

                .body(Mono.just(pet), PetResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)

                .expectBody()
                .jsonPath("$.petId").isEqualTo(pet.getPetId())
                .jsonPath("$.name").isEqualTo(pet.getName())
                .jsonPath("$.birthDate").isEqualTo(pet.getBirthDate())
                .jsonPath("$.type").isEqualTo(pet.getType())
                .jsonPath("$.isActive").isEqualTo(pet.getIsActive());

    }

    @Test
    void shouldUpdatePet() {
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-12345");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setPetId("30");
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);
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
                .jsonPath("$.birthDate").isEqualTo(pet.getBirthDate())
                .jsonPath("$.type").isEqualTo(pet.getType())
                .jsonPath("$.isActive").isEqualTo(pet.getIsActive());
    }

    @Test
    void shouldPatchPet() {
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setIsActive("false");

        PetResponseDTO petResponse = new PetResponseDTO();
        petResponse.setPetId("30");
        petResponse.setIsActive("false");

        when(customersServiceClient.patchPet(any(PetRequestDTO.class), any(String.class)))
                .thenReturn(Mono.just(petResponse));

        client.patch()
                .uri("/api/gateway/pet/{petId}", petResponse.getPetId())
                .body(fromValue(petRequest))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.petId").isEqualTo(petResponse.getPetId())
                .jsonPath("$.isActive").isEqualTo(petResponse.getIsActive());
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
        pet.setPetId("petId-123");
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);
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
        pet.setPetId("petId-123");
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);
        pet.setIsActive("true");

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
        pet.setPetId("petId-123");
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);
        pet.setIsActive("true");

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
                .expectStatus().isOk()
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
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody();

            assertEquals(billResponseDTO.getBillId(),"9");


        client.delete()
                .uri("/api/gateway/bills/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertNull(billServiceClient.getBilling(billResponseDTO.getBillId()));
    }

    @Test
    void shouldDeleteBillByVetId() {
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setVetId("9");
        billRequestDTO.setDate(null);
        billRequestDTO.setAmount(600);
        billRequestDTO.setVisitType("Adoption");

        BillResponseDTO billResponseDTO = new BillResponseDTO();
        billResponseDTO.setVetId("9");
        billResponseDTO.setDate(null);
        billResponseDTO.setAmount(600);
        billResponseDTO.setVisitType("Adoption");

        when(billServiceClient.createBill(billRequestDTO))
                .thenReturn(Mono.just(billResponseDTO));

        client.post()
                .uri("/api/gateway/bills")
                .body(Mono.just(billRequestDTO), BillRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        // Compare the vetId property, not the billId property
        assertEquals(billResponseDTO.getVetId(), "9");

        client.delete()
                .uri("/api/gateway/bills/vet/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertNull(billServiceClient.getBilling(billResponseDTO.getBillId()));
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
    }*/


//    @Test
//    void shouldDeleteAVisit() {
//        VisitDetails visit = new VisitDetails();
//        OwnerDetails owner = new OwnerDetails();
//        owner.setId(1);
//        visit.setVisitId(UUID.randomUUID().toString());
//        visit.setPetId(1);
//        visit.setDate("2021-12-12");
//        visit.setDescription("Charle's Richard cat has a paw infection.");
//        visit.setStatus(false);
//        visit.setPractitionerId(1);
//
//
//        when(visitsServiceClient.createVisitForPet(visit))
//                .thenReturn(Mono.just(visit));
//
//        client.post()
//                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", owner.getId(), visit.getPetId())
//                .body(Mono.just(visit), VisitDetails.class)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
//                .jsonPath("$.petId").isEqualTo(1)
//                .jsonPath("$.date").isEqualTo("2021-12-12")
//                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
//                .jsonPath("$.status").isEqualTo(false)
//                .jsonPath("$.practitionerId").isEqualTo(1);
//
//        client.delete()
//                .uri("/api/gateway/visits/{visitId}", visit.getVisitId())
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus()
//                .isOk()
//                .expectBody();
//
//        assertEquals(null, visitsServiceClient.getVisitsForPet(visit.getPetId()));
//    }

    @Test
    void shouldUpdateAVisitsById() {
        // Create instances of VisitRequestDTO and VisitResponseDTO for creating a visit
        VisitRequestDTO visitRequestDTO = VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Charle's Richard cat has a paw infection.")
                .petId("1")
                .practitionerId("1")
                .status(false)
                .build();

        VisitResponseDTO visitResponseDTO = VisitResponseDTO.builder()
                .visitId(UUID.randomUUID().toString())
                .visitDate(LocalDateTime.parse("2024-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Charle's Richard cat has a paw infection.")
                .petId("1")
                .practitionerId("1")
                .status(false)
                .build();

        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-90");

        // Mock the service call for creating a visit
        when(visitsServiceClient.createVisitForPet(visitRequestDTO))
                .thenReturn(Mono.just(visitResponseDTO));

        // Perform POST request to create a visit
        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", owner.getOwnerId(), visitRequestDTO.getPetId())
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo("1")
                .jsonPath("$.visitDate").isEqualTo("2024-11-25 14:45")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo("1");

        // Create an instance of VisitDetails for updating
        VisitDetails visitDetailsToUpdate = VisitDetails.builder()
                .visitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("Charle's Richard dog has a paw infection.")
                .petId("2")
                .practitionerId(2)
                .status(false)
                .build();

        // Mock the service call for updating a visit
        when(visitsServiceClient.updateVisitForPet(visitDetailsToUpdate))
                .thenReturn(Mono.just(visitDetailsToUpdate));

        // Perform PUT request to update a visit
        client.put()
                .uri("/api/gateway/owners/*/pets/{petId}/visits/{visitId}", visitRequestDTO.getPetId(), visitResponseDTO.getVisitId())
                .body(Mono.just(visitDetailsToUpdate), VisitDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        // Assert the updated visit
        assertEquals(visitsServiceClient.getVisitsForPet("1"), null);
    }
    @Test
    void shouldGetAllVisits() {
        VisitResponseDTO visitResponseDTO = new VisitResponseDTO("73b5c112-5703-4fb7-b7bc-ac8186811ae1", LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "this is a dummy description", "2", "2", true);
        VisitResponseDTO visitResponseDTO2 = new VisitResponseDTO("73b5c112-5703-4fb7-b7bc-ac8186811ae1", LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "this is a dummy description", "2", "2", true);
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
    void shouldGetAVisit() {
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId("1");
        visit.setVisitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        when(visitsServiceClient.getVisitsForPet(visit.getPetId()))
                .thenReturn(Flux.just(visit));

        client.get()
                .uri("/api/gateway/visits/pets/{petId}", visit.getPetId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$[0].petId").isEqualTo("1")
                .jsonPath("$[0].visitDate").isEqualTo("2022-11-25 13:45")
                .jsonPath("$[0].description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$[0].practitionerId").isEqualTo(1);
    }

    @Test
    void shouldGetAVisitForPractitioner(){
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId("1");
        visit.setVisitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        when(visitsServiceClient.getVisitForPractitioner(Integer.parseInt(visit.getPetId())))
                .thenReturn(Flux.just(visit));

        client.get()
                .uri("/api/gateway/visits/vets/{practitionerId}", visit.getPractitionerId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$[0].petId").isEqualTo("1")
                .jsonPath("$[0].visitDate").isEqualTo("2022-11-25 13:45")
                .jsonPath("$[0].description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$[0].practitionerId").isEqualTo(1);
    }
    /*
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
        VisitResponseDTO visitResponseDTO = new VisitResponseDTO("73b5c112-5703-4fb7-b7bc-ac8186811ae1", LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), "this is a dummy description", "2", "2", true);
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

    @Test
    @DisplayName("Should get the previous visits of a pet")
    void shouldGetPreviousVisitsOfAPet() {
        VisitDetails visit1 = new VisitDetails();
        VisitDetails visit2 = new VisitDetails();
        visit1.setVisitId(UUID.randomUUID().toString());
        visit1.setPetId("21");
        visit1.setVisitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit1.setDescription("John Smith's cat has a paw infection.");
        visit1.setStatus(false);
        visit1.setPractitionerId(2);
        visit2.setVisitId(UUID.randomUUID().toString());
        visit2.setPetId("21");
        visit2.setVisitDate(LocalDateTime.parse("2022-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit2.setDescription("John Smith's dog has a paw infection.");
        visit2.setStatus(false);
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
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$[0].petId").isEqualTo("21")
                .jsonPath("$[0].visitDate").isEqualTo("2022-11-25 13:45")
                .jsonPath("$[0].description").isEqualTo("John Smith's cat has a paw infection.")
                .jsonPath("$[0].status").isEqualTo(false)
                .jsonPath("$[0].practitionerId").isEqualTo(2)
                .jsonPath("$[1].visitId").isEqualTo(visit2.getVisitId())
                .jsonPath("$[1].petId").isEqualTo("21")
                .jsonPath("$[1].visitDate").isEqualTo("2022-11-25 14:45")
                .jsonPath("$[1].description").isEqualTo("John Smith's dog has a paw infection.")
                .jsonPath("$[1].status").isEqualTo(false)
                .jsonPath("$[1].practitionerId").isEqualTo(2);

    }

    @Test
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
        visit1.setStatus(true);
        visit1.setPractitionerId(2);
        visit2.setVisitId(UUID.randomUUID().toString());
        visit2.setPetId("21");
        visit2.setVisitDate(LocalDateTime.parse("2022-11-25 14:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit2.setDescription("John Smith's dog has a paw infection.");
        visit2.setStatus(true);
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
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$[0].petId").isEqualTo(21)
                .jsonPath("$[0].visitDate").isEqualTo("2022-11-25 13:45")
                .jsonPath("$[0].description").isEqualTo("John Smith's cat has a paw infection.")
                .jsonPath("$[0].status").isEqualTo(true)
                .jsonPath("$[0].practitionerId").isEqualTo(2)
                .jsonPath("$[1].visitId").isEqualTo(visit2.getVisitId())
                .jsonPath("$[1].petId").isEqualTo(21)
                .jsonPath("$[1].visitDate").isEqualTo("2022-11-25 14:45")
                .jsonPath("$[1].description").isEqualTo("John Smith's dog has a paw infection.")
                .jsonPath("$[1].status").isEqualTo(true)
                .jsonPath("$[1].practitionerId").isEqualTo(2);
    }

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

    @Test
    void deleteVisitById_visitId_shouldSucceed(){
        when(visitsServiceClient.deleteVisitByVisitId(VISIT_ID)).thenReturn(Mono.empty());
        client.delete()
                .uri("/api/gateway/visits/" + VISIT_ID)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound(); // Expecting a 404 status code

        Mockito.verify(visitsServiceClient, times(1))
                .deleteVisitByVisitId(invalidId);
    }

    private VisitResponseDTO buildVisitResponseDTO(){
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(UUID.randomUUID().toString())
                .status(true)
                .build();
    }
    /**
     * End of Visits Methods
     * **/





//    @Test
//    @DisplayName("Given valid JWT, verify user")
//    void verify_user() throws JsonProcessingException {
//
//        final String validToken = "some.valid.token";
//        final UserDetails user = UserDetails.builder()
//                .id(1)
//                .password(null)
//                .email("e@mail.com")
//                .username("user")
//                .roles(Collections.emptySet())
//                .build();
//
//        when(authServiceClient.verifyUser(validToken))
//                .thenReturn(Mono.just(user));
//
//        client.get()
//                .uri("/api/gateway/verification/{token}", validToken)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .json(objectMapper.writeValueAsString(user));
//    }

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

        when(authServiceClient.login(any(Login.class)))
                .thenReturn(httpResponse);

        when(authServiceClient.login(any(Login.class)))
                .thenReturn(httpResponse);


        final Login login = Login.builder()
                .password("valid")
                .email(user.getEmail())
                .build();
        when(authServiceClient.login(login))
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
        when(authServiceClient.login(login))
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
                .inventoryType(internal)
                .inventoryDescription("invtone")
                .build();
}
    @Test
    void addInventory_withValidValue_shouldSucceed() {

        InventoryRequestDTO requestDTO = new InventoryRequestDTO("internal", internal, "invt1");

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
                .jsonPath("$.inventoryType").isEqualTo("internal")
                .jsonPath("$.inventoryDescription").isEqualTo("invtone");


        verify(inventoryServiceClient, times(1))
                .addInventory(any());
    }



    @Test
    void updateInventory_withValidValue_shouldSucceed() {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO("internal", internal, "newDescription");

        InventoryResponseDTO expectedResponse = InventoryResponseDTO.builder()
                .inventoryId("1")
                .inventoryName("newName")
                .inventoryType(internal)
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
                .jsonPath("$.inventoryType").isEqualTo("internal")
                .jsonPath("$.inventoryDescription").isEqualTo(expectedResponse.getInventoryDescription());

        verify(inventoryServiceClient, times(1))
                .updateInventory(any(), eq(buildInventoryDTO().getInventoryId()));
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
            .expectStatus().isOk()
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
                .expectStatus().isOk()
                .expectBody().isEmpty();

        // Verify that the deleteAllProductInventoriesForInventory method on the service client was called exactly once with the specific inventoryId.
        verify(inventoryServiceClient, times(1))
                .deleteAllProductForInventory(eq(inventoryId));
    }
    //inventory tests

    @Test
    void testUpdateProductInInventory() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100);

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
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100);

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
                });

        // Verify that the inventoryServiceClient method was called
        verify(inventoryServiceClient, times(1))
                .addProductToInventory(eq(requestDTO), eq("sampleInventoryId"));
    }

    @Test
    @DisplayName("Given invalid inventoryId and valid productRequest Post and return NotFoundException")
    void testAddProductToInventory_InvalidInventoryId_ShouldReturnNotFoundException() {
        // Create a sample ProductRequestDTO
        ProductRequestDTO requestDTO = new ProductRequestDTO("Sample Product", "Sample Description", 10.0, 100);

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

    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday("Monday")
                .image("kjd".getBytes())
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
                .image("kjd".getBytes())
                .resume("Just became a vet")
                .workday("Monday")
                .specialties(new HashSet<>())
                .active(true)
                .build();
    }

    private RatingResponseDTO buildRatingResponseDTO() {
        return RatingResponseDTO.builder()
                .ratingId("123456")
                .vetId("678910")
                .rateScore(4.0)
                .build();
    }

    @Test
    void sendForgottenEmail_ShouldSucceed(){
        final UserEmailRequestDTO dto = UserEmailRequestDTO.builder()
                .email("email")
                .build();

        ServerHttpRequest request = MockServerHttpRequest.post("http://localhost:8080").build();

        when(authServiceClient.sendForgottenEmail(request,dto.getEmail()))
                .thenReturn(Mono.just(ResponseEntity.ok().build()));


        client.post()
                .uri("/api/gateway/users/forgot_password")
                .body(Mono.just(dto), UserEmailRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        verify(authServiceClient, times(1)).sendForgottenEmail(any(ServerHttpRequest.class),anyString());
    }


    @Test
    void sendForgottenEmail_ShouldFail(){
        final UserEmailRequestDTO dto = UserEmailRequestDTO.builder()
                .email("email")
                .build();

        ServerHttpRequest request = MockServerHttpRequest.post("http://localhost:8080").build();

        when(authServiceClient.sendForgottenEmail(any(),any()))
                .thenThrow(new GenericHttpException("error",BAD_REQUEST));



        client.post()
                .uri("/api/gateway/users/forgot_password")
                .body(Mono.just(dto), UserEmailRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody();

        verify(authServiceClient, times(1)).sendForgottenEmail(any(ServerHttpRequest.class),anyString());
        }


        @Test
        void processResetPassword_ShouldSucceed(){
            final UserPasswordAndTokenRequestModel dto = UserPasswordAndTokenRequestModel.builder()
                    .password("password")
                    .token("Valid token")
                    .build();


            when(authServiceClient.changePassword(dto))
                    .thenReturn(Mono.just(ResponseEntity.ok().build()));


            client.post()
                    .uri("/api/gateway/users/reset_password")
                    .body(Mono.just(dto), UserPasswordAndTokenRequestModel.class)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody();

            verify(authServiceClient, times(1)).changePassword(any(UserPasswordAndTokenRequestModel.class));
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
                .expectStatus().isOk()
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







