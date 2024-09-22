//package com.petclinic.billing.presentationlayer;
//
//import com.petclinic.billing.businesslayer.BillService;
//import com.petclinic.billing.datalayer.BillResponseDTO;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Flux;
//
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//@WebFluxTest(CustomerBillsController.class)
//public class CustomerBillsControllerUnitTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockBean
//    private BillService billService;
//
//    @Test
//    public void testGetBillsByCustomerId() {
//        Flux<BillResponseDTO> billFlux = Flux.just(new BillResponseDTO(/* Provide necessary values */));
//
//        when(billService.GetBillsByCustomerId(anyString())).thenReturn(billFlux);
//
//        webTestClient.get().uri("/customers/123/bills")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(BillResponseDTO.class);
//    }
//}
