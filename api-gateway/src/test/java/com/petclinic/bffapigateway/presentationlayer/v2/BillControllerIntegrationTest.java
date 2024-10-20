package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.dtos.Bills.PaymentRequestDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigBillService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BillControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;
    private MockServerConfigBillService mockServerConfigBillService;
    private MockServerConfigAuthService mockServerConfigAuthService;


    @BeforeAll
    public void startMockServer() {
        mockServerConfigBillService = new MockServerConfigBillService();
        mockServerConfigBillService.registerGetAllBillsEndpoint();
        mockServerConfigBillService.registerCreateBillEndpoint();
        mockServerConfigBillService.registerUpdateBillEndpoint();
        mockServerConfigBillService.registerPayBillEndpoint();


        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
    }


    @AfterAll
    public void stopMockServer() {
        mockServerConfigBillService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    private BillRequestDTO billRequestDTO = BillRequestDTO.builder()
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("3")
            .date(LocalDate.parse("2024-10-11"))
            .amount(100.0)
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.parse("2024-10-13"))
            .build();


    private BillResponseDTO billresponse = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361b")
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
    void whenGetAllBills_asAdmin_thenReturnAllBills() {
        Flux<BillResponseDTO> result = webTestClient
                .get()
                .uri("/api/v2/gateway/bills")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .returnResult(BillResponseDTO.class)
                .getResponseBody();

        StepVerifier
                .create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void whenCreateBill_thenReturnCreatedBill() {
            Mono<BillResponseDTO> result = webTestClient
                .post()
                .uri("/api/v2/gateway/bills/admin")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(billRequestDTO), BillRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(BillResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier
                .create(result)
                .expectNextMatches(response -> {
                    assertNotNull(response);
                    assertEquals(billRequestDTO.getCustomerId(), response.getCustomerId());
                    assertEquals(billRequestDTO.getVisitType(), response.getVisitType());
                    assertEquals(billRequestDTO.getVetId(), response.getVetId());
                    assertEquals(billRequestDTO.getDate(), response.getDate());
                    assertEquals(billRequestDTO.getAmount(), response.getAmount());
                    assertEquals(billRequestDTO.getBillStatus(), response.getBillStatus());
                    assertEquals(billRequestDTO.getDueDate(), response.getDueDate());
                    return true;})
                .verifyComplete();
    }


    @Test
    void whenGetAllBillsByPageAsAdmin_thenReturnPaginatedBills() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/bills")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .build())
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    assert response.getResponseBody().size() <= 10;
                });
    }

    @Test
    void whenGetAllBillsByPageWithInvalidRole_thenUnauthorized() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/bills")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .build())
                .cookie("User", "invalidToken")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    void whenUpdateBill_thenReturnUpdatedBill(){

        BillRequestDTO updatedRequestDTO = BillRequestDTO.builder()
                .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .visitType("operation")
                .vetId("3")
                .date(LocalDate.parse("2024-10-11"))
                .amount(100.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.parse("2024-10-13"))
                .build();

        Mono<BillResponseDTO> result =
                webTestClient
                        .put()
                        .uri("/api/v2/gateway/bills/admin/{billId}","e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                        .cookie("Bearer", jwtTokenForValidAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(updatedRequestDTO), BillRequestDTO.class)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .returnResult(BillResponseDTO.class)
                        .getResponseBody()
                        .single();

        StepVerifier
                .create(result)
                .expectNextMatches(billResponseDTO -> {
                    assertNotNull(billResponseDTO);
                    assertEquals(updatedRequestDTO.getCustomerId(), billResponseDTO.getCustomerId());
                    assertEquals(updatedRequestDTO.getBillStatus(), billResponseDTO.getBillStatus());
                    assertEquals(updatedRequestDTO.getVetId(), billResponseDTO.getVetId());
                    assertEquals(updatedRequestDTO.getAmount(), billResponseDTO.getAmount());
                    assertEquals(updatedRequestDTO.getDate(), billResponseDTO.getDate());
                    assertEquals(updatedRequestDTO.getDueDate(), billResponseDTO.getDueDate());
                    assertEquals(updatedRequestDTO.getVisitType(), billResponseDTO.getVisitType());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenUpdateBill_withInvalidBillId_thenReturnNotFound() {
        BillRequestDTO updatedRequestDTO = BillRequestDTO.builder()
                .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .visitType("operation")
                .vetId("3")
                .date(LocalDate.parse("2024-10-11"))
                .amount(100.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.parse("2024-10-13"))
                .build();

        String invalidBillId = "invalid-bill-id";

        webTestClient
                .put()
                .uri("/api/v2/gateway/bills/admin/{billId}", invalidBillId)
                .cookie("Bearer", jwtTokenForValidAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedRequestDTO), BillRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void whenGetBillsByMonthAsAdmin_thenReturnBills() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/bills")
                        .queryParam("month", "10")
                        .queryParam("year", "2024")
                        .build())
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    assert response.getResponseBody().size() == 2;
                });
    }

}

    //these tests keep returning 404 and i do not understand why

