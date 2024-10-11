package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CustomerBillController.class)
public class CustomerBillControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BillServiceClient billServiceClient;

    private final String baseBillUrl = "/api/v2/gateway/customers/1/bills";

    // Test data
    private byte[] pdfContent;

    @Test
    public void downloadBillPdf_ShouldReturnPdfFile() {
        // Arrange
        //byte[] pdfContent = "Sample PDF Content".getBytes(); // Simulating PDF content
        //when(billServiceClient.downloadBillPdf(anyString(), anyString())).thenReturn(Mono.just(pdfContent));
        when(billServiceClient.downloadBillPdf("1", "1234")).thenReturn(Mono.just(pdfContent));

        // Act & Assert
        webTestClient.get()
                //.uri("/api/v2/gateway/customers/1/bills/1234/pdf")
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

    @Test
    public void downloadBillPdf_InvalidCustomer_ShouldReturnUnauthorized() {
        // Arrange
        when(billServiceClient.downloadBillPdf("invalid-customer-id", "1234"))
            .thenReturn(Mono.error(new RuntimeException("Unauthorized")));

        // Act & Assert
        webTestClient.get()
                .uri(baseBillUrl + "/invalid-id/bills/1234/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
