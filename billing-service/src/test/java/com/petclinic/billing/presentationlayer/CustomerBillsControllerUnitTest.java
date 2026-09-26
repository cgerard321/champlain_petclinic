package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.domainclientlayer.Auth.AuthServiceClient;
import com.petclinic.billing.domainclientlayer.Auth.Rethrower;
import com.petclinic.billing.exceptionshandling.exceptions.InvalidPaymentException;
import com.petclinic.billing.exceptionshandling.exceptions.NotFoundException;
import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.presentationlayer.models.BillResponseModel;
import com.petclinic.billing.dataaccesslayer.BillStatus;
import com.petclinic.billing.presentationlayer.models.PaymentRequestModel;
import com.petclinic.billing.util.InterestCalculationUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

@WebFluxTest(controllers = CustomerBillsController.class)
public class CustomerBillsControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    BillService billService;
    @MockBean
    AuthServiceClient authServiceClient;

    @MockBean
    Rethrower rethrower;

    @Test
    void getBillsByCustomerId_shouldSucceed() {
        BillResponseModel billResponse = buildBillResponseDTO();

        when(billService.getBillsByCustomerId(anyString())).thenReturn(Flux.just(billResponse));

        client.get()
                .uri("/bills/customer/{customerId}/bills", billResponse.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    assert response.getResponseBody() != null;
                    assert response.getResponseBody().size() == 1;
                    assert response.getResponseBody().get(0).getCustomerId().equals(billResponse.getCustomerId());
                });

        verify(billService, times(1)).getBillsByCustomerId(billResponse.getCustomerId());
    }

    @Test
    void getBillsByNonExistentCustomerId_shouldFail() {
        when(billService.getBillsByCustomerId(anyString())).thenReturn(Flux.empty());

        client.get()
                .uri("/bills/customer/nonExistentCustomer/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseModel.class)
                .hasSize(0);

        verify(billService, times(1)).getBillsByCustomerId("nonExistentCustomer");
    }

    @Test
    void getCurrentBalance_ValidCustomer_ShouldReturnBalance() {
        String customerId = "valid-customer-id";
        BigDecimal expectedBalance = new BigDecimal("150.0");

        when(billService.calculateCurrentBalance(customerId)).thenReturn(Mono.just(expectedBalance));

        client.get()
                .uri("/bills/customer/{customerId}/bills/current-balance", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .value(balance -> assertEquals(expectedBalance, balance));

        verify(billService, times(1)).calculateCurrentBalance(customerId);
    }

    @Test
    void getCurrentBalance_InvalidCustomer_ShouldReturnNotFound() {
        String invalidCustomerId = "invalid-customer-id";

        when(billService.calculateCurrentBalance(invalidCustomerId))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));

        client.get()
                .uri("/bills/customer/{customerId}/bills/current-balance", invalidCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(billService, times(1)).calculateCurrentBalance(invalidCustomerId);
    }

    private BillResponseModel buildBillResponseDTO() {
        return BillResponseModel.builder()
                .billId("1")
                .customerId("custId")
                .vetId("vetId")
                .visitType("surgery")
                .billStatus(BillStatus.PAID)
                .amount(new BigDecimal(150.0))
                .build();
    }

    @Test
    void payBill_ValidRequest_ShouldReturnUpdatedBill() {
        String customerId = "cust-123";
        String billId = "bill-456";
        String jwtToken = "fake-cookie-token";
        PaymentRequestModel paymentRequest = new PaymentRequestModel("1234567812345678", "123", "12/25");

        BillResponseModel billResponse = BillResponseModel.builder()
                .billId(billId)
                .customerId(customerId)
                .billStatus(BillStatus.PAID)
                .amount(new BigDecimal(200.0))
                .build();

        when(billService.processPayment(customerId, billId, paymentRequest, jwtToken))
                .thenReturn(Mono.just(billResponse));

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", customerId, billId)
                .cookie("Bearer", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BillResponseModel.class)
                .consumeWith(response -> {
                    assert response.getResponseBody() != null;
                    assertEquals(BillStatus.PAID, response.getResponseBody().getBillStatus());
                });

        verify(billService, times(1)).processPayment(customerId, billId, paymentRequest, jwtToken);
    }

    @Test
    void payBill_InvalidPayment_ShouldReturnBadRequest() {
        String customerId = "cust-123";
        String billId = "bill-456";
        String jwtToken = "fake-cookie-token";
        PaymentRequestModel invalidPayment = new PaymentRequestModel("123", "12", "12");

        when(billService.processPayment(customerId, billId, invalidPayment, jwtToken))
                .thenReturn(Mono.error(new InvalidPaymentException("Invalid payment details")));

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", customerId, billId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue(invalidPayment)
                .exchange()
                .expectStatus().isBadRequest();

        verify(billService, times(1)).processPayment(customerId, billId, invalidPayment, jwtToken);
    }

    @Test
    void payBill_NonExistentBill_ShouldReturnNotFound() {
        String customerId = "cust-123";
        String billId = "bill-404";
        String jwtToken = "fake-cookie-token";
        PaymentRequestModel paymentRequest = new PaymentRequestModel("1234567812345678", "123", "12/25");

        when(billService.processPayment(customerId, billId, paymentRequest, jwtToken))
                .thenReturn(Mono.error(new NotFoundException("Bill not found")));

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", customerId, billId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isNotFound();

        verify(billService, times(1)).processPayment(customerId, billId, paymentRequest, jwtToken);
    }

    @Test
        void getBillsByCustomerId_OverdueBill_ShouldReturnInterest() {
                // Use centralized utility for compound interest calculation
                BigDecimal amount = new BigDecimal("100.00");
                LocalDate dueDate = LocalDate.now().minusMonths(1); // 1 month overdue
                LocalDate currentDate = LocalDate.now();
                BigDecimal calculatedInterest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);
                
                BillResponseModel overdueBill = BillResponseModel.builder()
                        .billId("overdue-1")
                        .customerId("custId")
                        .amount(amount)
                        .billStatus(BillStatus.OVERDUE)
                        .interest(calculatedInterest)
                        .build();

                when(billService.getBillsByCustomerId(anyString())).thenReturn(Flux.just(overdueBill));

                client.get()
                        .uri("/bills/customer/{customerId}/bills", overdueBill.getCustomerId())
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBodyList(BillResponseModel.class)
                        .consumeWith(response -> {
                                assert response.getResponseBody() != null;
                                // Compare using doubleValue to avoid BigDecimal precision issues (1.50 vs 1.5)
                                assertEquals(calculatedInterest.doubleValue(), response.getResponseBody().get(0).getInterest().doubleValue());
                        });
                verify(billService, times(1)).getBillsByCustomerId(overdueBill.getCustomerId());
        }

    @Test
    void getBillsByAmountRange_shouldReturnBills() {
        String customerId = "cust-1";

        // Prepare test DTO
        BillResponseModel bill = BillResponseModel.builder()
                .customerId(customerId)
                .amount(new BigDecimal("100.00"))
                .build();

        // Mock the service call
        when(billService.getBillsByAmountRange(eq(customerId), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Flux.just(bill));

        // Execute WebTestClient call
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills/customer/{customerId}/bills/filter-by-amount")
                        .queryParam("minAmount", "50")
                        .queryParam("maxAmount", "150")
                        .build(customerId))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseModel.class)
                .hasSize(1)
                .consumeWith(resp -> {
                    BillResponseModel responseBill = resp.getResponseBody().get(0);
                    assertNotNull(responseBill);
                    assertEquals(customerId, responseBill.getCustomerId());
                    assertTrue(responseBill.getAmount().compareTo(new BigDecimal("100.00")) == 0,
                            "Expected amount to be 100.00 but was " + responseBill.getAmount());
                });

    }


    @Test
    void getBillsByDueDateRange_noBills_shouldReturnNotFound() {
        String customerId = "cust-2";
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        when(billService.getBillsByDueDateRange(eq(customerId), eq(start), eq(end)))
                .thenReturn(Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills/customer/{customerId}/bills/filter-by-due-date")
                        .queryParam("startDate", start)
                        .queryParam("endDate", end)
                        .build(customerId))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getBillsByCustomerIdAndDateRange_shouldReturnBills() {
        String customerId = "cust-3";
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();
        BillResponseModel bill = BillResponseModel.builder()
                .customerId(customerId)
                .build();

        when(billService.getBillsByCustomerIdAndDateRange(eq(customerId), eq(start), eq(end)))
                .thenReturn(Flux.just(bill));

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills/customer/{customerId}/bills/filter-by-date")
                        .queryParam("startDate", start)
                        .queryParam("endDate", end)
                        .build(customerId))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseModel.class)
                .hasSize(1);
    }
}