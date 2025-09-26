package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.exceptions.NotFoundException;
import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import com.petclinic.billing.datalayer.PaymentRequestDTO;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.util.EntityDtoUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = CustomerBillsController.class)
public class CustomerBillsControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    BillService billService;

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
        double expectedBalance = 150.0;

        when(billService.calculateCurrentBalance(customerId)).thenReturn(Mono.just(expectedBalance));

        client.get()
                .uri("/bills/customer/{customerId}/bills/current-balance", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
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
                .amount(150.0)
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
                .amount(200.0)
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
    void whenDeleteBillWithInvalidCustomerId_thenReturnNotFound() {
        // given
        String invalidCustomerId = "INVALID-ID";

        // mock service to throw error
        when(billService.deleteBillsByCustomerId(invalidCustomerId))
                .thenReturn(Flux.error(new RuntimeException("Invalid customerId")));

        // when + then
        client.delete()
                .uri("/bills/customer/{customerId}", invalidCustomerId)
                .exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    void whenCreatingBillsIfBillStatusIsEmpty_thenReturnMissingVariable() {
        BillRequestDTO invalidRequest = BillRequestDTO.builder()
                .customerId("C001")
                .visitType("Regular Checkup")
                .vetId("V100")
                .date(LocalDate.now())
                .amount(75.50)
                .billStatus(null) // invalid
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        // Mock service so it doesn’t return null
        when(billService.createBill(any()))
                .thenReturn(Mono.error(new IllegalArgumentException("billStatus is required")));

        client.post()
                .uri("/bills")
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }












}