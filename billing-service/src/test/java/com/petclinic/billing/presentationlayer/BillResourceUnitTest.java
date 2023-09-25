package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

@WebFluxTest(controllers = BillResource.class)
class BillResourceUnitTest {

    private BillResponseDTO responseDTO = buildBillResponseDTO();
    private final String BILL_ID_OK = responseDTO.getBillId();

    private final String CUSTOMER_ID_OK = responseDTO.getCustomerId();
    private final String VET_ID_OK = responseDTO.getVetId();

    @Autowired
    private WebTestClient client;

    @MockBean
    BillService billService;





    @Test
    void findBill() {

        when(billService.GetBill(anyString())).thenReturn(Mono.just(responseDTO));

        client.get()
                .uri("/bills/" + BILL_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(responseDTO.getVisitType())
                .jsonPath("$.customerId").isEqualTo(responseDTO.getCustomerId())
                .jsonPath("$.amount").isEqualTo(responseDTO.getAmount());

        Mockito.verify(billService, times(1)).GetBill(BILL_ID_OK);

    }

    @Test
    void findAllBills() {

        when(billService.GetAllBills()).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });
        Mockito.verify(billService, times(1)).GetAllBills();

    }

    @Test
    void getBillByCustomerId() {

        when(billService.GetBillsByCustomerId(anyString())).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/customer/" + responseDTO.getCustomerId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });
        Mockito.verify(billService, times(1)).GetBillsByCustomerId(CUSTOMER_ID_OK);


    }
    @Test
    void getBillByVetId() {

        when(billService.GetBillsByVetId(anyString())).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/vet/" + responseDTO.getVetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });

        Mockito.verify(billService, times(1)).GetBillsByVetId(VET_ID_OK);


    }
    @Test
    void deleteBill() {

        when(billService.DeleteBill(anyString())).thenReturn(Mono.empty());

        client.delete()
                .uri("/bills/" + responseDTO.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).DeleteBill(BILL_ID_OK);
    }

    @Test
    void deleteBillByVetId() {

        when(billService.DeleteBillsByVetId(anyString())).thenReturn(Flux.empty());

        client.delete()
                .uri("/bills/vet/" + responseDTO.getVetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).DeleteBillsByVetId(VET_ID_OK);
    }

    @Test
    void deleteBillsByCustomerId() {

        when(billService.DeleteBillsByCustomerId(anyString())).thenReturn(Flux.empty());

        client.delete()
                .uri("/bills/customer/" + responseDTO.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()//.isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                .expectBody();

        Mockito.verify(billService, times(1)).DeleteBillsByCustomerId(CUSTOMER_ID_OK);
    }

//    private BillDTO buildBillDTO(){
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2022, Calendar.SEPTEMBER, 25);
//        LocalDate date = calendar.getTime().toInstant()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDate();;
//
//
//        return BillDTO.builder().billId("BillUUID").customerId(1).vetId("1").visitType("Test Type").date(date).amount(13.37).build();
//    }

    private BillResponseDTO buildBillResponseDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();


        return BillResponseDTO.builder().billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).build();
    }
}
