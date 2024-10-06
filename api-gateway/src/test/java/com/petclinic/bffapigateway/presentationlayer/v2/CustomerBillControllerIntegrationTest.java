// package com.petclinic.bffapigateway.presentationlayer.v2;

// import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
// import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigBillService;
// import org.junit.jupiter.api.AfterAll;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.TestInstance;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.reactive.server.WebTestClient;
// import reactor.core.publisher.Mono;

// import static org.junit.jupiter.api.Assertions.assertNotNull;

// @SpringBootTest
// @AutoConfigureWebTestClient
// @ActiveProfiles("test")
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// public class CustomerBillControllerIntegrationTest {

//     @Autowired
//     private WebTestClient webTestClient;

//     private MockServerConfigBillService mockServerConfigBillService;
//     private MockServerConfigAuthService mockServerConfigAuthService;

//     @BeforeAll
//     public void startMockServer() {
//         mockServerConfigBillService = new MockServerConfigBillService();
//         mockServerConfigBillService.registerDownloadBillPdfEndpoint(); 

//         mockServerConfigAuthService = new MockServerConfigAuthService();
//         mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint(); 
//     }

//     @AfterAll
//     public void stopMockServer() {
//         mockServerConfigBillService.stopMockServer();
//         mockServerConfigAuthService.stopMockServer();
//     }

//     @Test
//     public void testDownloadBillPdf_Integration() {
//         // Arrange: Mocking the service call to return a PDF byte array
//         byte[] pdfContent = "Sample PDF Content".getBytes();
    
//         // Act & Assert: Mocking a request as a valid customer with a token in a cookie
//         webTestClient.get()
//             .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
//             .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidOwnerId)  // Use cookie instead of Authorization header
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
    
    
// }

package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint(); 
    }

    @AfterAll
    public void stopMockServer() {
        // Stop the mock servers
        mockServerConfigBillService.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    public void testDownloadBillPdf_Integration() {
        // Arrange: Mocking the service call to return a PDF byte array
        byte[] mockPdfContent = "Sample PDF Content".getBytes();

        // Act & Assert: Mocking a request as a valid customer with a token in a cookie
        webTestClient.get()
            .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
            .cookie("Bearer", MockServerConfigAuthService.jwtTokenForValidOwnerId)  // Using JWT token in cookie
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
}

