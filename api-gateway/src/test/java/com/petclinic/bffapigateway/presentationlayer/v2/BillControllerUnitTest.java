package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.dtos.Bills.PaymentRequestDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
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

    private final BillResponseDTO billresponse = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("3")
            .date(LocalDate.parse("2024-10-11"))
            .amount(new BigDecimal("100.0"))
            .taxedAmount(new BigDecimal("0.0"))
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.parse("2024-10-13"))
            .build();

    private BillResponseDTO billresponse2 = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361b")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("2")
            .date(LocalDate.parse("2024-10-11"))
            .amount(new BigDecimal("120.0"))
            .taxedAmount(new BigDecimal("10.0"))
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.parse("2024-10-13"))
            .build();

    private BillRequestDTO billRequestDTO = BillRequestDTO.builder()
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("3")
            .date(LocalDate.parse("2024-10-11"))
            .amount(new BigDecimal("100.0"))
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
       when(billServiceClient.getAllBills())
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
               .getAllBills();
    }

    @Test
    public void whenGetBillById_thenReturnBill(){
        when(billServiceClient.getBillById("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a"))
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
                .getBillById("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
    }

    @Test
    public void whenGetAllPaidBills_thenReturnAllPaidBills(){
        when(billServiceClient.getAllPaidBills())
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
                .getAllPaidBills();
    }

    @Test
    public void whenGetAllUnpaidBills_thenReturnAllUnpaidBills(){
        when(billServiceClient.getAllUnpaidBills())
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
                .getAllUnpaidBills();
    }

    @Test
    public void whenGetAllOverdueBills_thenReturnAllOverdueBills() {
        when(billServiceClient.getAllOverdueBills())
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
                .getAllOverdueBills();
    }
    public void AddBill_thenReturnBill(){
        when(billServiceClient.createBill(billRequestDTO)).thenReturn(Mono.just(billresponse));
        webTestClient.post()
                .uri(baseBillURL + "/admin")
                .bodyValue(billRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BillResponseDTO.class)
                .isEqualTo(billresponse);
    }

    @Test
    public void whenGetAllBillsByPageWithValidParameters_ThenReturnPagedBills() {
        when(billServiceClient.getAllBillsByPage(Optional.of(1), Optional.of(5), null, null,
                null, null, null, null, null, null))
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseBillURL)
                        .queryParam("page", 1)
                        .queryParam("size", 5)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1)).getAllBillsByPage(Optional.of(1),
                Optional.of(5), null, null, null, null, null,
                null, null, null);
    }

    @Test
    public void whenGetAllBillsByPageWithInvalidParameters_ThenReturnBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseBillURL)
                        .queryParam("page", -1)
                        .queryParam("size", "invalid")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void whenGetBillsByMonthWithValidParameters_ThenReturnBills() {
        when(billServiceClient.getBillsByMonth(2024, 10))
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseBillURL + "/admin/month")
                        .queryParam("year", 2024)
                        .queryParam("month", 10)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1)).getBillsByMonth(2024, 10);
    }

    @Test
    public void whenGetBillsByMonthWithInvalidParameters_ThenReturnUnprocessableEntity() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseBillURL + "/admin/month")
                        .queryParam("year", -1)
                        .queryParam("month", 13)
                        .build())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Test
    public void whenDeleteBill_Succeeds_ThenReturnNoContent() {
        String billId = "B-77";

        when(billServiceClient.deleteBill(billId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(baseBillURL + "/{billId}", billId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_NO_CONTENT);

        verify(billServiceClient, times(1)).deleteBill(billId);
    }

    @Test
    public void whenDeleteBill_Unprocessable_ThenReturn422() {
        String billId = "B-88";

        when(billServiceClient.deleteBill(billId))
                .thenReturn(Mono.error(new InvalidInputException("Cannot delete a bill that is unpaid or overdue.")));

        webTestClient.delete()
                .uri(baseBillURL + "/{billId}", billId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        verify(billServiceClient, times(1)).deleteBill(billId);
    }

}