package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = BFFApiGatewayController.class)
@AutoConfigureWebTestClient
class ApiGatewayControllerTest {



    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @MockBean
    private VetsServiceClient vetsServiceClient;

    @MockBean
    private AuthServiceClient authServiceClient;

    @Autowired
    private WebTestClient client;


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
        when(authServiceClient.createUser(user)).thenReturn(Mono.just(user));

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

        when(authServiceClient.createUser(user))
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

//    @Test
//    void getPutRequestOk()  {
//
//        OwnerDetails od = new OwnerDetails();
//        od.setId(1);
//        od.setFirstName("John");
//        od.setLastName("Doe");
//        od.setAddress("1 Star Street");
//        od.setCity("Boston");
//        od.setTelephone("5553451125");
//
//        Mockito.when(customersServiceClient.updateOwner(od,1))
//                .thenReturn(Mono.just(od));
//
//        client.put()
//                .uri("api/gateway/owners/{ownerId}", 1)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody();
//
//
//        assertEquals(od.getId(), 1);
//        assertEquals(od.getFirstName(), "John");
//        assertEquals(od.getLastName(),"Doe");
//        assertEquals(od.getAddress(), "1 Star Street");
//        assertEquals(od.getCity(), "Boston");
//        assertEquals(od.getTelephone(),"5553451125");
//
//
//    }



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
}


