package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import com.petclinic.billing.datalayer.PaymentRequestDTO;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

@WebFluxTest(controllers = BillController.class)
class BillControllerUnitTest {

    private BillResponseDTO responseDTO = buildBillResponseDTO();

    private BillResponseDTO unpaidResponseDTO = buildUnpaidBillResponseDTO();


    private BillResponseDTO overdueResponseDTO = buildBillOverdueResponseDTO();

    private final String BILL_ID_OK = responseDTO.getBillId();

    private final String CUSTOMER_ID_OK = responseDTO.getCustomerId();
    private final String VET_ID_OK = responseDTO.getVetId();

    @Autowired
    private WebTestClient client;

    @MockBean
    BillService billService;





    @Test
    void getBillByBillId() {

        when(billService.getBillByBillId(anyString())).thenReturn(Mono.just(responseDTO));

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

        Mockito.verify(billService, times(1)).getBillByBillId(BILL_ID_OK);
    }

    @Test
    void getAllBills() {

        when(billService.getAllBills()).thenReturn(Flux.just(responseDTO));

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
        Mockito.verify(billService, times(1)).getAllBills();

    }

    @Test
    void getAllPaidBills() {
        when(billService.getAllBillsByStatus(BillStatus.PAID)).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/paid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });

        Mockito.verify(billService, times(1)).getAllBillsByStatus(BillStatus.PAID);
    }

    @Test
    void getAllUnpaidBills() {
        when(billService.getAllBillsByStatus(BillStatus.UNPAID)).thenReturn(Flux.just(unpaidResponseDTO));

        client.get()
                .uri("/bills/unpaid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });

        Mockito.verify(billService, times(1)).getAllBillsByStatus(BillStatus.UNPAID);
    }

    @Test
    void getAllOverdueBills() {
        when(billService.getAllBillsByStatus(BillStatus.OVERDUE)).thenReturn(Flux.just(overdueResponseDTO));

        client.get()
                .uri("/bills/overdue")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });

        Mockito.verify(billService, times(1)).getAllBillsByStatus(BillStatus.OVERDUE);
    }

    @Test
    void getBillByCustomerId() {

        when(billService.getBillsByCustomerId(anyString())).thenReturn(Flux.just(responseDTO));

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
        Mockito.verify(billService, times(1)).getBillsByCustomerId(CUSTOMER_ID_OK);


    }
    @Test
    void getBillByVetId() {

        when(billService.getBillsByVetId(anyString())).thenReturn(Flux.just(responseDTO));

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

        Mockito.verify(billService, times(1)).getBillsByVetId(VET_ID_OK);


    }

    @Test
    void deleteAllBills() {
        when(billService.deleteAllBills()).thenReturn(Mono.empty());

        client.delete()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).deleteAllBills();
    }

    @Test
    void deleteBill() {

        when(billService.deleteBill(anyString())).thenReturn(Mono.empty());

        client.delete()
                .uri("/bills/" + responseDTO.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).deleteBill(BILL_ID_OK);
    }

    @Test
    void deleteBillByVetId() {

        when(billService.deleteBillsByVetId(anyString())).thenReturn(Flux.empty());

        client.delete()
                .uri("/bills/vet/" + responseDTO.getVetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).deleteBillsByVetId(VET_ID_OK);
    }

    @Test
    void deleteBillsByCustomerId() {

        when(billService.deleteBillsByCustomerId(anyString())).thenReturn(Flux.empty());

        client.delete()
                .uri("/bills/customer/" + responseDTO.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()//.isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                .expectBody();

        Mockito.verify(billService, times(1)).deleteBillsByCustomerId(CUSTOMER_ID_OK);
    }


    private BillResponseDTO buildBillResponseDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022,Month.OCTOBER,15);

        return BillResponseDTO.builder().billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.PAID).dueDate(dueDate).build();
    }

    private BillResponseDTO buildUnpaidBillResponseDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 5);

        return BillResponseDTO.builder().billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.UNPAID).dueDate(dueDate).build();
    }

    private BillResponseDTO buildBillOverdueResponseDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);

        return BillResponseDTO.builder().billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.OVERDUE).dueDate(dueDate).build();
    }

    @Test
    void whenValidParametersForPaginationProvided_thenShouldCallServiceWithCorrectParams() {
        // Mocking the service layer response
        when(billService.getAllBillsByPage(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(responseDTO));

        // Triggering the controller endpoint
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills")
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(1);  // Checking that the response body has exactly 1 element

        // Verifying the correct method calls
        Mockito.verify(billService, times(1))
                .getAllBillsByPage(PageRequest.of(1, 10), null, null, null,
                        null, null, null, null, null);
    }


    @Test
    void whenGetBillsByMonthCalled_thenShouldCallServiceWithCorrectParams() {
        // Mocking the service layer response
        when(billService.getBillsByMonth(anyInt(), anyInt()))
                .thenReturn(Flux.just(responseDTO));

        // Triggering the controller endpoint
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills/month")
                        .queryParam("month", 1)
                        .queryParam("year", 2022)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(1);  // Checking that the response body has exactly 1 element

        // Verifying the correct method calls with correct argument order
        Mockito.verify(billService, times(1))
                .getBillsByMonth(2022, 1);  // year first, then month
    }

    @Test
    void whenGetBillsByMonthCalledWithInvalidParams_thenShouldBadRequest() {
        // Triggering the controller endpoint with invalid parameters
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills/month")
                        .queryParam("month", 13)
                        .queryParam("year", -1)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }


}