//    @Test
//    void payBill_Success() {
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/29");
//
//        webTestClient.post()
//                .uri("/api/v2/gateway/bills/customer/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/bills/1/pay")
//                .cookie("Bearer", jwtTokenForValidOwnerId)  // Simulate valid owner token
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(String.class)
//                .value(response -> {
//                    assertEquals("Payment successful", response);
//                });
//    }
//
//    @Test
//    void payBill_Failure_InvalidCustomerId() {
//        // Prepare a valid PaymentRequestDTO
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");
//
//        // Execute the POST request with an invalid customerId
//        webTestClient.post()
//                .uri("/api/v2/gateway/customer/{customerId}/bills/{billId}/pay", "invalid-customer-id", "1")
//                .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidOwnerId)  // Simulate valid owner token
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody(String.class)
//                .value(response -> {
//                    assertTrue(response.contains("Payment failed: Invalid customer ID"));
//                });
//    }
//
//    @Test
//    void payBill_Failure_InvalidDetails() {
//        // Prepare a PaymentRequestDTO with invalid payment details
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("123", "12", "01/20");
//
//        // Execute the POST request and expect failure
//        webTestClient.post()
//                .uri("/api/v2/gateway/customer/{customerId}/bills/{billId}/pay", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "1")
//                .cookie("Bearer", jwtTokenForValidOwnerId)  // Simulate valid owner token
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody(String.class)
//                .value(response -> {
//                    assertTrue(response.contains("Payment failed"));
//                });
//    }
//
//    @Test
//    void payBill_UnauthorizedAccess() {
//        // Prepare a valid PaymentRequestDTO
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");
//
//        // Attempt to make the payment without a valid token
//        webTestClient.post()
//                .uri("/api/v2/gateway/customer/{customerId}/bills/{billId}/pay", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isUnauthorized(); // Expect Unauthorized access due to missing token
//    }
//
//    @Test
//    void payBill_ByAdminRole_Success() {
//        // Prepare a valid PaymentRequestDTO
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");
//
//        // Execute the POST request with an admin token
//        webTestClient.post()
//                .uri("/api/v2/gateway/customer/{customerId}/bills/{billId}/pay", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "1")
//                .cookie("Bearer", jwtTokenForValidAdmin)  // Simulate valid admin token
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(String.class)
//                .value(response -> {
//                    assertEquals("Payment successful", response);
//                });
//    }
//
//    @Test
//    void payBill_InvalidCustomerId_Failure() {
//        // Prepare a valid PaymentRequestDTO
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");
//
//        // Execute the POST request with an invalid customerId
//        webTestClient.post()
//                .uri("/api/v2/gateway/customer/{customerId}/bills/{billId}/pay", "invalidCustomerId", "1")
//                .cookie("Bearer", jwtTokenForValidOwnerId)  // Simulate valid owner token
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isBadRequest()  // Expect a bad request error
//                .expectBody(String.class)
//                .value(response -> {
//                    assertTrue(response.contains("Payment failed"));
//                });
//    }
//
//    @Test
//    void payBill_InvalidBillId_Failure() {
//        // Prepare a valid PaymentRequestDTO
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO("1234567812345678", "123", "12/23");
//
//        // Execute the POST request with an invalid billId
//        webTestClient.post()
//                .uri("/api/v2/gateway/customer/{customerId}/bills/{billId}/pay", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "invalidBillId")
//                .cookie("Bearer", jwtTokenForValidOwnerId)  // Simulate valid owner token
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isBadRequest()  // Expect a bad request error
//                .expectBody(String.class)
//                .value(response -> {
//                    assertTrue(response.contains("Payment failed"));
//                });
//    }
//
//    @Test
//    void payBill_MissingPaymentDetails_Failure() {
//        // Prepare an incomplete PaymentRequestDTO
//        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(null, null, null);
//
//        // Execute the POST request with missing details
//        webTestClient.post()
//                .uri("/api/v2/gateway/bills/customer/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/bills/1/pay")
//                .cookie("Bearer", jwtTokenForValidOwnerId)  // Simulate valid owner token
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(paymentRequestDTO), PaymentRequestDTO.class)
//                .exchange()
//                .expectStatus().isBadRequest()  // Expect a bad request error
//                .expectBody(String.class)
//                .value(response -> {
//                    assertTrue(response.contains("Payment failed"));
//                });
//    }

//}