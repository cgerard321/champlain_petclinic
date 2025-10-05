package com.petclinic.bffapigateway.presentationlayer.v1.Products;

import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigProductsService;
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

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductControllerV1IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigProductsService mockServerConfigProductsServices;

    private MockServerConfigAuthService mockServerConfigAuthService;

    @Autowired
    private ProductsServiceClient productsServiceClient;

    @BeforeAll
    public void startMockServer(){
        mockServerConfigProductsServices= new MockServerConfigProductsService();
        mockServerConfigProductsServices.registerGetAllProductsEndpoint();


        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigProductsServices.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }

    @Test
    void whenGetAllProducts_thenReturnProducts() {
        webTestClient.get()
                .uri("/api/gateway/products")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(8,productResponseDTOS.size());
                });
    }

    @Test
    void whenGetAllProductsWithWrongEndpoint_thenReturnNotFound() {
        webTestClient.get()
                .uri("/api/gateway/product")  //missing "ts"
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isNotFound();
    }
}