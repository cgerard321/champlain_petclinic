package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.PetDetails;
import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.Visits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = BFFApiGatewayController.class)
class ApiGatewayControllerTest {

    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @MockBean
    private VetsServiceClient vetsServiceClient;

    @Autowired
    private WebTestClient client;

    @Test
    void getOwnerDetails_withAvailableVisitsService() {
        OwnerDetails owner = new OwnerDetails();
        PetDetails cat = new PetDetails();
        cat.setId(20);
        cat.setName("Garfield");
        owner.getPets().add(cat);
        Mockito
                .when(customersServiceClient.getOwner(1))
                .thenReturn(Mono.just(owner));

        Visits visits = new Visits();
        VisitDetails visit = new VisitDetails();
        visit.setId(300);
        visit.setDescription("First visit");
        visit.setPetId(cat.getId());
        visits.getItems().add(visit);
        Mockito
                .when(visitsServiceClient.getVisitsForPets(Collections.singletonList(cat.getId())))
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
}

