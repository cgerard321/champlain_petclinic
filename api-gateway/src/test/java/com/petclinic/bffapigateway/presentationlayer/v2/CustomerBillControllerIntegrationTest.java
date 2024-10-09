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
    public void testDownloadBillPdf_Integration() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    byte[] pdf = response.getResponseBody();
                    assert pdf != null;
                    assert pdf.length > 0;
                });
    }
}
