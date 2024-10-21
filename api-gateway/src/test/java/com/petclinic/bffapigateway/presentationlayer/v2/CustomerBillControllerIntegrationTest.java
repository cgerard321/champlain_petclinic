package com.petclinic.bffapigateway.presentationlayer.v2;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerBillControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigBillService mockServerConfigBillService;
    private MockServerConfigAuthService mockServerConfigAuthService;

    @BeforeAll
    public void startMockServer() {
        // Start the mock servers for bills and authentication services
        mockServerConfigBillService = new MockServerConfigBillService();
        mockServerConfigBillService.registerDownloadBillPdfEndpoint();

        mockServerConfigBillService.registerGetCurrentBalanceEndpoint(); 
        mockServerConfigBillService.registerGetCurrentBalanceInvalidCustomerIdEndpoint(); 
        mockServerConfigBillService.registerPayBillEndpoint();

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        // Stop the mock servers
        mockServerConfigBillService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }
//
//     @Test
//     public void testDownloadBillPdf_ValidToken_ShouldReturnPdf() {
//         // Arrange: Mocking the service call to return a PDF byte array
//         byte[] mockPdfContent = "Sample PDF Content".getBytes();
//
//         // Act & Assert: Mocking a request as a valid customer with a token in a cookie
//         webTestClient.get()
//             .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
//             .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidOwnerId)  // Using JWT token in cookie
//             .accept(MediaType.APPLICATION_PDF)
//             .exchange()
//             .expectStatus().isOk()
//             .expectHeader().contentType(MediaType.APPLICATION_PDF)
//             .expectBody(byte[].class)
//             .consumeWith(response -> {
//                 byte[] pdf = response.getResponseBody();
//                 assertNotNull(pdf);
//                 assert pdf.length > 0;
//             });
//     }

    @Test
    public void testDownloadBillPdf_InvalidToken_ShouldReturnUnauthorized() {
        // Act & Assert: Mocking a request with an invalid token
        webTestClient.get()
                .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
                .cookie("Bearer", "invalid-token") // Using an invalid JWT token
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isUnauthorized(); // Expect Unauthorized status
    }

    @Test
    public void testGetCurrentBalance_InvalidToken_ShouldReturnUnauthorized() {

        webTestClient.get()
                .uri("/api/v2/gateway/customers/1/bills/current-balance")
                .cookie("Bearer", "invalid-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // @Test
    // public void testGetCurrentBalance_ValidCustomerId_AsOwner_ReturnsBalance() {
    // String validCustomerId = "1";

    // webTestClient.get()
    // .uri("/api/v2/gateway/customers/{customerId}/bills/current-balance",
    // validCustomerId)
    // .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidOwnerId)
    // .accept(MediaType.APPLICATION_JSON)
    // .exchange()
    // .expectStatus().isOk()
    // .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
    // .expectBody(Double.class)
    // .value(balance -> assertEquals(150.0, balance));
    // }

    // @Test
    // public void testGetCurrentBalance_InvalidCustomerId_Returns404() {
    // String invalidCustomerId = "invalid-id";

    // webTestClient.get()
    // .uri("/api/v2/gateway/customers/{customerId}/bills/current-balance",
    // invalidCustomerId)
    // .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidOwnerId)
    // .accept(MediaType.APPLICATION_JSON)
    // .exchange()
    // .expectStatus().isNotFound();
    // }
}