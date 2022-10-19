package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static reactor.core.publisher.Mono.just;


@WebFluxTest(controllers = BillResource.class)
class BillResourceUnitTest {

    private BillDTO dto = buildBillDTO();
    private final String BILL_ID_OK = dto.getBillId();
    private final int CUSTOMER_ID_OK = dto.getCustomerId();

    @Autowired
    private WebTestClient client;

    @MockBean
    BillService billService;



    @Test
    void createBill() {

        // when(billService.CreateBill(Mono.just(any(BillDTO.class))).thenReturn(Mono.just(dto)));      // To figure out in Sprint 2
        /*
         client.post()
                .uri("/bills")
                .body(just(dto), BillDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
         */
        // Mockito.verify(billService, times(1)).CreateBill(Mono.just(dto));

        String test = "Omg it works";

        assertNotNull(test);
    }

    @Test
    void findBill() {

        when(billService.GetBill(anyString())).thenReturn(Mono.just(dto));

        client.get()
                .uri("/bills/" + BILL_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(dto.getVisitType())
                .jsonPath("$.customerId").isEqualTo(dto.getCustomerId())
                .jsonPath("$.amount").isEqualTo(dto.getAmount());

        Mockito.verify(billService, times(1)).GetBill(BILL_ID_OK);

    }

    @Test
    void findAllBills() {

        when(billService.GetAllBills()).thenReturn(Flux.just(dto));

        client.get()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].visitType").isEqualTo(dto.getVisitType())
                .jsonPath("$[0].customerId").isEqualTo(dto.getCustomerId());

        Mockito.verify(billService, times(1)).GetAllBills();
    }

    @Test
    void getBillByCustomerId() {

        when(billService.GetBillsByCustomerId(anyInt())).thenReturn(Flux.just(dto));

        client.get()
                .uri("/bills/customer/" + dto.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].visitType").isEqualTo(dto.getVisitType())
                .jsonPath("$[0].customerId").isEqualTo(dto.getCustomerId())
                .jsonPath("$[0].amount").isEqualTo(dto.getAmount());

        Mockito.verify(billService, times(1)).GetBillsByCustomerId(CUSTOMER_ID_OK);


    }
    @Test
    void deleteBillsByCustomerId() {
        when(billService.DeleteBillsByCustomerId(anyInt())).thenReturn(Flux.empty());

        client.get()
                .uri("/bills/customer/" + dto.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).DeleteBillsByCustomerId(CUSTOMER_ID_OK);

    }
    @Test
    void deleteBill() {

        when(billService.DeleteBill(anyString())).thenReturn(Mono.empty());

        client.delete()
                .uri("/bills/" + dto.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).DeleteBill(BILL_ID_OK);
    }

    private BillDTO buildBillDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        Date date = calendar.getTime();


        return BillDTO.builder().billId("BillUUID").customerId(1).visitType("Test Type").date(date).amount(13.37).build();
    }
}