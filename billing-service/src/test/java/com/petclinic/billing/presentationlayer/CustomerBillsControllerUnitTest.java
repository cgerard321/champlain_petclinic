package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CustomerBillsController.class)
public class CustomerBillsControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    BillService billService;

    @Test
    void getBillsByCustomerId_shouldSucceed() {
        BillResponseDTO billResponse = buildBillResponseDTO();

        when(billService.GetBillsByCustomerId(anyString())).thenReturn(Flux.just(billResponse));

        client.get()
                .uri("/customers/{customerId}/bills", billResponse.getCustomerId())
                .accept(MediaType.APPLICATION_JSON) 
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    assert response.getResponseBody() != null;
                    assert response.getResponseBody().size() == 1;
                    assert response.getResponseBody().get(0).getCustomerId().equals(billResponse.getCustomerId());
                });

        Mockito.verify(billService, times(1)).GetBillsByCustomerId(billResponse.getCustomerId());
    }

    @Test
    void getBillsByNonExistentCustomerId_shouldFail() {
        when(billService.GetBillsByCustomerId(anyString())).thenReturn(Flux.empty());

        client.get()
                .uri("/customers/nonExistentCustomer/bills")
                .accept(MediaType.APPLICATION_JSON) 
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(0);

        Mockito.verify(billService, times(1)).GetBillsByCustomerId("nonExistentCustomer");
    }

    private BillResponseDTO buildBillResponseDTO() {
        return BillResponseDTO.builder()
                .billId("1")
                .customerId("custId")
                .vetId("vetId")
                .visitType("surgery")
                .billStatus(BillStatus.PAID)
                .amount(150.0)
                .build();
    }
}
