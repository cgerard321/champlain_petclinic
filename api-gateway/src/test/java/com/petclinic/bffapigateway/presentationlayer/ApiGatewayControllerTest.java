package com.petclinic.bffapigateway.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.*;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Bills.BillDetails;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.dtos.Visits.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

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
    void getAllRatingsForVet() {
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
                .expectStatus().isOk();

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
                .expectStatus().isEqualTo(NOT_FOUND)
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
        when(vetsServiceClient.addRatingToVet(VET_ID, Mono.just(ratingRequestDTO)))
                .thenReturn(Mono.just(ratingResponseDTO));

        client.post()
                .uri("/api/gateway/vets/{vetId}/ratings", ratingRequestDTO.getVetId())
                .bodyValue(ratingRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
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
        when(vetsServiceClient.createVet(dto))
                .thenReturn(dto);

        client
                .post()
                .uri("/api/gateway/vets")
                .body(dto, VetDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
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
                .expectStatus().isOk();

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



    @Test
    void createOwner(){
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-123");
        owner.setFirstName("John");
        owner.setLastName("Johnny");
        owner.setAddress("111 John St");
        owner.setCity("Johnston");
        owner.setTelephone("51451545144");
        when(customersServiceClient.createOwner(owner))
                .thenReturn(Mono.just(owner));


        client.post()
                .uri("/api/gateway/owners")
                .body(Mono.just(owner), OwnerResponseDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();



        assertEquals(owner.getOwnerId(),owner.getOwnerId());
        assertEquals(owner.getFirstName(),"John");
        assertEquals(owner.getLastName(),"Johnny");
        assertEquals(owner.getAddress(),"111 John St");
        assertEquals(owner.getCity(),"Johnston");
        assertEquals(owner.getTelephone(),"51451545144");
    }

    @Test

    void shouldCreatePet(){

        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-12345");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
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

                .expectBody()
                .jsonPath("$.id").isEqualTo(pet.getId())
                .jsonPath("$.name").isEqualTo(pet.getName())
                .jsonPath("$.birthDate").isEqualTo(pet.getBirthDate())
                .jsonPath("$.type").isEqualTo(pet.getType());

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
    @Test
    void createPetPhoto(){

        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-123");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Cat");
        pet.setId(1);
        pet.setName("Bonkers");
        pet.setBirthDate("2015-03-03");
        pet.setType(type);
        pet.setImageId(2);

        final String test = "Test photo";
        final byte[] testBytes = test.getBytes();

        PhotoDetails photo = new PhotoDetails();
        photo.setId(2);
        photo.setName("photo");
        photo.setType("jpeg");
        photo.setPhoto("testBytes");

        when(customersServiceClient.setPetPhoto(owner.getOwnerId(), photo, pet.getId()))
                .thenReturn(Mono.just("Image uploaded successfully: " + photo.getName()));

        client.post()
                .uri("/api/gateway/owners/1/pet/photo/1")
                .body(Mono.just(photo), PhotoDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }
    //
    @Test
    void getPetPhoto(){

        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-1234");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Cat");
        pet.setId(1);
        pet.setName("Bonkers");
        pet.setBirthDate("2015-03-03");
        pet.setType(type);
        pet.setImageId(2);

        final String test = "Test photo";
        final byte[] testBytes = test.getBytes();

        PhotoDetails photo = new PhotoDetails();
        photo.setId(2);
        photo.setName("photo");
        photo.setType("jpeg");
        photo.setPhoto("testBytes");

        when(customersServiceClient.getPetPhoto(owner.getOwnerId(), pet.getId()))
                .thenReturn(Mono.just(photo));

        client.get()
                .uri("/api/gateway/owners/"+ owner.getOwnerId() +"/pet/photo/" + pet.getId() )
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo("photo")
                .jsonPath("$.type").isEqualTo("jpeg");
    }
    //
//
    @Test
    void shouldThrowUnsupportedMediaTypeIfBodyDoesNotExist(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-21");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getOwnerId()))
                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getOwnerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(UNSUPPORTED_MEDIA_TYPE)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Content type '' not supported");


    }
    //
    @Test
    void ifOwnerIdIsNotSpecifiedInUrlThrowNotAllowed(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

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
    @Test
    void shouldCreateThenDeletePet(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-65");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
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
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}",od.getOwnerId(), pet.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();


    }
    //
    @Test
    void shouldThrowNotFoundWhenOwnerIdIsNotSpecifiedOnDeletePets(){
        OwnerResponseDTO od = new OwnerResponseDTO();
        od.setOwnerId("ownerId-24");
        PetResponseDTO pet = new PetResponseDTO();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
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
                .uri("/api/gateway/owners/pets/{petId}", pet.getId())
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
        pet.setId(30);
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

        BillDetails entity = new BillDetails();

        entity.setBillId("9");

        entity.setAmount(599);

        entity.setCustomerId(2);

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
        BillDetails bill = new BillDetails();
        bill.setBillId(UUID.randomUUID().toString());
        bill.setCustomerId(1);
        bill.setAmount(499);
        bill.setVisitType("Test");

        when(billServiceClient.getBillsByOwnerId(bill.getCustomerId()))
                .thenReturn(Flux.just(bill));

        client.get()
                .uri("/api/gateway/bills/customer/{customerId}", bill.getCustomerId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].billId").isEqualTo(bill.getBillId())
                .jsonPath("$[0].customerId").isEqualTo(1)
                .jsonPath("$[0].amount").isEqualTo(499)
                .jsonPath("$[0].visitType").isEqualTo("Test");
    }

    @Test
    public void getBillsByVetId(){
        BillDetails bill = new BillDetails();
        bill.setBillId(UUID.randomUUID().toString());
        bill.setVetId("1");
        bill.setAmount(499);
        bill.setVisitType("Test");

        when(billServiceClient.getBillsByVetId(bill.getVetId()))
                .thenReturn(Flux.just(bill));

        client.get()
                .uri("/api/gateway/bills/vet/{vetId}", bill.getVetId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].billId").isEqualTo(bill.getBillId())
                .jsonPath("$[0].vetId").isEqualTo("1")
                .jsonPath("$[0].amount").isEqualTo(499)
                .jsonPath("$[0].visitType").isEqualTo("Test");
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
        BillDetails bill = new BillDetails();
        bill.setBillId("9");
        bill.setDate(null);
        bill.setAmount(600);
        bill.setVisitType("Adoption");
        when(billServiceClient.createBill(bill))
                .thenReturn(Mono.just(bill));

        client.post()
                .uri("/api/gateway/bills")
                .body(Mono.just(bill), BillDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(bill.getBillId(),"9");
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
        BillDetails bill = new BillDetails();
        bill.setBillId("9");

        bill.setDate(null);

        bill.setAmount(600);

        bill.setVisitType("Adoption");

        when(billServiceClient.createBill(bill))
                .thenReturn(Mono.just(bill));


        client.post()
                .uri("/api/gateway/bills")
                .body(Mono.just(bill), BillDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(bill.getBillId(),"9");
        client.delete()
                .uri("/api/gateway/bills/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, billServiceClient.getBilling(bill.getBillId()));
    }

    @Test
    void shouldDeleteBillByVetId(){
        BillDetails bill = new BillDetails();
        bill.setVetId("9");

        bill.setDate(null);

        bill.setAmount(600);

        bill.setVisitType("Adoption");

        when(billServiceClient.createBill(bill))
                .thenReturn(Mono.just(bill));


        client.post()
                .uri("/api/gateway/bills")
                .body(Mono.just(bill), BillDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(bill.getVetId(),"9");
        client.delete()
                .uri("/api/gateway/bills/vet/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, billServiceClient.getBilling(bill.getVetId()));
    }

    @Test
    void shouldCreateAVisitWithOwnerInfo(){
        OwnerResponseDTO owner = new OwnerResponseDTO();
        VisitDetails visit = new VisitDetails();
        owner.setOwnerId("ownerId-123");
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        when(visitsServiceClient.createVisitForPet(visit))
                .thenReturn(Mono.just(visit));


        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", owner.getOwnerId(), visit.getPetId())
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
    }
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
        VisitDetails visit = new VisitDetails();
        OwnerResponseDTO owner = new OwnerResponseDTO();
        owner.setOwnerId("ownerId-90");
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        VisitDetails visit2 = new VisitDetails();
        OwnerResponseDTO owner2 = new OwnerResponseDTO();

        owner2.setOwnerId("ownerId-12");
        visit2.setVisitId(UUID.randomUUID().toString());
        visit2.setPetId(2);
        visit2.setDate("2034-12-12");
        visit2.setDescription("Charle's Richard dog has a paw infection.");
        visit2.setStatus(false);
        visit2.setPractitionerId(2);


        when(visitsServiceClient.createVisitForPet(visit))
                .thenReturn(Mono.just(visit));

        client.post()
                .uri("/api/gateway/visit/owners/{ownerId}/pets/{petId}/visits", owner.getOwnerId(), visit.getPetId())
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

        when(visitsServiceClient.updateVisitForPet(visit2))
                .thenReturn(Mono.just(visit2));

        client.put()
                .uri("/api/gateway/owners/*/pets/{petId}/visits/{visitId}",visit.getPetId(), visit.getVisitId())
                .body(Mono.just(visit2), VisitDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(visitsServiceClient.getVisitsForPet(1), null);


    }

    @Test
    void shouldGetAVisit() {
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(1);
        visit.setDate("2021-12-12");
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
                .jsonPath("$[0].petId").isEqualTo(1)
                .jsonPath("$[0].date").isEqualTo("2021-12-12")
                .jsonPath("$[0].description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$[0].practitionerId").isEqualTo(1);
    }

    @Test
    void shouldGetAVisitForPractitioner(){
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        when(visitsServiceClient.getVisitForPractitioner(visit.getPetId()))
                .thenReturn(Flux.just(visit));

        client.get()
                .uri("/api/gateway/visits/vets/{practitionerId}", visit.getPractitionerId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$[0].petId").isEqualTo(1)
                .jsonPath("$[0].date").isEqualTo("2021-12-12")
                .jsonPath("$[0].description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$[0].practitionerId").isEqualTo(1);
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

    @Test
    void getSingleVisit_Valid() {
        VisitResponseDTO visitResponseDTO = new VisitResponseDTO("773fa7b2-e04e-47b8-98e7-4adf7cfaaeee", 2023, 11, 20, "test visit", 1, 1, false);

        when(visitsServiceClient.getVisitByVisitId(anyString())).thenReturn(Mono.just(visitResponseDTO));

        client.get()
                .uri("/api/gateway/visits/{visitId}", visitResponseDTO.getVisitId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.petId").isEqualTo(visitResponseDTO.getPetId())
//                .jsonPath("$.date").isEqualTo(visitResponseDTO.getDate())
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
        visit1.setPetId(21);
        visit1.setDate("2021-12-7");
        visit1.setDescription("John Smith's cat has a paw infection.");
        visit1.setStatus(false);
        visit1.setPractitionerId(2);
        visit2.setVisitId(UUID.randomUUID().toString());
        visit2.setPetId(21);
        visit2.setDate("2021-12-8");
        visit2.setDescription("John Smith's dog has a paw infection.");
        visit2.setStatus(false);
        visit2.setPractitionerId(2);

        List<VisitDetails> previousVisitsList = new ArrayList<>();
        previousVisitsList.add(visit1);
        previousVisitsList.add(visit2);

        Flux<VisitDetails> previousVisits = Flux.fromIterable(previousVisitsList);

        when(visitsServiceClient.getPreviousVisitsForPet(21))
                .thenReturn(previousVisits);

        client.get()
                .uri("/api/gateway/visits/previous/{petId}", 21)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$[0].petId").isEqualTo(21)
                .jsonPath("$[0].date").isEqualTo("2021-12-7")
                .jsonPath("$[0].description").isEqualTo("John Smith's cat has a paw infection.")
                .jsonPath("$[0].status").isEqualTo(false)
                .jsonPath("$[0].practitionerId").isEqualTo(2)
                .jsonPath("$[1].visitId").isEqualTo(visit2.getVisitId())
                .jsonPath("$[1].petId").isEqualTo(21)
                .jsonPath("$[1].date").isEqualTo("2021-12-8")
                .jsonPath("$[1].description").isEqualTo("John Smith's dog has a paw infection.")
                .jsonPath("$[1].status").isEqualTo(false)
                .jsonPath("$[1].practitionerId").isEqualTo(2);

    }

    @Test
    @DisplayName("Should return a bad request if the petId is invalid when trying to get the previous visits of a pet")
    void shouldGetBadRequestWhenInvalidPetIdToRetrievePreviousVisits() {
        final int invalidPetId = -1;
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
        visit1.setPetId(21);
        visit1.setDate("2021-12-7");
        visit1.setDescription("John Smith's cat has a paw infection.");
        visit1.setStatus(true);
        visit1.setPractitionerId(2);
        visit2.setVisitId(UUID.randomUUID().toString());
        visit2.setPetId(21);
        visit2.setDate("2021-12-8");
        visit2.setDescription("John Smith's dog has a paw infection.");
        visit2.setStatus(true);
        visit2.setPractitionerId(2);

        List<VisitDetails> scheduledVisitsList = new ArrayList<>();
        scheduledVisitsList.add(visit1);
        scheduledVisitsList.add(visit2);

        Flux<VisitDetails> scheduledVisits = Flux.fromIterable(scheduledVisitsList);

        when(visitsServiceClient.getScheduledVisitsForPet(21))
                .thenReturn(scheduledVisits);

        client.get()
                .uri("/api/gateway/visits/scheduled/{petId}", 21)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].visitId").isEqualTo(visit1.getVisitId())
                .jsonPath("$[0].petId").isEqualTo(21)
                .jsonPath("$[0].date").isEqualTo("2021-12-7")
                .jsonPath("$[0].description").isEqualTo("John Smith's cat has a paw infection.")
                .jsonPath("$[0].status").isEqualTo(true)
                .jsonPath("$[0].practitionerId").isEqualTo(2)
                .jsonPath("$[1].visitId").isEqualTo(visit2.getVisitId())
                .jsonPath("$[1].petId").isEqualTo(21)
                .jsonPath("$[1].date").isEqualTo("2021-12-8")
                .jsonPath("$[1].description").isEqualTo("John Smith's dog has a paw infection.")
                .jsonPath("$[1].status").isEqualTo(true)
                .jsonPath("$[1].practitionerId").isEqualTo(2);
    }

    @Test
    @DisplayName("Should return a bad request if the petId is invalid when trying to get the scheduled visits of a pet")
    void shouldGetBadRequestWhenInvalidPetIdToRetrieveScheduledVisits() {
        final int invalidPetId = -1;
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
//
//    @Test
//    @DisplayName("Given valid Login, return JWT and user details")
//    void login_valid() throws JsonProcessingException {
//        final String validToken = "some.valid.token";
//        final UserDetails user = UserDetails.builder()
//                .id(-1)
//                .password(null)
//                .email("e@mail.com")
//                .username("user")
//                .roles(Collections.emptySet())
//                .build();
//
//        final Login login = Login.builder()
//                .password("valid")
//                .email(user.getEmail())
//                .build();
//        when(authServiceClient.login(login))
//                .thenReturn(Mono.just(Tuples.of(
//                        validToken,
//                        user
//                )));
//
//        final WebTestClient.ResponseSpec ok = client.post()
//                .uri("/api/gateway/users/login")
//                .accept(APPLICATION_JSON)
//                .body(Mono.just(login), Login.class)
//                .exchange()
//                .expectStatus().isOk();
//
//        ok.expectBody()
//                .json(objectMapper.writeValueAsString(user));
//        ok.expectHeader()
//                .valueEquals(HttpHeaders.AUTHORIZATION, validToken);
//    }

//    @Test
//    @DisplayName("Given invalid Login, throw 401")
//    void login_invalid() {
//        final UserDetails user = UserDetails.builder()
//                .id(-1)
//                .password(null)
//                .email("e@mail.com")
//                .username("user")
//                .roles(Collections.emptySet())
//                .build();
//
//        final Login login = Login.builder()
//                .password("valid")
//                .email(user.getEmail())
//                .build();
//        final String message = "I live in unending agony. I spent 6 hours and ended up with nothing";
//        when(authServiceClient.login(login))
//                .thenThrow(new GenericHttpException(message, UNAUTHORIZED));
//
//        client.post()
//                .uri("/api/gateway/users/login")
//                .accept(APPLICATION_JSON)
//                .body(Mono.just(login), Login.class)
//                .exchange()
//                .expectStatus().isUnauthorized()
//                .expectBody()
//                .jsonPath("$.statusCode").isEqualTo(UNAUTHORIZED.value())
//                .jsonPath("$.message").isEqualTo(message)
//                .jsonPath("$.timestamp").exists();
//    }

    @Test
    @DisplayName("Should get all the roles")
    void shouldGetRoles() {
        Role parentRole = new Role();
        parentRole.setId(1);
        parentRole.setName("admin");

        Role role1 = new Role();
        role1.setId(2);
        role1.setName("vet");
        role1.setParent(parentRole);

        Role role2 = new Role();
        role2.setId(3);
        role2.setName("user");
        role2.setParent(parentRole);

        List<Role> allRolesList = new ArrayList<>();
        allRolesList.add(role1);
        allRolesList.add(role2);

        Flux<Role> allRoles = Flux.fromIterable(allRolesList);

        when(authServiceClient.getRoles("Bearer token"))
                .thenReturn(allRoles);

        client.get()
                .uri("/api/gateway/admin/roles")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("vet")
                .jsonPath("$[0].parent").isEqualTo(role1.getParent())
                .jsonPath("$[1].id").isEqualTo(3)
                .jsonPath("$[1].name").isEqualTo("user")
                .jsonPath("$[1].parent").isEqualTo(role2.getParent());

    }

    @Test
    void shouldAddRole() {
        final Role parentRole = new Role();
        parentRole.setId(1);
        parentRole.setName("admin");

        final Role role = new Role();
        role.setId(2);
        role.setName("vet");
        role.setParent(parentRole);

        when(authServiceClient.addRole("Bearer token", role))
                .thenReturn(Mono.just(role));

        client.post()
                .uri("/api/gateway/admin/roles")
                .header("Authorization", "Bearer token")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(role), Role.class)
                .exchange()
                .expectStatus().isOk();

        verify(authServiceClient).addRole("Bearer token", role);
    }

    @Test
    void shouldDeleteRole() {
        final Role parentRole = new Role();
        parentRole.setId(1);
        parentRole.setName("admin");

        final Role role = new Role();
        role.setId(2);
        role.setName("vet");
        role.setParent(parentRole);

        when(authServiceClient.addRole("Bearer token", role))
                .thenReturn(Mono.just(role));

        client.post()
                .uri("/api/gateway/admin/roles")
                .header("Authorization", "Bearer token")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(role), Role.class)
                .exchange()
                .expectStatus().isOk();

        verify(authServiceClient).addRole("Bearer token", role);

        client.delete()
                .uri("/api/gateway/admin/roles/{id}", role.getId())
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        verify(authServiceClient).deleteRole("Bearer token", role.getId());
    }
    //inventory tests



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

}


