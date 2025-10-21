package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.presentationlayer.v1.BillControllerV1;
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

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        BillController.class,
        BillServiceClient.class,
        GlobalExceptionHandler.class,
        BillControllerV1.class
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


    public void AddBill_thenReturnBill(){
        when(billServiceClient.createBill(billRequestDTO, false, "jwtToken")).thenReturn(Mono.just(billresponse));
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
    public void whenGetBillsByCustomerIdPaginated_thenReturnBillList(){
        when(billServiceClient.getBillsByCustomerIdPaginated("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", Optional.of(0), Optional.of(5)))
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseBillURL + "/customer/{customerId}/paginated")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .build("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a"))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1)).getBillsByCustomerIdPaginated("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", Optional.of(0), Optional.of(5));
    }

    @Test
    void getInterest_Positive_ReturnsInterest() {
        // Arrange
        String billId = "B-123";
        Double interest = 15.75;

        // Mock the service call
        when(billServiceClient.getInterest(billId)).thenReturn(Mono.just(interest));

        // Act & Assert
        webTestClient.get()
                .uri(baseBillURL + "/admin/{billId}/interest", billId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(interest);

        // Verify the interaction
        verify(billServiceClient, times(1)).getInterest(billId);
    }

    @Test
    void getTotalWithInterest_Positive_ReturnsTotal() {
        // Arrange
        String billId = "B-123";
        Double totalWithInterest = 115.75;

        when(billServiceClient.getTotalWithInterest(billId)).thenReturn(Mono.just(totalWithInterest));

        // Act & Assert
        webTestClient.get()
                .uri(baseBillURL + "/admin/{billId}/total", billId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(totalWithInterest);

        verify(billServiceClient, times(1)).getTotalWithInterest(billId);
    }

    @Test
    void setInterestExempt_Positive_SetsExemption() {
        // Arrange
        String billId = "B-123";
        boolean exempt = true;

        when(billServiceClient.setInterestExempt(billId, exempt)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.patch()
                .uri(baseBillURL + "/{billId}/exempt-interest?exempt={exempt}", billId, exempt)
                .exchange()
                .expectStatus().isNoContent();

        verify(billServiceClient, times(1)).setInterestExempt(billId, exempt);
    }

    @Test
    void getInterestForCustomer_Positive_ReturnsInterest() {
        // Arrange
        String customerId = "C-123";
        String billId = "B-123";
        Double interest = 15.75;

        when(billServiceClient.getInterest(billId)).thenReturn(Mono.just(interest));

        // Act & Assert
        webTestClient.get()
                .uri(baseBillURL + "/customer/{customerId}/bills/{billId}/interest", customerId, billId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(interest);

        verify(billServiceClient, times(1)).getInterest(billId);
    }

    @Test
    void getTotalForCustomer_Positive_ReturnsTotal() {
        // Arrange
        String customerId = "C-123";
        String billId = "B-123";
        Double totalWithInterest = 115.75;

        when(billServiceClient.getTotalWithInterest(billId)).thenReturn(Mono.just(totalWithInterest));

        // Act & Assert
        webTestClient.get()
                .uri(baseBillURL + "/customer/{customerId}/bills/{billId}/total", customerId, billId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .isEqualTo(totalWithInterest);

        verify(billServiceClient, times(1)).getTotalWithInterest(billId);
    }
    
}