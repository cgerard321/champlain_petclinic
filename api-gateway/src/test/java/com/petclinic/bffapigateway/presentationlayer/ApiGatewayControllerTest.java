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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;



import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;


import static org.springframework.http.HttpStatus.*;

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

    void shouldCreatePet(){

        OwnerDetails od = new OwnerDetails();
        od.setId(1);
        PetDetails pet = new PetDetails();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getId()))

        .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getId())

                .body(Mono.just(pet), PetDetails.class)
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


    @Test
    void shouldThrowUnsupportedMediaTypeIfBodyDoesNotExist(){
        OwnerDetails od = new OwnerDetails();
        od.setId(0);
        PetDetails pet = new PetDetails();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getId()))
        .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(UNSUPPORTED_MEDIA_TYPE)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/api/gateway/owners/0/pets");


    }

    @Test
    void ifOwnerIdIsNotSpecifiedInUrlThrowNotAllowed(){
        OwnerDetails od = new OwnerDetails();
        PetDetails pet = new PetDetails();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getId()))
                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/pets")
                .body(Mono.just(pet), PetDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(METHOD_NOT_ALLOWED)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/api/gateway/owners/pets");
    }

    @Test
    void shouldCreateThenDeletePet(){
        OwnerDetails od = new OwnerDetails();
        od.setId(1);
        PetDetails pet = new PetDetails();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getId()))

                .thenReturn(Mono.just(pet));


        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getId())
                .body(Mono.just(pet), PetDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets/{petId}",od.getId(), pet.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();


    }

    @Test
    void shouldThrowNotFoundWhenOwnerIdIsNotSpecifiedOnDeletePets(){
        OwnerDetails od = new OwnerDetails();
        od.setId(1);
        PetDetails pet = new PetDetails();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getId()))

                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getId())
                .body(Mono.just(pet), PetDetails.class)
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

    @Test
    void shouldThrowMethodNotAllowedWhenDeletePetsIsMissingPetId(){
        OwnerDetails od = new OwnerDetails();
        od.setId(1);
        PetDetails pet = new PetDetails();
        PetType type = new PetType();
        type.setName("Dog");
        pet.setId(30);
        pet.setName("Fluffy");
        pet.setBirthDate("2000-01-01");
        pet.setType(type);

        when(customersServiceClient.createPet(pet,od.getId()))

                .thenReturn(Mono.just(pet));

        client.post()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getId())
                .body(Mono.just(pet), PetDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        client.delete()
                .uri("/api/gateway/owners/{ownerId}/pets", od.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(METHOD_NOT_ALLOWED)
                .expectBody();
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
    void createBill(){
        BillDetails bill = new BillDetails();
        bill.setBillId(1);

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



        assertEquals(bill.getBillId(),1);
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
            bill.setBillId(1);

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

            assertEquals(bill.getBillId(),1);
        client.delete()
                .uri("/api/gateway/bills/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        assertEquals(null, billServiceClient.getBilling(bill.getBillId()));
    }



    @Test
    void shouldCreateAVisitWithOwnerInfo(){
        OwnerDetails owner = new OwnerDetails();
        VisitDetails visit = new VisitDetails();
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
    }
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

    @Test
    void shouldUpdateAVisitsById() {
        VisitDetails visit = new VisitDetails();
        OwnerDetails owner = new OwnerDetails();
        owner.setId(1);
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(1);
        visit.setDate("2021-12-12");
        visit.setDescription("Charle's Richard cat has a paw infection.");
        visit.setStatus(false);
        visit.setPractitionerId(1);

        VisitDetails visit2 = new VisitDetails();
        OwnerDetails owner2 = new OwnerDetails();

        owner2.setId(1);
        visit2.setVisitId(UUID.randomUUID().toString());
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
                .uri("/api/gateway/visits/{petId}", visit.getPetId())
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
        VisitDetails visit = new VisitDetails();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setPetId(7);
        visit.setDate("2022-04-20");
        visit.setDescription("Fetching a single visit!");
        visit.setStatus(false);
        visit.setPractitionerId(177013);
        
        when(visitsServiceClient.getVisitByVisitId(anyString())).thenReturn(Mono.just(visit));
    
        client.get()
                .uri("/api/gateway/visit/{visitId}", visit.getVisitId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visit.getVisitId())
                .jsonPath("$.petId").isEqualTo(visit.getPetId())
                .jsonPath("$.date").isEqualTo(visit.getDate())
                .jsonPath("$.description").isEqualTo(visit.getDescription())
                .jsonPath("$.practitionerId").isEqualTo(visit.getPractitionerId());
    }
    
    @Test
    void getSingleVisit_Invalid() {
        final String invalidVisitId = "invalid";
        final String expectedErrorMessage = "error message";
    
        when(visitsServiceClient.getVisitByVisitId(invalidVisitId))
                .thenThrow(new GenericHttpException(expectedErrorMessage, BAD_REQUEST));
        
        client.get()
                .uri("/api/gateway/visit/{visitId}", invalidVisitId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(BAD_REQUEST.value())
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.message").isEqualTo(expectedErrorMessage);
    }

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
        final String invalidToken = "some.invalid.token";

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

    @Test
    @DisplayName("Given valid Login, return JWT and user details")
    void login_valid() throws JsonProcessingException {
        final String validToken = "some.valid.token";
        final UserDetails user = UserDetails.builder()
                .id(-1)
                .password(null)
                .email("e@mail.com")
                .username("user")
                .roles(Collections.emptySet())
                .build();

        final Login login = Login.builder()
                .password("valid")
                .email(user.getEmail())
                .build();
        when(authServiceClient.login(login))
                .thenReturn(Mono.just(Tuples.of(
                        validToken,
                        user
                )));

        final WebTestClient.ResponseSpec ok = client.post()
                .uri("/api/gateway/users/login")
                .accept(APPLICATION_JSON)
                .body(Mono.just(login), Login.class)
                .exchange()
                .expectStatus().isOk();

        ok.expectBody()
                .json(objectMapper.writeValueAsString(user));
        ok.expectHeader()
                .valueEquals(HttpHeaders.AUTHORIZATION, validToken);
    }

    @Test
    @DisplayName("Given invalid Login, throw 401")
    void login_invalid() {
        final UserDetails user = UserDetails.builder()
                .id(-1)
                .password(null)
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

        when(authServiceClient.getRoles())
                .thenReturn(allRoles);

        client.get()
                .uri("/api/gateway/admin/roles")
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

        when(authServiceClient.addRole(role))
                .thenReturn(Mono.just(role));

        client.post()
                .uri("/api/gateway/admin/roles")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(role), Role.class)
                .exchange()
                .expectStatus().isOk();

        verify(authServiceClient).addRole(role);
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

        when(authServiceClient.addRole(role))
                .thenReturn(Mono.just(role));

        client.post()
                .uri("/api/gateway/admin/roles")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(role), Role.class)
                .exchange()
                .expectStatus().isOk();

        verify(authServiceClient).addRole(role);

        client.delete()
                .uri("/api/gateway/admin/roles/{id}", role.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        verify(authServiceClient).deleteRole(role.getId());
    }
}


