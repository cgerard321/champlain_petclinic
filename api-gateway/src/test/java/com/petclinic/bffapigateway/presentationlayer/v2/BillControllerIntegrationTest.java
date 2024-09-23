package com.petclinic.bffapigateway.presentationlayer.v2;

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
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.*;


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
        mockServerConfigBillService.registerDeleteBillEndpoint();


        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
    }


    @AfterAll
    public void stopMockServer() {
        mockServerConfigBillService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }


    private BillResponseDTO billresponse = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("3")
            .date(LocalDate.parse("2024-10-11"))
            .amount(100.0)
            .taxedAmount(0.0)
            .billStatus(BillStatus.PAID)
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

    private BillResponseDTO billresponse3 = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361c")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("2")
            .date(LocalDate.parse("2024-10-11"))
            .amount(120.0)
            .taxedAmount(10.0)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(LocalDate.parse("2024-10-13"))
            .build();

    @Test
    void whenGetAllBills_asAdmin_thenReturnAllBills() {
        Flux<BillResponseDTO> result = webTestClient
                .get()
                .uri("/api/v2/gateway/bills/admin")
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
    void whenDeleteBill_asAdmin_thenReturnNoContent() {

        Flux<BillResponseDTO> result = webTestClient
                .delete()
                .uri("/api/v2/gateway/bills/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isNotFound()
                .returnResult(BillResponseDTO.class)
                .getResponseBody();

        StepVerifier
                .create(result)
                .expectNextCount(0);
    }

    @Test
    void whenDeleteUnpaidBill_asAdmin_thenReturnUnprocessableEntity() {
        webTestClient
                .delete()
                .uri("/api/v2/gateway/bills/e6c7398e-8ac4-4e10-9ee0-03ef33f0361b")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .exchange()
                .expectStatus().isNotFound();
    }

}


