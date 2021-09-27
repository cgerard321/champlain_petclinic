package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.ConnectException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//import com.petclinic.billing.datalayer.BillDTO;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = BFFApiGatewayController.class)
class ApiGatewayControllerTest {

    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @MockBean
    private VetsServiceClient vetsServiceClient;

    @MockBean
    private BillServiceClient billServiceClient;

    @Autowired
    private WebTestClient client;




    private static final int BILL_ID_OKAY = 1;
    private static final int BILL_ID_NOT_FOUND = 213;
    private static final String BILL_ID_INVALID_STRING = "not-integer";
    private static final int BILL_ID_NEGATIVE_VALUE = -1;

    private static final int BILL_ID = 1;


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
    public void getBillByProductId(){


        //int expectedLength = 1;

        BillDetails entity = new BillDetails();
        entity.setBillId(1);

        entity.setDate(null);

        entity.setAmount(599);

        entity.setVisitType("Consultation");

        client.get()
                //check the URI
                .uri("/api/gateway/bill/" + BILL_ID_OKAY)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
//                .jsonPath("$.length()").isEqualTo(expectedLength)
//                .jsonPath("$[0].billId").isEqualTo(BILL_ID_OKAY);
//                .jsonPath("$[1].billId").isEqualTo(BILL_ID_OKAY)
//                .jsonPath("$[2].billId").isEqualTo(BILL_ID_OKAY);

        assertEquals(entity.getBillId(), 1);


    }

//
//    @Test
//    public void getBillMissingParameter(){
//
//        Bill entity = new Bill(BILL_ID_OKAY,null, "Consultation", 500);
//        repository.save(entity);
//
//
//        client.get()
//                .uri("/api/gateway/bill")
//                .accept(APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectHeader().contentType(APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.path").isEqualTo("/bill")
//                .jsonPath("$.message").isEqualTo("Required int parameter 'billId' is not present");
//
//        assertTrue(repository.findByBillId(BILL_ID_OKAY).isPresent());
//
//    }

    @Test
    public void getBillInvalidParameterString(){
//        client.get()
//                //check the uri
//                .uri("bill/{billId}" + BILL_ID_INVALID_STRING)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.path").isEqualTo("/bill")
//                .jsonPath("$.message").isEqualTo("Type mismatch.");

    }

    @Test
    public void GetBillInvalidParameterNegativeValue(){
//        client.get()
//                .uri("bill/{billId}" + BILL_ID_NEGATIVE_VALUE)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.path").isEqualTo("/bill")
//                .jsonPath("$.message").isEqualTo("Invalid billId: "+ BILL_ID_NEGATIVE_VALUE);
    }

    @Test
    public void getBillsNotFound(){

//        int expectedLength = 0;
//        client.get()
//                .uri("bill/{billId}" + BILL_ID_NOT_FOUND)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.length()").isEqualTo(expectedLength);
//

    }


}

