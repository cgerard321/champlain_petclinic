package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CustomerBillController.class)
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {CustomerBillController.class})
public class CustomerBillControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BillServiceClient billServiceClient;

    private final String baseBillUrl = "/api/v2/gateway/customers/1/bills";

    // Test data
    private byte[] pdfContent;

    @BeforeEach
    public void setup() {
        // Simulate a sample PDF file
        pdfContent = "Mock PDF Content".getBytes();
    }

    @Test
    public void downloadBillPdf_ShouldReturnPdfFile() {
        // Arrange: Mock the service to return a PDF byte array
        when(billServiceClient.downloadBillPdf("1", "1234")).thenReturn(Mono.just(pdfContent));

        // Act & Assert: Verify the PDF response
        webTestClient.get()
                .uri(baseBillUrl + "/1234/pdf")
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

    // @Test
    // public void downloadBillPdf_InvalidCustomer_ShouldReturnUnauthorized() {
    //     // Arrange: Mock the service to return an error for invalid access
    //     when(billServiceClient.downloadBillPdf("invalid-customer-id", "1234"))
    //             .thenReturn(Mono.error(new RuntimeException("Unauthorized")));

    //     // Act & Assert: Verify the unauthorized status
    //     webTestClient.get()
    //             .uri(baseBillUrl + "/invalid-id/bills/1234/pdf")
    //             .accept(MediaType.APPLICATION_PDF)
    //             .exchange()
    //             .expectStatus().isUnauthorized();
    // }

    @Test
    public void getCurrentBalance_ShouldReturnBalance() {
        
        when(billServiceClient.getCurrentBalance("1")).thenReturn(Mono.just(150.0));

        webTestClient.get()
                .uri(baseBillUrl + "/current-balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .value(balance -> assertEquals(150.0, balance));
    }

    @Test
    public void getCurrentBalance_NegativeBalance_ShouldReturnNegativeValue() {

        when(billServiceClient.getCurrentBalance("1")).thenReturn(Mono.just(-20.0));

        webTestClient.get()
                .uri(baseBillUrl + "/current-balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class)
                .value(balance -> assertEquals(-20.0, balance));
    }
}