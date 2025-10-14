package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.domainclientlayer.Auth.AuthServiceClient;
import com.petclinic.billing.domainclientlayer.Auth.Rethrower;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.exceptions.NotFoundException;
import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import com.petclinic.billing.datalayer.PaymentRequestDTO;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.util.EntityDtoUtil;
import com.petclinic.billing.util.InterestCalculationUtil;
import com.petclinic.billing.util.InterestCalculationUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        BillResponseDTO billResponse = buildBillResponseDTO();

        when(billService.getBillsByCustomerId(anyString())).thenReturn(Flux.just(billResponse));

        client.get()
                .uri("/bills/customer/{customerId}/bills", billResponse.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
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
                .expectBodyList(BillResponseDTO.class)
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

    private BillResponseDTO buildBillResponseDTO() {
        return BillResponseDTO.builder()
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
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/25");

        BillResponseDTO billResponse = BillResponseDTO.builder()
                .billId(billId)
                .customerId(customerId)
                .billStatus(BillStatus.PAID)
                .amount(new BigDecimal(200.0))
                .build();

        when(billService.processPayment(customerId, billId, paymentRequest))
                .thenReturn(Mono.just(billResponse));

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", customerId, billId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BillResponseDTO.class)
                .consumeWith(response -> {
                    assert response.getResponseBody() != null;
                    assertEquals(BillStatus.PAID, response.getResponseBody().getBillStatus());
                });

        verify(billService, times(1)).processPayment(customerId, billId, paymentRequest);
    }

    @Test
    void payBill_InvalidPayment_ShouldReturnBadRequest() {
        String customerId = "cust-123";
        String billId = "bill-456";
        PaymentRequestDTO invalidPayment = new PaymentRequestDTO("123", "12", "12");

        when(billService.processPayment(customerId, billId, invalidPayment))
                .thenReturn(Mono.error(new InvalidPaymentException("Invalid payment details")));

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", customerId, billId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidPayment)
                .exchange()
                .expectStatus().isBadRequest();

        verify(billService, times(1)).processPayment(customerId, billId, invalidPayment);
    }

    @Test
    void payBill_NonExistentBill_ShouldReturnNotFound() {
        String customerId = "cust-123";
        String billId = "bill-404";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/25");

        when(billService.processPayment(customerId, billId, paymentRequest))
                .thenReturn(Mono.error(new NotFoundException("Bill not found")));

        client.post()
                .uri("/bills/customer/{customerId}/bills/{billId}/pay", customerId, billId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isNotFound();

        verify(billService, times(1)).processPayment(customerId, billId, paymentRequest);
    }

    @Test
        void getBillsByCustomerId_OverdueBill_ShouldReturnInterest() {
                // Use centralized utility for compound interest calculation
                BigDecimal amount = new BigDecimal("100.00");
                LocalDate dueDate = LocalDate.now().minusMonths(1); // 1 month overdue
                LocalDate currentDate = LocalDate.now();
                BigDecimal calculatedInterest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);
                
                BillResponseDTO overdueBill = BillResponseDTO.builder()
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
                        .expectBodyList(BillResponseDTO.class)
                        .consumeWith(response -> {
                                assert response.getResponseBody() != null;
                                // Compare using doubleValue to avoid BigDecimal precision issues (1.50 vs 1.5)
                                assertEquals(calculatedInterest.doubleValue(), response.getResponseBody().get(0).getInterest().doubleValue());
                        });
                verify(billService, times(1)).getBillsByCustomerId(overdueBill.getCustomerId());
        }
}