package com.petclinic.bffapigateway.presentationlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.*;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
//
//import com.petclinic.billing.datalayer.BillDTO;



@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = BFFApiGatewayController.class)
@AutoConfigureWebTestClient
class ApiGatewayControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @MockBean
    private VetsServiceClient vetsServiceClient;

    @MockBean
    private AuthServiceClient authServiceClient;

    @MockBean
    private BillServiceClient billServiceClient;

    @Autowired
    private WebTestClient client;


    Integer id = new Integer(1);
    Integer id2 = new Integer(2);

    @Test
    void createAndDeleteVet() {

        final int vetId = 1234567;
        VetDetails vet = new VetDetails();
        vet.setVetId(vetId);
        vet.setFirstName("Kevin");
        vet.setLastName("Tremblay");
        vet.setEmail("hello@test.com");
        vet.setPhoneNumber("1-800-GOT-JUNK");
        vet.setResume("Working since I started working.");
        vet.setWorkday("Monday");

        when(vetsServiceClient.createVet(vet))
                .thenReturn(Mono.just(vet));

        client.post()
                .uri("/api/gateway/vets")
                .body(Mono.just(vet), VetDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(vetId, vet.getVetId());

        client.delete()
                .uri("/api/gateway/vets/" + vetId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, vetsServiceClient.getVet(vetId));
    }




    @Test
    void getOwnerDetails_withAvailableVisitsService() {
        OwnerDetails owner = new OwnerDetails();
        PetDetails cat = new PetDetails();
        cat.setId(20);
        cat.setName("Garfield");
        owner.getPets().add(cat);
        when(customersServiceClient.getOwner(1))
                .thenReturn(Mono.just(owner));

        Visits visits = new Visits();
        VisitDetails visit = new VisitDetails();
        visit.setId(300);
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
    }

    @Test
    void getUserDetails() {
        UserDetails user = new UserDetails();
        user.setId(1);
        user.setUsername("roger675");
        user.setPassword("secretnooneknows");
        user.setEmail("RogerBrown@gmail.com");

        when(authServiceClient.getUser(1))
                .thenReturn(Mono.just(user));

        client.get()

                .uri("/api/gateway/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("roger675")
                .jsonPath("$.password").isEqualTo("secretnooneknows")
                .jsonPath("$.email").isEqualTo("RogerBrown@gmail.com");

        assertEquals(user.getId(), 1);
    }
    
    @Test
    void createUser(){
        UserDetails user = new UserDetails();
        user.setId(1);
        user.setUsername("Johnny123");
        user.setPassword("password");
        user.setEmail("email@email.com");
        when(authServiceClient.createUser(argThat(
                n -> user.getEmail().equals(n.getEmail())
        ))).thenReturn(Mono.just(user));

        client.post()
                .uri("/api/gateway/users")
                .body(Mono.just(user), UserDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(user.getId(), 1);
        assertEquals(user.getUsername(), "Johnny123");
        assertEquals(user.getPassword(), "password");
        assertEquals(user.getEmail(), "email@email.com");

    }



      @Test
      void createOwner(){
        OwnerDetails owner = new OwnerDetails();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Johnny");
        owner.setAddress("111 John St");
        owner.setCity("Johnston");
        owner.setTelephone("51451545144");
        when(customersServiceClient.createOwner(owner))
                .thenReturn(Mono.just(owner));


        client.post()
                .uri("/api/gateway/owners")
                .body(Mono.just(owner), OwnerDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();



        assertEquals(owner.getId(),1);
        assertEquals(owner.getFirstName(),"John");
        assertEquals(owner.getLastName(),"Johnny");
        assertEquals(owner.getAddress(),"111 John St");
        assertEquals(owner.getCity(),"Johnston");
        assertEquals(owner.getTelephone(),"51451545144");
    }

    @Test
    void deleteUser() {
        UserDetails user = new UserDetails();
        user.setId(1);
        user.setUsername("johndoe");
        user.setPassword("pass");
        user.setEmail("johndoe2@gmail.com");

        when(authServiceClient.createUser(argThat(
                n -> user.getEmail().equals(n.getEmail())
        )))
                .thenReturn(Mono.just(user));

        client.post()
                .uri("/api/gateway/users")
                .body(Mono.just(user), UserDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(1, user.getId());

        client.delete()
                .uri("/api/gateway/users/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, authServiceClient.getUser(user.getId()));
    }



    //private static final int BILL_ID = 1;

    @Test
    public void getBillById(){

        //int expectedLength = 1;

        BillDetails entity = new BillDetails();

        entity.setBillId(1);

        entity.setAmount(599);

        entity.setCustomerId(2);

        entity.setVisitType("Consultation");

        when(billServiceClient.getBilling(1))
                .thenReturn(Mono.just(entity));

        client.get()
                //check the URI
                .uri("/api/gateway/bills/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.billId").isEqualTo(1)
                .jsonPath("$.customerId").isEqualTo(entity.getCustomerId())
                .jsonPath("$.visitType").isEqualTo(entity.getVisitType())
                .jsonPath("$.amount").isEqualTo(entity.getAmount());




        assertEquals(entity.getBillId(), 1);


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
    void shouldCreateAVisitWithOwnerInfo(){
        OwnerDetails owner = new OwnerDetails();
        VisitDetails visit = new VisitDetails();
        owner.setId(1);
        visit.setId(1);
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
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.petId").isEqualTo(1)
                .jsonPath("$.date").isEqualTo("2021-12-12")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);



        assertEquals(id, visit.getId());
    }
    @Test
    void shouldDeleteAVisit() {
        VisitDetails visit = new VisitDetails();
        OwnerDetails owner = new OwnerDetails();
        owner.setId(1);
        visit.setId(1);
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
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.petId").isEqualTo(1)
                .jsonPath("$.date").isEqualTo("2021-12-12")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);

        assertEquals(id, visit.getId());

        client.delete()
                .uri("/api/gateway/pets/visits/{petId}", visit.getPetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, visitsServiceClient.getVisitsForPet(visit.getId()));
    }

    @Test
    void shouldDeleteVisitsById() {
        VisitDetails visit = new VisitDetails();
        OwnerDetails owner = new OwnerDetails();
        owner.setId(1);
        visit.setId(1);
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
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.petId").isEqualTo(1)
                .jsonPath("$.date").isEqualTo("2021-12-12")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);;

        assertEquals(id, visit.getId());

        client.delete()
                .uri("/api/gateway/visits/{visitId}", visit.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, visitsServiceClient.getVisitsForPet(visit.getId()));
    }

    @Test
    void shouldDeleteVisitsByPetId() {
        VisitDetails visit = new VisitDetails();
        OwnerDetails owner = new OwnerDetails();
        owner.setId(1);
        visit.setId(1);
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
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.petId").isEqualTo(1)
                .jsonPath("$.date").isEqualTo("2021-12-12")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);;

        assertEquals(id, visit.getId());

        client.delete()
                .uri("/api/gateway/pets/visits/{petId}", visit.getPetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, visitsServiceClient.getVisitsForPet(visit.getId()));
    }

    @Test
    void shouldUpdateAVisitsById() {
        VisitDetails visit = new VisitDetails();
        OwnerDetails owner = new OwnerDetails();
        owner.setId(1);
        visit.setId(1);
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        VisitDetails visit2 = new VisitDetails();
        OwnerDetails owner2 = new OwnerDetails();

        owner2.setId(2);
        visit2.setId(2);
        visit2.setPetId(2);
        visit2.setDate("2034-12-12");
        visit2.setDescription("Charle's Richard dog has a paw infection.");
        visit2.setStatus(false);
        visit2.setPractitionerId(2);


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
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.petId").isEqualTo(1)
                .jsonPath("$.date").isEqualTo("2021-12-12")
                .jsonPath("$.description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$.status").isEqualTo(false)
                .jsonPath("$.practitionerId").isEqualTo(1);


        assertEquals(id, visit.getId());

        when(visitsServiceClient.updateVisitForPet(visit))
                .thenReturn(Mono.just(visit2));

        client.put()
                .uri("/api/gateway/pets/visits/{petId}",visit.getPetId())
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
        visit.setId(1);
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        when(visitsServiceClient.getVisitsForPet(visit.getPetId()))
                .thenReturn(Flux.just(visit));

        client.get()
                .uri("/api/gateway/visits/{petId}", visit.getPetId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].petId").isEqualTo(1)
                .jsonPath("$[0].date").isEqualTo("2021-12-12")
                .jsonPath("$[0].description").isEqualTo("Charle's Richard cat has a paw infection.")
                .jsonPath("$[0].practitionerId").isEqualTo(1);
    }

    @Test
    @DisplayName("Given valid JWT, verify user")
    void verify_user() throws JsonProcessingException {

        final String validToken = "some.valid.token";
        final UserDetails user = UserDetails.builder()
                .id(1)
                .password(null)
                .email("e@mail.com")
                .username("user")
                .roles(Collections.emptySet())
                .build();

        when(authServiceClient.verifyUser(validToken))
                .thenReturn(Mono.just(user));

        client.get()
                .uri("/api/gateway/verification/{token}", validToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(user));
    }

    @Test
    @DisplayName("Given invalid JWT, expect 400")
    void verify_user_bad_token() {

        final String errorMessage = "some error message";
        final String invalidToken = "some.invlaid.token";

        when(authServiceClient.verifyUser(invalidToken))
                .thenThrow(new GenericHttpException(errorMessage, BAD_REQUEST));

        client.get()
                .uri("/api/gateway/verification/{token}", invalidToken)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.message").isEqualTo(errorMessage);
    }
}


