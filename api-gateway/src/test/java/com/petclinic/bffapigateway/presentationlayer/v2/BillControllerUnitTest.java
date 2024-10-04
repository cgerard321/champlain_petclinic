package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        BillController.class,
        BillServiceClient.class,
        GlobalExceptionHandler.class
})
@WebFluxTest(controllers = BillController.class)
@AutoConfigureWebTestClient
public class BillControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private BillServiceClient billServiceClient;

private final String baseBillURL = "/api/v2/gateway/bills";

    private BillResponseDTO billresponse = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("3")
            .date(LocalDate.parse("2024-10-11"))
            .amount(100.0)
            .taxedAmount(0.0)
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.parse("2024-10-13"))
            .build();

    private BillResponseDTO billresponse2 = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361b")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("2")
            .date(LocalDate.parse("2024-10-11"))
            .amount(120.0)
            .taxedAmount(10.0)
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.parse("2024-10-13"))
            .build();


   @Test
    public void whenGetAllBillsByCustomerId_ThenReturnCustomerBills() {
       when(billServiceClient.getBillsByOwnerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")).thenReturn(Flux.just(billresponse, billresponse2));
        webTestClient.get()
                .uri(baseBillURL + "/customer/{customerId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);
    }

    @Test
    public void whenGetAllBillsByInvalidCustomerId_ThenReturnInvalidInput() {
        when(billServiceClient.getBillsByOwnerId("invalid-owner-id")).thenReturn(Flux.defer(() -> Flux.error(new InvalidInputException("Invalid owner id"))));
        webTestClient.get()
                .uri(baseBillURL + "/customer/{customerId}", "invalid-owner-id")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    }

    @Test
    public void whenGetAllBills_thenReturnAllBills(){
       when(billServiceClient.getAllBilling())
               .thenReturn(Flux.just(billresponse, billresponse2));

       webTestClient
               .get()
               .uri(baseBillURL + "/admin")
               .accept(MediaType.TEXT_EVENT_STREAM)
               .exchange()
               .expectStatus().isOk()
               .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
               .expectBodyList(BillResponseDTO.class)
               .hasSize(2)
               .contains(billresponse, billresponse2);

       verify(billServiceClient, times(1))
               .getAllBilling();
    }

    @Test
    public void whenGetBillById_thenReturnBill(){
        when(billServiceClient.getBilling("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a"))
                .thenReturn(Mono.just(billresponse));

        webTestClient
                .get()
                .uri(baseBillURL + "/admin/{billId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(1)
                .contains(billresponse);

        verify(billServiceClient, times(1))
                .getBilling("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
    }

    @Test
    public void whenGetAllPaidBills_thenReturnAllPaidBills(){
        when(billServiceClient.getAllPaidBilling())
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient
                .get()
                .uri(baseBillURL + "/admin/paid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1))
                .getAllPaidBilling();
    }

    @Test
    public void whenGetAllUnpaidBills_thenReturnAllUnpaidBills(){
        when(billServiceClient.getAllUnpaidBilling())
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient
                .get()
                .uri(baseBillURL + "/admin/unpaid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1))
                .getAllUnpaidBilling();
    }

    @Test
    public void whenGetAllOverdueBills_thenReturnAllOverdueBills(){
        when(billServiceClient.getAllOverdueBilling())
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient
                .get()
                .uri(baseBillURL + "/admin/overdue")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1))
                .getAllOverdueBilling();
    }

}