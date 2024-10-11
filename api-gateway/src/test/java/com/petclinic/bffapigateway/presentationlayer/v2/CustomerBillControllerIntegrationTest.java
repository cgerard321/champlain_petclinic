package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigBillService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerBillControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigBillService mockServerConfig;

    @BeforeEach
    void setUp() {
        // Start the mock server and register the PDF endpoint
        mockServerConfig = new MockServerConfigBillService();
        mockServerConfig.registerDownloadBillPdfEndpoint();
    }

    @AfterEach
    void tearDown() {
        // Stop the mock server
        mockServerConfig.stopMockServer();
    }

    @Test
    public void testDownloadBillPdf_ValidToken_ShouldReturnPdf() {
        // Arrange: Mocking the service call to return a PDF byte array
        byte[] mockPdfContent = "Sample PDF Content".getBytes();

        // Act & Assert: Mocking a request as a valid customer with a token in a cookie
        webTestClient.get()
            .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
            .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidOwnerId)
            .accept(MediaType.APPLICATION_PDF)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
            .expectBody(byte[].class)
            .consumeWith(response -> {
                byte[] pdf = response.getResponseBody();
                assertNotNull(pdf);
                assert pdf.length > 0;
            });
    }

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
}
