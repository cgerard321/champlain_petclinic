package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigBillService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;
import static org.junit.jupiter.api.Assertions.*;

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
        mockServerConfigBillService.registerDownloadStaffBillPdfEndpoint();


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
            .amount(new BigDecimal("100.0"))
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
                .uri("/api/gateway/bills")
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
                    assertEquals(0, billRequestDTO.getAmount().compareTo(response.getAmount()));
                    assertEquals(billRequestDTO.getBillStatus(), response.getBillStatus());
                    assertEquals(billRequestDTO.getDueDate(), response.getDueDate());
                    return true;
                })
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
    void whenUpdateBill_thenReturnUpdatedBill() {

        BillRequestDTO updatedRequestDTO = BillRequestDTO.builder()
                .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .visitType("operation")
                .vetId("3")
                .date(LocalDate.parse("2024-10-11"))
                .amount(new BigDecimal("100.0"))
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.parse("2024-10-13"))
                .build();

        Mono<BillResponseDTO> result =
                webTestClient
                        .put()
                        .uri("/api/v2/gateway/bills/admin/{billId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
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
                .amount(new BigDecimal("100.0"))
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

    @Test
    void whenGetInterest_withInvalidRole_thenUnauthorized() {
        String billId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";

        webTestClient.get()
                .uri("/api/v2/gateway/bills/admin/{billId}/interest", billId)
                .cookie("User", "invalidToken")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenGetTotal_withInvalidRole_thenUnauthorized() {
        String billId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361b";

        webTestClient.get()
                .uri("/api/v2/gateway/bills/admin/{billId}/total", billId)
                .cookie("User", "invalidToken")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // --- Customer endpoints (user-specific) ---

    @Test
    void whenGetInterestForCustomer_withInvalidRole_thenUnauthorized() {
        String customerId = "cust-123";
        String billId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361c";

        webTestClient.get()
                .uri("/api/v2/gateway/bills/customer/{customerId}/bills/{billId}/interest", customerId, billId)
                .cookie("User", "invalidToken")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenGetTotalForCustomer_withInvalidRole_thenUnauthorized() {
        String customerId = "cust-123";
        String billId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361d";

        webTestClient.get()
                .uri("/api/v2/gateway/bills/customer/{customerId}/bills/{billId}/total", customerId, billId)
                .cookie("User", "invalidToken")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    void testDownloadStaffBillPdf_ShouldReturnPdf() {
        webTestClient.get()
                .uri("/api/v2/gateway/bills/{billId}/pdf", "staffBill-1")
                // ✅ simulate valid admin login
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    byte[] pdf = response.getResponseBody();
                    assertNotNull(pdf);
                    assertTrue(pdf.length > 0, "PDF bytes should not be empty");
                });
    }

    @Test
    void testDownloadStaffBillPdf_BillNotFound_ShouldReturn500() {
        webTestClient.get()
                .uri("/api/v2/gateway/bills/{billId}/pdf", "nonexistent-bill-id")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isEqualTo(500);
    }
}