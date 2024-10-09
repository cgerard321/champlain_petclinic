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

    @Test
    public void downloadBillPdf_ShouldReturnPdfFile() {
        // Arrange
        byte[] pdfContent = "Sample PDF Content".getBytes(); // Simulating PDF content
        when(billServiceClient.downloadBillPdf(anyString(), anyString())).thenReturn(Mono.just(pdfContent));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    assert response.getResponseBody() != null;
                    assert response.getResponseBody().length > 0;
                });

        // Verify the service method was called
        Mockito.verify(billServiceClient, Mockito.times(1)).downloadBillPdf(anyString(), anyString());
    }

    @Test
    public void downloadBillPdf_ShouldReturnInternalServerErrorOnFailure() {
        // Arrange
        when(billServiceClient.downloadBillPdf(anyString(), anyString())).thenReturn(Mono.error(new RuntimeException("Error")));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v2/gateway/customers/1/bills/1234/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
